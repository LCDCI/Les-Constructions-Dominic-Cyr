package com.ecp.les_constructions_dominic_cyr.backend.utils.Exception;

public class ForbiddenAccessException extends RuntimeException{
    public ForbiddenAccessException(String message) {
        super(message);
    }
}

