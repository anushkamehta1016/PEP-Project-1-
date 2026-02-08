package org.example;

public final class Entry<T> {
    private final T value;
    private final long expiryTime;

    public Entry(T value, long expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public T value() {
        return value;
    }

    public long expiryTime() {
        return expiryTime;
    }

    public boolean isExpired(long now) {
        return expiryTime != InMemoryDatabase.NO_EXPIRY && now >= expiryTime;
    }
}
