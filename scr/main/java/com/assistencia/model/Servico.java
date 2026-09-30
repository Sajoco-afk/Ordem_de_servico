package com.assistencia.model;

import java.math.BigDecimal;

public record Servico(int id, int osId, String descricao, BigDecimal valor) {
}
