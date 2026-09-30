package com.assistencia.service;

/** Violação de uma regra de negócio (mensagem pensada para o usuário final). */
public class RegraNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
