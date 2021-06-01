package com.moji.exception;

public class JSONParseErrorException extends Exception {
    static final long serialVersionUID = 7818375828126090156L;

    public JSONParseErrorException() {
        super();
    }

    public JSONParseErrorException(String message) {
        super(message);
    }

    public JSONParseErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONParseErrorException(Throwable cause) {
        super(cause);
    }
}
