package com.levandr.custompaymentsystem.exception;


public class FileProcessingException extends Exception {

    public FileProcessingException() {
        super("An error occurred while processing the file.");
    }

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileProcessingException(Throwable cause) {
        super(cause);
    }
}
