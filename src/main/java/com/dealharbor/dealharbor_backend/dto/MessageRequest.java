package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.MessageType;
import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private MessageType messageType;
    private String attachmentUrl;
}
