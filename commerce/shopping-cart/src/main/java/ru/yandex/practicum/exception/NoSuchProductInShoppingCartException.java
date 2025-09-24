package ru.yandex.practicum.exception;

public class NoSuchProductInShoppingCartException extends RuntimeException {

    public NoSuchProductInShoppingCartException(String message) {
        super(message);
    }
}
