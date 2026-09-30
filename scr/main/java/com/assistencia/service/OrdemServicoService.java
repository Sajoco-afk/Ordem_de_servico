package com.assistencia.service;

import com.assistencia.dao.EquipamentoDAO;
import com.assistencia.dao.HistoricoDAO;
import com.assistencia.dao.OrdemServicoDAO;
import com.assistencia.dao.PagamentoDAO;
import com.assistencia.dao.ServicoDAO;
import com.assistencia.model.FormaPagamento;
import com.assistencia.model.HistoricoStatus;
import com.assistencia.model.OrdemServico;
import com.assistencia.model.OrdemServicoResumo;
import com.assistencia.model.Pagamento;
import com.assistencia.model.Servico;
import com.assistencia.model.StatusOS;
import com.assistencia.util.ConexaoFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras de negócio da Ordem de Serviço. Toda operação que altera dados roda
 * em uma única transação, com a linha da OS bloqueada (SELECT ... FOR UPDATE).
 */
public class OrdemServicoService {

    private final EquipamentoDAO equipamentoDao = new EquipamentoDAO();
    private final OrdemServicoDAO osDao = new OrdemServicoDAO();
    private final ServicoDAO servicoDao = new ServicoDAO();
    private final PagamentoDAO pagamentoDao = new PagamentoDAO();
    private final HistoricoDAO historicoDao = new HistoricoDAO();

    @FunctionalInterface
    private interface Transacao {
        void executar(Connection con) throws SQLException;
    }

    private void emTransacao(Transacao t) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                t.executar(con);
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // ---------------------------------------------------------------- comandos

    public int abrirOS(int equipamentoId, String defeito) throws SQLException {
        if (defeito == null || defeito.isBlank()) {
            throw new RegraNegocioException("Informe o defeito relatado.");
        }
        if (equipamentoDao.buscarPorId(equipamentoId).isEmpty()) {
            throw new RegraNegocioException("Equipamento não encontrado.");
        }
        int[] idGerado = new int[1];
        emTransacao(con -> {
            idGerado[0] = osDao.inserir(con, equipamentoId, defeito.trim());
            historicoDao.inserir(con, idGerado[0], null, StatusOS.ABERTA, "OS aberta");
        });
        return idGerado[0];
    }

    public void registrarDiagnostico(int osId, String diagnostico) throws SQLException {
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new RegraNegocioException("O diagnóstico não pode ser vazio.");
        }
        emTransacao(con -> {
            OrdemServico os = buscarOuFalhar(con, osId, true);
            exigirStatus(os, StatusOS.EM_ANALISE, "registrar o diagnóstico");
            osDao.atualizarDiagnostico(con, osId, diagnostico.trim());
        });
    }

    public void adicionarServico(int osId, String descricao, BigDecimal valor) throws SQLException {
        if (descricao == null || descricao.isBlank()) {
            throw new RegraNegocioException("Informe a descrição do serviço.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("O valor do serviço deve ser maior que zero.");
        }
        emTransacao(con -> {
            OrdemServico os = buscarOuFalhar(con, osId, true);
            exigirStatus(os, StatusOS.EM_ANALISE, "adicionar serviços ao orçamento");
            servicoDao.inserir(con, osId, descricao.trim(), valor);
        });
    }

    public void alterarStatus(int osId, StatusOS novo, String observacao) throws SQLException {
        emTransacao(con -> {
            OrdemServico os = buscarOuFalhar(con, osId, true);
            StatusOS atual = os.status();

            if (!atual.podeIrPara(novo)) {
                throw new RegraNegocioException("Transição inválida: " + atual + " -> " + novo + ".");
            }

            if (atual == StatusOS.EM_ANALISE && novo == StatusOS.AGUARDANDO_APROVACAO) {
                if (os.diagnostico() == null || os.diagnostico().isBlank()) {
                    throw new RegraNegocioException("Registre o diagnóstico antes de enviar o orçamento.");
                }
                if (servicoDao.somaPorOS(con, osId).signum() == 0) {
                    throw new RegraNegocioException("Adicione ao menos um serviço ao orçamento.");
                }
            }

            if (novo == StatusOS.ENTREGUE) {
                BigDecimal total = servicoDao.somaPorOS(con, osId);
                BigDecimal pago = pagamentoDao.somaPorOS(con, osId);
                if (pago.compareTo(total) < 0) {
                    throw new RegraNegocioException("A OS só pode ser entregue após o pagamento total (falta "
                            + total.subtract(pago) + ").");
                }
            }

            if (novo == StatusOS.CANCELADA && pagamentoDao.somaPorOS(con, osId).signum() > 0) {
                throw new RegraNegocioException("Há pagamentos registrados; não é possível cancelar a OS.");
            }

            osDao.atualizarStatus(con, osId, novo, novo.isFinal());
            historicoDao.inserir(con, osId, atual, novo, observacao);
        });
    }

    public void registrarPagamento(int osId, BigDecimal valor, FormaPagamento forma) throws SQLException {
        if (valor == null || valor.signum() <= 0) {
            throw new RegraNegocioException("O valor do pagamento deve ser maior que zero.");
        }
        emTransacao(con -> {
            OrdemServico os = buscarOuFalhar(con, osId, true);
            exigirStatus(os, StatusOS.FINALIZADA, "registrar pagamento");
            BigDecimal restante = servicoDao.somaPorOS(con, osId).subtract(pagamentoDao.somaPorOS(con, osId));
            if (valor.compareTo(restante) > 0) {
                throw new RegraNegocioException("Valor maior que o saldo devedor (" + restante + ").");
            }
            pagamentoDao.inserir(con, osId, valor, forma);
        });
    }

    // ---------------------------------------------------------------- consultas

    public OrdemServico buscarOS(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return buscarOuFalhar(con, osId, false);
        }
    }

    public List<OrdemServicoResumo> listarOS(StatusOS status, Integer clienteId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return osDao.listarResumo(con, status, clienteId);
        }
    }

    public List<Servico> listarServicos(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return servicoDao.listarPorOS(con, osId);
        }
    }

    public List<Pagamento> listarPagamentos(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return pagamentoDao.listarPorOS(con, osId);
        }
    }

    public List<HistoricoStatus> listarHistorico(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return historicoDao.listarPorOS(con, osId);
        }
    }

    public BigDecimal totalOrcamento(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return servicoDao.somaPorOS(con, osId);
        }
    }

    public BigDecimal totalPago(int osId) throws SQLException {
        try (Connection con = ConexaoFactory.getConnection()) {
            return pagamentoDao.somaPorOS(con, osId);
        }
    }

    // ---------------------------------------------------------------- auxiliares

    private OrdemServico buscarOuFalhar(Connection con, int osId, boolean bloquear) throws SQLException {
        return osDao.buscarPorId(con, osId, bloquear)
                .orElseThrow(() -> new RegraNegocioException("OS #" + osId + " não encontrada."));
    }

    private void exigirStatus(OrdemServico os, StatusOS esperado, String acao) {
        if (os.status() != esperado) {
            throw new RegraNegocioException("Só é possível " + acao + " com a OS em " + esperado
                    + " (status atual: " + os.status() + ").");
        }
    }
}
