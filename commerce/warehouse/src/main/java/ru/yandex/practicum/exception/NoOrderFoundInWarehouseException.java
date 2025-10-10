package ru.yandex.practicum.exception;

public class NoOrderFoundInWarehouseException extends RuntimeException {

    public NoOrderFoundInWarehouseException(String message) {
        super(message);
    }
}
