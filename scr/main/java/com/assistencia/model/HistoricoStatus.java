package com.assistencia.model;

import java.time.LocalDateTime;

public record HistoricoStatus(int id, int osId, String statusAnterior, String statusNovo,
                              LocalDateTime dataMudanca, String observacao) {
}
