package com.example.demo.model;



import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String message;

    private String author;

    private LocalDate date = LocalDate.now(); // default to today

    public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	// Constructors
    public Notice() {}
    public Notice(String title, String message, String author) {
        this.title = title;
        this.message = message;
        this.author = author;
        this.date = LocalDate.now();
    }

    // Getters and Setters
    // ...
}

