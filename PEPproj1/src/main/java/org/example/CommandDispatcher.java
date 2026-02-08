package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommandDispatcher implements AutoCloseable {
    private final ExecutorService executor;
    private final CommandExecutor commandExecutor;

    public CommandDispatcher(CommandExecutor commandExecutor, int workerCount) {
        this.commandExecutor = commandExecutor;
        this.executor = Executors.newFixedThreadPool(workerCount);
    }

    public void dispatch(Command command) {
        executor.submit(() -> {
            try {
                commandExecutor.execute(command);
            } catch (DatabaseStoppedException
                     | KeyNotFoundException
                     | InvalidTTLException e) {
                System.err.println(e.getMessage());
            }
        });
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
