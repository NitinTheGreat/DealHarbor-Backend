package com.dealharbor.dealharbor_backend.dto;

import com.dealharbor.dealharbor_backend.enums.ProductCondition;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductSearchRequest {
    private String keyword;
    private String categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<ProductCondition> conditions;
    private String location;    
    private Boolean deliveryAvailable;
    private Boolean isNegotiable;
    private Boolean verifiedStudentsOnly; // Filter for verified student sellers
    private String sortBy; // price_asc, price_desc, date_asc, date_desc, popularity
    private Integer page;
    private Integer size;
}
