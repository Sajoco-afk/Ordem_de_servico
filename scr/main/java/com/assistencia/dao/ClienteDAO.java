package com.assistencia.dao;

import com.assistencia.model.Cliente;
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

public class ClienteDAO {

    public int inserir(Cliente c) throws SQLException {
        String sql = "INSERT INTO cliente (nome, cpf, telefone, email) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.nome());
            ps.setString(2, Texto.vazioParaNull(c.cpf()));
            ps.setString(3, Texto.vazioParaNull(c.telefone()));
            ps.setString(4, Texto.vazioParaNull(c.email()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void atualizar(Cliente c) throws SQLException {
        String sql = "UPDATE cliente SET nome = ?, cpf = ?, telefone = ?, email = ? WHERE id = ?";
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.nome());
            ps.setString(2, Texto.vazioParaNull(c.cpf()));
            ps.setString(3, Texto.vazioParaNull(c.telefone()));
            ps.setString(4, Texto.vazioParaNull(c.email()));
            ps.setInt(5, c.id());
            ps.executeUpdate();
        }
    }

    public Optional<Cliente> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone, email FROM cliente WHERE id = ?";
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT id, nome, cpf, telefone, email FROM cliente ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        try (Connection con = ConexaoFactory.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        return new Cliente(rs.getInt("id"), rs.getString("nome"), rs.getString("cpf"),
                rs.getString("telefone"), rs.getString("email"));
    }
}
