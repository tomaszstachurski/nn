package com.example.nn;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest {

    @NotBlank(message = "uuid is mandatory")
    private String uuid;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be more than zero")
    private BigDecimal amount;

    @NotNull(message = "From is mandatory")
    private Currency from;
}
