package com.ecp.les_constructions_dominic_cyr.backend.utils.Exception;

public class InvalidRequestException extends RuntimeException{
    public InvalidRequestException(String message) {
        super(message);
    }
}
