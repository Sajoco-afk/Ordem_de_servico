package com.assistencia.dao;

import com.assistencia.model.Servico;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicoDAO {

    public void inserir(Connection con, int osId, String descricao, BigDecimal valor) throws SQLException {
        String sql = "INSERT INTO servico_os (os_id, descricao, valor) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            ps.setString(2, descricao);
            ps.setBigDecimal(3, valor);
            ps.executeUpdate();
        }
    }

    public List<Servico> listarPorOS(Connection con, int osId) throws SQLException {
        String sql = "SELECT id, os_id, descricao, valor FROM servico_os WHERE os_id = ? ORDER BY id";
        List<Servico> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Servico(rs.getInt("id"), rs.getInt("os_id"),
                            rs.getString("descricao"), rs.getBigDecimal("valor")));
                }
            }
        }
        return lista;
    }

    public BigDecimal somaPorOS(Connection con, int osId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(valor), 0) FROM servico_os WHERE os_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }
}
