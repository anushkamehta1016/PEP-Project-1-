package org.example;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDatabase<T> implements AutoCloseable {
    static final long NO_EXPIRY = -1L;

    private final ConcurrentHashMap<Integer, Entry<T>> store = new ConcurrentHashMap<>();
    private final Thread cleanerThread;
    private volatile boolean running = true;
    private volatile boolean shutdown = false;

    public InMemoryDatabase() {
        cleanerThread = new Thread(this::runCleaner, "db-ttl-cleaner");
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }

    public void put(Integer key, T value) throws DatabaseStoppedException, InvalidTTLException {
        put(key, value, null);
    }

    public void put(Integer key, T value, Long ttlMillis)
            throws DatabaseStoppedException, InvalidTTLException {
        ensureRunning();
        long expiryTime = NO_EXPIRY;
        if (ttlMillis != null) {
            if (ttlMillis <= 0) {
                throw new InvalidTTLException("TTL must be greater than zero: " + ttlMillis);
            }
            expiryTime = System.currentTimeMillis() + ttlMillis;
        }
        store.put(key, new Entry<>(value, expiryTime));
    }

    public T get(Integer key) throws DatabaseStoppedException, KeyNotFoundException {
        ensureRunning();
        Entry<T> entry = store.get(key);
        if (entry == null) {
            throw new KeyNotFoundException("Key not found: " + key);
        }
        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            store.remove(key, entry);
            throw new KeyNotFoundException("Key expired: " + key);
        }
        return entry.value();
    }

    public void delete(Integer key) throws DatabaseStoppedException, KeyNotFoundException {
        ensureRunning();
        Entry<T> removed = store.remove(key);
        if (removed == null) {
            throw new KeyNotFoundException("Key not found: " + key);
        }
    }

    public void stop() {
        running = false;
    }

    public void start() {
        running = true;
    }

    private void ensureRunning() throws DatabaseStoppedException {
        if (!running) {
            throw new DatabaseStoppedException("Database is stopped.");
        }
    }

    void cleanupExpiredKeys() {
        long now = System.currentTimeMillis();
        store.forEach((key, entry) -> {
            if (entry.isExpired(now)) {
                store.remove(key, entry);
            }
        });
    }

    private void runCleaner() {
        while (!shutdown) {
            if (running) {
                cleanupExpiredKeys();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void close() {
        shutdown = true;
        cleanerThread.interrupt();
    }
}
