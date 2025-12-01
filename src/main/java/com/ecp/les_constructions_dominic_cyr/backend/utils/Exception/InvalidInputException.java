package com.ecp.les_constructions_dominic_cyr.backend.utils.Exception;

public class InvalidInputException extends RuntimeException{

    public InvalidInputException() {}

    public InvalidInputException(String message) { super(message); }

    public InvalidInputException(Throwable cause) { super(cause); }

    public InvalidInputException(String message, Throwable cause) { super(message, cause); }
}
