package com.example.demo.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
