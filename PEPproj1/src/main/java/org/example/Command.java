package org.example;

public final class Command {
    private final CommandType type;
    private final Integer key;
    private final String rawValue;
    private final Long ttl;

    public Command(CommandType type, Integer key, String rawValue, Long ttl) {
        this.type = type;
        this.key = key;
        this.rawValue = rawValue;
        this.ttl = ttl;
    }

    public CommandType type() {
        return type;
    }

    public Integer key() {
        return key;
    }

    public String rawValue() {
        return rawValue;
    }

    public Long ttl() {
        return ttl;
    }
}
