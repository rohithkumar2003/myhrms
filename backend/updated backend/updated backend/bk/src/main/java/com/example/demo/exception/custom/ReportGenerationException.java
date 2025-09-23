package com.example.demo.exception.custom;



public class ReportGenerationException extends RuntimeException {
    public ReportGenerationException(String message) {
        super("Failed to generate report: " + message);
    }
}
