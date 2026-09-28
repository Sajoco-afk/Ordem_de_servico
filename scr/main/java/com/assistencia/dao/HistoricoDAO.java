package com.assistencia.dao;

import com.assistencia.model.HistoricoStatus;
import com.assistencia.model.StatusOS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    /** statusAnterior pode ser null (criação da OS). */
    public void inserir(Connection con, int osId, StatusOS anterior, StatusOS novo, String observacao)
            throws SQLException {
        String sql = "INSERT INTO historico_status (os_id, status_anterior, status_novo, observacao) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            ps.setString(2, anterior == null ? null : anterior.name());
            ps.setString(3, novo.name());
            ps.setString(4, (observacao == null || observacao.isBlank()) ? null : observacao.trim());
            ps.executeUpdate();
        }
    }

    public List<HistoricoStatus> listarPorOS(Connection con, int osId) throws SQLException {
        String sql = "SELECT id, os_id, status_anterior, status_novo, data_mudanca, observacao "
                + "FROM historico_status WHERE os_id = ? ORDER BY id";
        List<HistoricoStatus> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, osId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new HistoricoStatus(rs.getInt("id"), rs.getInt("os_id"),
                            rs.getString("status_anterior"), rs.getString("status_novo"),
                            rs.getObject("data_mudanca", LocalDateTime.class), rs.getString("observacao")));
                }
            }
        }
        return lista;
    }
}
