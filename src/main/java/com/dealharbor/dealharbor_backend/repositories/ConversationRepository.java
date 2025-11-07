package com.dealharbor.dealharbor_backend.repositories;

import com.dealharbor.dealharbor_backend.entities.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    
    @Query("SELECT c FROM Conversation c WHERE (c.user1.id = :userId OR c.user2.id = :userId) AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findUserConversations(String userId, Pageable pageable);
    
    @Query("SELECT c FROM Conversation c WHERE ((c.user1.id = :user1Id AND c.user2.id = :user2Id) OR (c.user1.id = :user2Id AND c.user2.id = :user1Id)) AND c.isActive = true")
    Optional<Conversation> findConversationBetweenUsers(String user1Id, String user2Id);
    
    @Query("SELECT c FROM Conversation c WHERE ((c.user1.id = :user1Id AND c.user2.id = :user2Id) OR (c.user1.id = :user2Id AND c.user2.id = :user1Id)) AND c.product.id = :productId AND c.isActive = true")
    Optional<Conversation> findConversationBetweenUsersForProduct(String user1Id, String user2Id, String productId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation.id IN (SELECT c.id FROM Conversation c WHERE (c.user1.id = :userId OR c.user2.id = :userId)) AND m.sender.id != :userId AND m.isRead = false")
    long countUnreadMessages(String userId);
}
