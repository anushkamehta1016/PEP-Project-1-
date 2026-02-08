# Command-Driven In-Memory Database

## Overview
This project implements a generic, thread-safe, in-memory database that accepts commands via `System.in`. It supports integer keys, optional TTL, start/stop lifecycle control, and a background cleanup thread.

## OOP Design
- `Command`, `CommandType`, `CommandParser` handle parsing and validation.
- `InMemoryDatabase<T>` encapsulates storage and lifecycle behavior.
- `Entry<T>` stores values with an expiry timestamp.
- `CommandExecutor` executes parsed commands against the database.
- `CommandDispatcher` manages a worker pool to execute commands concurrently.

## Thread Safety Strategy
- Storage uses `ConcurrentHashMap` for scalable concurrent access.
- Expired key removal uses `store.remove(key, entry)` to avoid races.
- The cleaner thread iterates using `forEach` on the concurrent map to prevent `ConcurrentModificationException`.
- Lifecycle uses `volatile boolean running` to ensure stop/start visibility across threads.

## TTL Handling
- TTL is stored as absolute epoch time in milliseconds.
- `expiryTime = -1` means no expiry.
- `GET` checks expiry and removes expired entries lazily.

## Command Syntax
```
PUT <key> <value>
PUT <key> <value> <ttlMillis>
GET <key>
DELETE <key>
STOP
START
EXIT
```

## Sample Input
```
PUT 3 hello
PUT 5 100 3000
GET 3
DELETE 5
STOP
START
EXIT
```

## Multithreaded Execution Demo
Commands are dispatched to a fixed thread pool sized by CPU count. This means output order may differ from input order, which demonstrates concurrent access.

Example run:
```
mvn -q -DskipTests package
java -cp target/classes org.example.Main
```

Then paste or pipe commands from a file:
```
type commands.txt | java -cp target/classes org.example.Main
```

## Exceptions
- `InvalidCommandException` / `InvalidTTLException`: parsing and validation failures.
- `DatabaseStoppedException`: operations attempted while stopped.
- `KeyNotFoundException`: missing or expired keys.
