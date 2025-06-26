package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, String> {
    Page<Message> findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(String conversationId, Pageable pageable);
    
    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP WHERE m.conversation.id = :conversationId AND m.sender.id != :userId AND m.isRead = false")
    void markConversationMessagesAsRead(String conversationId, String userId);
    
    long countByConversationIdAndSenderIdNotAndIsReadFalse(String conversationId, String senderId);
}
