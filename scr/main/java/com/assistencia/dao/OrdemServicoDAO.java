package com.assistencia.dao;

import com.assistencia.model.OrdemServico;
import com.assistencia.model.OrdemServicoResumo;
import com.assistencia.model.StatusOS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Todos os métodos recebem a Connection para que o Service controle a transação.
 */
public class OrdemServicoDAO {

    public int inserir(Connection con, int equipamentoId, String defeito) throws SQLException {
        String sql = "INSERT INTO ordem_servico (equipamento_id, defeito_relatado) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, equipamentoId);
            ps.setString(2, defeito);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Se bloquear = true usa SELECT ... FOR UPDATE (dentro de uma transação). */
    public Optional<OrdemServico> buscarPorId(Connection con, int id, boolean bloquear) throws SQLException {
        String sql = "SELECT id, equipamento_id, defeito_relatado, diagnostico, status, "
                + "data_abertura, data_fechamento FROM ordem_servico WHERE id = ?"
                + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new OrdemServico(
                        rs.getInt("id"),
                        rs.getInt("equipamento_id"),
                        rs.getString("defeito_relatado"),
                        rs.getString("diagnostico"),
                        StatusOS.valueOf(rs.getString("status")),
                        rs.getObject("data_abertura", LocalDateTime.class),
                        rs.getObject("data_fechamento", LocalDateTime.class)));
            }
        }
    }

    public void atualizarStatus(Connection con, int id, StatusOS novo, boolean fechar) throws SQLException {
        String sql = "UPDATE ordem_servico SET status = ?"
                + (fechar ? ", data_fechamento = CURRENT_TIMESTAMP" : "")
                + " WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, novo.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void atualizarDiagnostico(Connection con, int id, String diagnostico) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("UPDATE ordem_servico SET diagnostico = ? WHERE id = ?")) {
            ps.setString(1, diagnostico);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Lista OS com cliente e equipamento. Filtros opcionais (null = sem filtro). */
    public List<OrdemServicoResumo> listarResumo(Connection con, StatusOS status, Integer clienteId)
            throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT os.id, c.nome AS cliente, "
                        + "CONCAT_WS(' ', e.tipo, e.marca, e.modelo) AS equipamento, "
                        + "os.status, os.data_abertura "
                        + "FROM ordem_servico os "
                        + "JOIN equipamento e ON e.id = os.equipamento_id "
                        + "JOIN cliente c ON c.id = e.cliente_id WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (status != null) {
            sql.append(" AND os.status = ?");
            params.add(status.name());
        }
        if (clienteId != null) {
            sql.append(" AND c.id = ?");
            params.add(clienteId);
        }
        sql.append(" ORDER BY os.id DESC");

        List<OrdemServicoResumo> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new OrdemServicoResumo(
                            rs.getInt("id"),
                            rs.getString("cliente"),
                            rs.getString("equipamento"),
                            StatusOS.valueOf(rs.getString("status")),
                            rs.getObject("data_abertura", LocalDateTime.class)));
                }
            }
        }
        return lista;
    }
}
