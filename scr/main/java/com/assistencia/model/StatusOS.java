package com.assistencia.model;

import java.util.Arrays;
import java.util.List;

/**
 * Status de uma Ordem de Serviço e as transições permitidas entre eles.
 */
public enum StatusOS {
    ABERTA,
    EM_ANALISE,
    AGUARDANDO_APROVACAO,
    EM_MANUTENCAO,
    FINALIZADA,
    ENTREGUE,
    CANCELADA;

    public boolean podeIrPara(StatusOS destino) {
        return switch (this) {
            case ABERTA -> destino == EM_ANALISE || destino == CANCELADA;
            case EM_ANALISE -> destino == AGUARDANDO_APROVACAO || destino == CANCELADA;
            case AGUARDANDO_APROVACAO -> destino == EM_MANUTENCAO || destino == CANCELADA;
            case EM_MANUTENCAO -> destino == FINALIZADA || destino == CANCELADA;
            case FINALIZADA -> destino == ENTREGUE;
            case ENTREGUE, CANCELADA -> false;
        };
    }

    public boolean isFinal() {
        return this == ENTREGUE || this == CANCELADA;
    }

    public List<StatusOS> proximosPermitidos() {
        return Arrays.stream(values()).filter(this::podeIrPara).toList();
    }
}
