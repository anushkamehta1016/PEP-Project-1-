package org.example;

public class CommandParser {
    public Command parse(String input) throws InvalidCommandException {
        String[] tokens = input.trim().split("\\s+");
        if (tokens.length == 0) {
            throw new InvalidCommandException("Empty command.");
        }

        CommandType type = parseCommandType(tokens[0]);

        switch (type) {
            case PUT:
                return parsePut(tokens);
            case GET:
                return parseGet(tokens);
            case DELETE:
                return parseDelete(tokens);
            case STOP:
            case START:
            case EXIT:
                return parseNoArg(type, tokens);
            default:
                throw new InvalidCommandException("Unsupported command: " + tokens[0]);
        }
    }

    private Command parsePut(String[] tokens) throws InvalidCommandException {
        if (tokens.length != 3 && tokens.length != 4) {
            throw new InvalidCommandException("PUT requires 3 or 4 tokens: PUT <key> <value> [ttlMillis].");
        }

        Integer key = parseKey(tokens[1]);
        String rawValue = tokens[2];
        Long ttl = null;

        if (tokens.length == 4) {
            ttl = parseTtl(tokens[3]);
        }

        return new Command(CommandType.PUT, key, rawValue, ttl);
    }

    private Command parseGet(String[] tokens) throws InvalidCommandException {
        if (tokens.length != 2) {
            throw new InvalidCommandException("GET requires 2 tokens: GET <key>.");
        }

        Integer key = parseKey(tokens[1]);
        return new Command(CommandType.GET, key, null, null);
    }

    private Command parseDelete(String[] tokens) throws InvalidCommandException {
        if (tokens.length != 2) {
            throw new InvalidCommandException("DELETE requires 2 tokens: DELETE <key>.");
        }

        Integer key = parseKey(tokens[1]);
        return new Command(CommandType.DELETE, key, null, null);
    }

    private Command parseNoArg(CommandType type, String[] tokens) throws InvalidCommandException {
        if (tokens.length != 1) {
            throw new InvalidCommandException(type + " does not accept arguments.");
        }
        return new Command(type, null, null, null);
    }

    private CommandType parseCommandType(String token) throws InvalidCommandException {
        try {
            return CommandType.valueOf(token.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidCommandException("Unknown command: " + token);
        }
    }

    private Integer parseKey(String token) throws InvalidCommandException {
        try {
            return Integer.valueOf(token);
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("Key must be an integer: " + token);
        }
    }

    private Long parseTtl(String token) throws InvalidCommandException {
        try {
            long ttl = Long.parseLong(token);
            if (ttl <= 0) {
                throw new InvalidTTLException("TTL must be greater than zero: " + token);
            }
            return ttl;
        } catch (NumberFormatException e) {
            throw new InvalidTTLException("TTL must be a long integer: " + token);
        }
    }
}
