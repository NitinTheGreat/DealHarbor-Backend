package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.OrderStatus;
import lombok.Data;

@Data
public class OrderUpdateRequest {
    private OrderStatus status;
    private String sellerNotes;
    private String pickupLocation;
}
