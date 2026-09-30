package com.assistencia.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Cria conexões JDBC. Configure por variáveis de ambiente:
 * DB_URL, DB_USER e DB_PASS (há valores padrão para ambiente local).
 */
public final class ConexaoFactory {

    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/assistencia_tecnica"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo");
    private static final String USER = env("DB_USER", "root");
    private static final String PASS = env("DB_PASS", "");

    private ConexaoFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private static String env(String chave, String padrao) {
        String valor = System.getenv(chave);
        return (valor == null || valor.isBlank()) ? padrao : valor;
    }
}
