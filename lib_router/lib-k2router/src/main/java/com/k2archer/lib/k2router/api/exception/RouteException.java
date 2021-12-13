package com.k2archer.lib.k2router.api.exception;

public class RouteException extends RuntimeException {
    public static final String NONE_DESTINATION = " none destination";

    public RouteException(String message) {
        super(message);
    }
}
