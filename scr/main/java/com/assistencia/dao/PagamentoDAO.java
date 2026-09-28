package com.assistencia.dao;

import com.assistencia.model.FormaPagamento;
import com.assistencia.model.Pagamento;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagamentoDAO {

    public void inserir(Connection con, int osId, BigDecimal valor, FormaPagamento forma) throws SQLException {
        String sql = "INSERT INTO pagamento (os_id, valor, forma) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            ps.setBigDecimal(2, valor);
            ps.setString(3, forma.name());
            ps.executeUpdate();
        }
    }

    public List<Pagamento> listarPorOS(Connection con, int osId) throws SQLException {
        String sql = "SELECT id, os_id, valor, forma, data_pagamento FROM pagamento WHERE os_id = ? ORDER BY id";
        List<Pagamento> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Pagamento(rs.getInt("id"), rs.getInt("os_id"), rs.getBigDecimal("valor"),
                            FormaPagamento.valueOf(rs.getString("forma")),
                            rs.getObject("data_pagamento", LocalDateTime.class)));
                }
            }
        }
        return lista;
    }

    public BigDecimal somaPorOS(Connection con, int osId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor), 0) FROM pagamento WHERE os_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }
}
