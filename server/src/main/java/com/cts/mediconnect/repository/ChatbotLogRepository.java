package com.cts.mediconnect.repository;

import com.cts.mediconnect.model.ChatbotLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotLogRepository extends JpaRepository<ChatbotLog, Integer> {
    List<ChatbotLog> findByUser_UserId(Integer userId);
}
