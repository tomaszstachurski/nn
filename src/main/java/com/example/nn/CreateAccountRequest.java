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
public class CreateAccountRequest {

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "Lastname is mandatory")
    private String lastName;

    @NotNull(message = "Balance is mandatory")
    @DecimalMin(value = "0.0", inclusive = false, message = "Balance must be more than zero")
    BigDecimal balance;
}
