package ru.practicum.shareit.exceptions;

public class EmptyInformationException extends RuntimeException {
    public EmptyInformationException(String message) {
        super(message);
    }
}
