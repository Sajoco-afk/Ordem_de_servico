package com.assistencia.model;

import static com.assistencia.model.StatusOS.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class StatusOSTest {

    @Test
    void fluxoNormalEPermitido() {
        assertTrue(ABERTA.podeIrPara(EM_ANALISE));
        assertTrue(EM_ANALISE.podeIrPara(AGUARDANDO_APROVACAO));
        assertTrue(AGUARDANDO_APROVACAO.podeIrPara(EM_MANUTENCAO));
        assertTrue(EM_MANUTENCAO.podeIrPara(FINALIZADA));
        assertTrue(FINALIZADA.podeIrPara(ENTREGUE));
    }

    @Test
    void naoPodePularEtapas() {
        assertFalse(ABERTA.podeIrPara(ENTREGUE));
        assertFalse(ABERTA.podeIrPara(EM_MANUTENCAO));
        assertFalse(EM_ANALISE.podeIrPara(FINALIZADA));
    }

    @Test
    void naoPodeVoltarStatus() {
        assertFalse(EM_MANUTENCAO.podeIrPara(EM_ANALISE));
        assertFalse(FINALIZADA.podeIrPara(EM_MANUTENCAO));
    }

    @Test
    void naoPodeCancelarDepoisDeFinalizada() {
        assertFalse(FINALIZADA.podeIrPara(CANCELADA));
    }

    @Test
    void estadosFinaisNaoTemSaida() {
        assertTrue(ENTREGUE.isFinal());
        assertTrue(CANCELADA.isFinal());
        assertTrue(ENTREGUE.proximosPermitidos().isEmpty());
        assertTrue(CANCELADA.proximosPermitidos().isEmpty());
    }

    @Test
    void proximosPermitidosDeAberta() {
        assertEquals(List.of(EM_ANALISE, CANCELADA), ABERTA.proximosPermitidos());
    }
}
