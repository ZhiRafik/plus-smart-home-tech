package ru.yandex.practicum.exception;

public class AssemblyFailedException extends RuntimeException {

    public AssemblyFailedException(String message) {
        super(message);
    }
}
