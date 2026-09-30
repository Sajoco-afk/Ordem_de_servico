package com.assistencia.model;

import java.time.LocalDateTime;

/** Visão resumida (com joins) usada nas listagens. */
public record OrdemServicoResumo(int id, String cliente, String equipamento,
                                 StatusOS status, LocalDateTime dataAbertura) {
}
