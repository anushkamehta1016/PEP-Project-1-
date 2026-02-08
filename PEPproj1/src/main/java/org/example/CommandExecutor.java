package org.example;

public class CommandExecutor {
    private final InMemoryDatabase<String> database;

    public CommandExecutor(InMemoryDatabase<String> database) {
        this.database = database;
    }

    public void execute(Command command)
            throws DatabaseStoppedException, KeyNotFoundException, InvalidTTLException {
        switch (command.type()) {
            case PUT:
                if (command.ttl() == null) {
                    database.put(command.key(), command.rawValue());
                } else {
                    database.put(command.key(), command.rawValue(), command.ttl());
                }
                System.out.println("OK");
                break;
            case GET:
                String value = database.get(command.key());
                System.out.println(value);
                break;
            case DELETE:
                database.delete(command.key());
                System.out.println("OK");
                break;
            case STOP:
                database.stop();
                System.out.println("STOPPED");
                break;
            case START:
                database.start();
                System.out.println("STARTED");
                break;
            case EXIT:
                break;
            default:
                throw new IllegalStateException("Unsupported command: " + command.type());
        }
    }
}
