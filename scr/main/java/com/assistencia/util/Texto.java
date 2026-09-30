package com.assistencia.util;

public final class Texto {

    private Texto() {
    }

    /** Converte texto vazio em null (útil para colunas UNIQUE opcionais, como CPF). */
    public static String vazioParaNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
