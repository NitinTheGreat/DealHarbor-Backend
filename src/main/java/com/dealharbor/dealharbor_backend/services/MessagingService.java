package com.dealharbor.dealharbor_backend.services;

import com.dealharbor.dealharbor_backend.dto.*;
import com.dealharbor.dealharbor_backend.entities.*;
import com.dealharbor.dealharbor_backend.enums.NotificationType;
import com.dealharbor.dealharbor_backend.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessagingService {
    
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Transactional
    public ConversationResponse startConversation(String otherUserId, String productId, Authentication authentication) {
        User currentUser = getUserFromAuthentication(authentication);
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (currentUser.getId().equals(otherUserId)) {
            throw new RuntimeException("Cannot start conversation with yourself");
        }
        
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        }
        
        // Check if conversation already exists
        Conversation existingConversation;
        if (product != null) {
            existingConversation = conversationRepository
                    .findConversationBetweenUsersForProduct(currentUser.getId(), otherUserId, productId)
                    .orElse(null);
        } else {
            existingConversation = conversationRepository
                    .findConversationBetweenUsers(currentUser.getId(), otherUserId)
                    .orElse(null);
        }
        
        if (existingConversation != null) {
            return convertToConversationResponse(existingConversation, currentUser.getId());
        }
        
        // Create new conversation
        Conversation conversation = Conversation.builder()
                .user1(currentUser)
                .user2(otherUser)
                .product(product)
                .isActive(true)
                .build();
        
        conversation = conversationRepository.save(conversation);
        
        return convertToConversationResponse(conversation, currentUser.getId());
    }

    @Transactional
    public MessageResponse sendMessage(String conversationId, MessageRequest request, Authentication authentication) {
        User sender = getUserFromAuthentication(authentication);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify user is part of conversation
        if (!conversation.getUser1().getId().equals(sender.getId()) && 
            !conversation.getUser2().getId().equals(sender.getId())) {
            throw new RuntimeException("Access denied to this conversation");
        }
        
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .messageType(request.getMessageType())
                .attachmentUrl(request.getAttachmentUrl())
                .build();
        
        message = messageRepository.save(message);
        
        // Update conversation last message time
        conversation.setLastMessageAt(Instant.now());
        conversationRepository.save(conversation);
        
        // Notify the other user
        String recipientId = conversation.getUser1().getId().equals(sender.getId()) 
                ? conversation.getUser2().getId() 
                : conversation.getUser1().getId();
        
        notificationService.createNotification(
                recipientId,
                "New Message",
                "You have a new message from " + sender.getName(),
                NotificationType.NEW_MESSAGE,
                "/messages/" + conversationId,
                conversationId,
                "CONVERSATION"
        );
        
        return convertToMessageResponse(message);
    }

    public PagedResponse<ConversationResponse> getUserConversations(Authentication authentication, int page, int size) {
        User user = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Conversation> conversationPage = conversationRepository.findUserConversations(user.getId(), pageable);
        
        List<ConversationResponse> content = conversationPage.getContent().stream()
                .map(conv -> convertToConversationResponse(conv, user.getId()))
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                conversationPage.getNumber(),
                conversationPage.getSize(),
                conversationPage.getTotalElements(),
                conversationPage.getTotalPages(),
                conversationPage.isFirst(),
                conversationPage.isLast(),
                conversationPage.hasNext(),
                conversationPage.hasPrevious()
        );
    }

    public PagedResponse<MessageResponse> getConversationMessages(String conversationId, Authentication authentication, int page, int size) {
        User user = getUserFromAuthentication(authentication);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify user is part of conversation
        if (!conversation.getUser1().getId().equals(user.getId()) && 
            !conversation.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this conversation");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository
                .findByConversationIdAndIsDeletedFalseOrderByCreatedAtAsc(conversationId, pageable);
        
        List<MessageResponse> content = messagePage.getContent().stream()
                .map(this::convertToMessageResponse)
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                messagePage.getNumber(),
                messagePage.getSize(),
                messagePage.getTotalElements(),
                messagePage.getTotalPages(),
                messagePage.isFirst(),
                messagePage.isLast(),
                messagePage.hasNext(),
                messagePage.hasPrevious()
        );
    }

    @Transactional
    public void markConversationAsRead(String conversationId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify user is part of conversation
        if (!conversation.getUser1().getId().equals(user.getId()) && 
            !conversation.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this conversation");
        }
        
        messageRepository.markConversationMessagesAsRead(conversationId, user.getId());
    }

    public long getUnreadMessageCount(Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        return conversationRepository.countUnreadMessages(user.getId());
    }

    /**
     * Search for sellers by name (debounce-friendly)
     */
    public PagedResponse<SellerSearchResponse> searchSellers(String query, Authentication authentication, int page, int size) {
        User currentUser = getUserFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);
        
        // Search for users who are sellers (have products) and match the query
        Page<User> sellersPage = userRepository.searchSellers(query.toLowerCase(), currentUser.getId(), pageable);
        
        List<SellerSearchResponse> content = sellersPage.getContent().stream()
                .map(seller -> {
                    // Check if conversation already exists
                    Conversation existingConv = conversationRepository
                            .findConversationBetweenUsers(currentUser.getId(), seller.getId())
                            .orElse(null);
                    
                    // Split name into first and last (if possible)
                    String fullName = seller.getName();
                    String firstName = fullName;
                    String lastName = "";
                    if (fullName != null && fullName.contains(" ")) {
                        String[] parts = fullName.split(" ", 2);
                        firstName = parts[0];
                        lastName = parts.length > 1 ? parts[1] : "";
                    }
                    
                    return SellerSearchResponse.builder()
                            .userId(seller.getId())
                            .firstName(firstName)
                            .lastName(lastName)
                            .email(seller.getEmail())
                            .profileImageUrl(seller.getProfilePhotoUrl())
                            .isVerified(seller.isVerifiedStudent())
                            .productCount(seller.getTotalListings())
                            .averageRating(seller.getSellerRating() != null ? seller.getSellerRating().doubleValue() : 0.0)
                            .totalReviews(seller.getPositiveReviews() + seller.getNegativeReviews())
                            .lastActive(seller.getLastLoginAt())
                            .isOnline(false) // Can be enhanced with Redis presence tracking
                            .hasExistingConversation(existingConv != null)
                            .existingConversationId(existingConv != null ? existingConv.getId() : null)
                            .lastMessageTime(existingConv != null ? existingConv.getLastMessageAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        return new PagedResponse<>(
                content,
                sellersPage.getNumber(),
                sellersPage.getSize(),
                sellersPage.getTotalElements(),
                sellersPage.getTotalPages(),
                sellersPage.isFirst(),
                sellersPage.isLast(),
                sellersPage.hasNext(),
                sellersPage.hasPrevious()
        );
    }

    /**
     * Get or create conversation with a specific seller
     */
    @Transactional
    public ConversationResponse getOrCreateConversationWithSeller(String sellerId, String productId, Authentication authentication) {
        User currentUser = getUserFromAuthentication(authentication);
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        if (currentUser.getId().equals(sellerId)) {
            throw new RuntimeException("Cannot create conversation with yourself");
        }
        
        Product product = null;
        if (productId != null) {
            product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
        }
        
        // Check if conversation already exists
        Conversation existingConversation;
        if (product != null) {
            existingConversation = conversationRepository
                    .findConversationBetweenUsersForProduct(currentUser.getId(), sellerId, productId)
                    .orElse(null);
        } else {
            existingConversation = conversationRepository
                    .findConversationBetweenUsers(currentUser.getId(), sellerId)
                    .orElse(null);
        }
        
        if (existingConversation != null) {
            return convertToConversationResponse(existingConversation, currentUser.getId());
        }
        
        // Create new conversation
        Conversation conversation = Conversation.builder()
                .user1(currentUser)
                .user2(seller)
                .product(product)
                .isActive(true)
                .build();
        
        conversation = conversationRepository.save(conversation);
        
        return convertToConversationResponse(conversation, currentUser.getId());
    }

    /**
     * Get conversation by ID
     */
    public ConversationResponse getConversationById(String conversationId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify user is part of conversation
        if (!conversation.getUser1().getId().equals(user.getId()) && 
            !conversation.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this conversation");
        }
        
        return convertToConversationResponse(conversation, user.getId());
    }

    /**
     * Delete/Archive conversation
     */
    @Transactional
    public void deleteConversation(String conversationId, Authentication authentication) {
        User user = getUserFromAuthentication(authentication);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        // Verify user is part of conversation
        if (!conversation.getUser1().getId().equals(user.getId()) && 
            !conversation.getUser2().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied to this conversation");
        }
        
        // Soft delete - mark as inactive
        conversation.setIsActive(false);
        conversationRepository.save(conversation);
    }

    private ConversationResponse convertToConversationResponse(Conversation conversation, String currentUserId) {
        User otherUser = conversation.getUser1().getId().equals(currentUserId) 
                ? conversation.getUser2() 
                : conversation.getUser1();
        
        // Get last message
        String lastMessage = "";
        if (conversation.getMessages() != null && !conversation.getMessages().isEmpty()) {
            Message lastMsg = conversation.getMessages().get(conversation.getMessages().size() - 1);
            lastMessage = lastMsg.getContent();
        }
        
        // Get unread count
        long unreadCount = messageRepository.countByConversationIdAndSenderIdNotAndIsReadFalse(
                conversation.getId(), currentUserId);
        
        String productImageUrl = null;
        if (conversation.getProduct() != null && conversation.getProduct().getPrimaryImage() != null) {
            productImageUrl = conversation.getProduct().getPrimaryImage().getImageUrl();
        }
        
        return new ConversationResponse(
                conversation.getId(),
                otherUser.getId(),
                otherUser.getName(),
                otherUser.getProfilePhotoUrl(),
                conversation.getProduct() != null ? conversation.getProduct().getId() : null,
                conversation.getProduct() != null ? conversation.getProduct().getTitle() : null,
                productImageUrl,
                conversation.getOrder() != null ? conversation.getOrder().getId() : null,
                lastMessage,
                conversation.getLastMessageAt(),
                unreadCount,
                conversation.getIsActive()
        );
    }

    private MessageResponse convertToMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getSender().getProfilePhotoUrl(),
                message.getContent(),
                message.getMessageType(),
                message.getAttachmentUrl(),
                message.getIsRead(),
                message.getIsEdited(),
                message.getCreatedAt(),
                message.getReadAt()
        );
    }

    private User getUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
