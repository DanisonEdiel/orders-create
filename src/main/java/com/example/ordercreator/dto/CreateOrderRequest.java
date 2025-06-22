package com.example.ordercreator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class CreateOrderRequest {
    @NotNull(message = "Items are required")
    @Size(min = 1, message = "At least one item is required")
    private List<CreateOrderItemRequest> items;
}
