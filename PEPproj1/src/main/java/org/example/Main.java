package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws IOException {
        CommandParser parser = new CommandParser();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        InMemoryDatabase<String> database = new InMemoryDatabase<>();
        CommandExecutor executor = new CommandExecutor(database);
        int workerCount = Math.max(2, Runtime.getRuntime().availableProcessors());

        try (database; CommandDispatcher dispatcher = new CommandDispatcher(executor, workerCount)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }

                try {
                    Command command = parser.parse(trimmed);
                    if (command.type() == CommandType.EXIT) {
                        break;
                    }
                    dispatcher.dispatch(command);
                } catch (InvalidCommandException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }
}
