package com.assistencia.model;

import java.time.LocalDateTime;

public record OrdemServico(int id, int equipamentoId, String defeitoRelatado, String diagnostico,
                           StatusOS status, LocalDateTime dataAbertura, LocalDateTime dataFechamento) {
}
