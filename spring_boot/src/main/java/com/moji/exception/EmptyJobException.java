package com.moji.exception;

/**
 * @description: 工程中没有有效任务的异常
 * @author: lyh
 * @date: 2021/5/28
 * @version: v1.0
 */
public class EmptyJobException extends Exception {
    static final long serialVersionUID = 7818375828146090156L;

    public EmptyJobException() {
        super();
    }

    public EmptyJobException(String message) {
        super(message);
    }

    public EmptyJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyJobException(Throwable cause) {
        super(cause);
    }
}
