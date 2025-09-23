package com.example.demo.exception.custom;



public class InvalidEmployeeDataException extends RuntimeException {
    public InvalidEmployeeDataException(String field, String reason) {
        super("Invalid employee data - " + field + ": " + reason);
    }
}