package com.assistencia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Pagamento(int id, int osId, BigDecimal valor, FormaPagamento forma, LocalDateTime dataPagamento) {
}
