package com.assistencia.dao;

import com.assistencia.model.Equipamento;
import com.assistencia.util.ConexaoFactory;
import com.assistencia.util.Texto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EquipamentoDAO {

    public int inserir(Equipamento e) throws SQLException {
        String sql = "INSERT INTO equipamento (cliente_id, tipo, marca, modelo, num_serie) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.clienteId());
            ps.setString(2, e.tipo());
            ps.setString(3, Texto.vazioParaNull(e.marca()));
            ps.setString(4, Texto.vazioParaNull(e.modelo()));
            ps.setString(5, Texto.vazioParaNull(e.numSerie()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public Optional<Equipamento> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, cliente_id, tipo, marca, modelo, num_serie FROM equipamento WHERE id = ?";
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Equipamento> listarPorCliente(int clienteId) throws SQLException {
        String sql = "SELECT id, cliente_id, tipo, marca, modelo, num_serie FROM equipamento "
                + "WHERE cliente_id = ? ORDER BY id";
        List<Equipamento> lista = new ArrayList<>();
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Equipamento mapear(ResultSet rs) throws SQLException {
        return new Equipamento(rs.getInt("id"), rs.getInt("cliente_id"), rs.getString("tipo"),
                rs.getString("marca"), rs.getString("modelo"), rs.getString("num_serie"));
    }
}
