package com.moji.exception;

/**
 * @description: DAG中环异常
 * @author: lyh
 * @date: 2021/5/28
 * @version: v1.0
 */
public class RingLoopException extends Exception {
    static final long serialVersionUID = 7818375828146090155L;

    public RingLoopException() {
        super();
    }

    public RingLoopException(String message) {
        super(message);
    }

    public RingLoopException(String message, Throwable cause) {
        super(message, cause);
    }

    public RingLoopException(Throwable cause) {
        super(cause);
    }
}
