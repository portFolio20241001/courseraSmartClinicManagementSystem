package com.project.back_end.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {

    private BigDecimal amount;

    @NotNull(message = "支払方法は必須です。")
    private String paymentMethod;

    @NotNull(message = "支払ステータスは必須です。")
    private String paymentStatus;

}
