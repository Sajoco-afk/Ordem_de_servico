package com.assistencia.model;

public record Equipamento(Integer id, int clienteId, String tipo, String marca, String modelo, String numSerie) {

    public String descricao() {
        StringBuilder sb = new StringBuilder(tipo);
        if (marca != null) sb.append(' ').append(marca);
        if (modelo != null) sb.append(' ').append(modelo);
        return sb.toString();
    }
}
