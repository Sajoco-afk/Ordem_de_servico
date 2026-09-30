package com.assistencia.ui;

import com.assistencia.dao.ClienteDAO;
import com.assistencia.dao.EquipamentoDAO;
import com.assistencia.model.Cliente;
import com.assistencia.model.Equipamento;
import com.assistencia.model.FormaPagamento;
import com.assistencia.model.HistoricoStatus;
import com.assistencia.model.OrdemServico;
import com.assistencia.model.OrdemServicoResumo;
import com.assistencia.model.Pagamento;
import com.assistencia.model.Servico;
import com.assistencia.model.StatusOS;
import com.assistencia.service.OrdemServicoService;
import com.assistencia.service.RegraNegocioException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Interface de console (menus) do sistema. */
public class ConsoleUI {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner in = new Scanner(System.in);
    private final ClienteDAO clienteDao = new ClienteDAO();
    private final EquipamentoDAO equipamentoDao = new EquipamentoDAO();
    private final OrdemServicoService service = new OrdemServicoService();

    @FunctionalInterface
    private interface Acao {
        void executar() throws SQLException;
    }

    public void iniciar() {
        System.out.println("=== Assistência Técnica - Ordens de Serviço ===");
        int op;
        do {
            System.out.println("\n1) Clientes\n2) Equipamentos\n3) Ordens de serviço\n0) Sair");
            op = inteiro("Opção");
            switch (op) {
                case 1 -> menuClientes();
                case 2 -> menuEquipamentos();
                case 3 -> menuOS();
                case 0 -> System.out.println("Até logo!");
                default -> System.out.println("Opção inválida.");
            }
        } while (op != 0);
    }

    // ------------------------------------------------------------------ menus

    private void menuClientes() {
        int op;
        do {
            System.out.println("\n--- Clientes ---\n1) Cadastrar\n2) Listar\n3) Editar\n0) Voltar");
            op = inteiro("Opção");
            switch (op) {
                case 1 -> executar(this::cadastrarCliente);
                case 2 -> executar(this::listarClientes);
                case 3 -> executar(this::editarCliente);
                case 0 -> { }
                default -> System.out.println("Opção inválida.");
            }
        } while (op != 0);
    }

    private void menuEquipamentos() {
        int op;
        do {
            System.out.println("\n--- Equipamentos ---\n1) Cadastrar\n2) Listar por cliente\n0) Voltar");
            op = inteiro("Opção");
            switch (op) {
                case 1 -> executar(this::cadastrarEquipamento);
                case 2 -> executar(this::listarEquipamentos);
                case 0 -> { }
                default -> System.out.println("Opção inválida.");
            }
        } while (op != 0);
    }

    private void menuOS() {
        int op;
        do {
            System.out.println("""

                    --- Ordens de Serviço ---
                    1) Abrir OS
                    2) Listar OS
                    3) Detalhar OS (orçamento, pagamentos e histórico)
                    4) Registrar diagnóstico
                    5) Adicionar serviço ao orçamento
                    6) Alterar status
                    7) Registrar pagamento
                    8) Histórico de OS de um cliente
                    0) Voltar""");
            op = inteiro("Opção");
            switch (op) {
                case 1 -> executar(this::abrirOS);
                case 2 -> executar(this::listarOS);
                case 3 -> executar(this::detalharOS);
                case 4 -> executar(this::registrarDiagnostico);
                case 5 -> executar(this::adicionarServico);
                case 6 -> executar(this::alterarStatus);
                case 7 -> executar(this::registrarPagamento);
                case 8 -> executar(this::historicoCliente);
                case 0 -> { }
                default -> System.out.println("Opção inválida.");
            }
        } while (op != 0);
    }

    // --------------------------------------------------------------- clientes

    private void cadastrarCliente() throws SQLException {
        String nome = textoObrigatorio("Nome");
        String cpf = texto("CPF (opcional)");
        String telefone = texto("Telefone (opcional)");
        String email = texto("E-mail (opcional)");
        int id = clienteDao.inserir(new Cliente(null, nome, cpf, telefone, email));
        System.out.println("Cliente cadastrado com ID " + id + ".");
    }

    private void listarClientes() throws SQLException {
        List<Cliente> clientes = clienteDao.listar();
        if (clientes.isEmpty()) {
            System.out.println("Nenhum cliente cadastrado.");
            return;
        }
        for (Cliente c : clientes) {
            System.out.printf("#%d | %s | CPF: %s | Tel: %s | %s%n", c.id(), c.nome(),
                    nvl(c.cpf()), nvl(c.telefone()), nvl(c.email()));
        }
    }

    private void editarCliente() throws SQLException {
        Cliente atual = clienteOuFalha(inteiro("ID do cliente"));
        System.out.println("(deixe em branco para manter o valor atual)");
        String nome = texto("Nome [" + atual.nome() + "]");
        String cpf = texto("CPF [" + nvl(atual.cpf()) + "]");
        String telefone = texto("Telefone [" + nvl(atual.telefone()) + "]");
        String email = texto("E-mail [" + nvl(atual.email()) + "]");
        clienteDao.atualizar(new Cliente(atual.id(),
                nome.isBlank() ? atual.nome() : nome,
                cpf.isBlank() ? atual.cpf() : cpf,
                telefone.isBlank() ? atual.telefone() : telefone,
                email.isBlank() ? atual.email() : email));
        System.out.println("Cliente atualizado.");
    }

    // ------------------------------------------------------------ equipamentos

    private void cadastrarEquipamento() throws SQLException {
        Cliente cliente = clienteOuFalha(inteiro("ID do cliente"));
        String tipo = textoObrigatorio("Tipo (ex.: Notebook, Smartphone)");
        String marca = texto("Marca (opcional)");
        String modelo = texto("Modelo (opcional)");
        String serie = texto("Nº de série (opcional)");
        int id = equipamentoDao.inserir(new Equipamento(null, cliente.id(), tipo, marca, modelo, serie));
        System.out.println("Equipamento cadastrado com ID " + id + " para " + cliente.nome() + ".");
    }

    private void listarEquipamentos() throws SQLException {
        Cliente cliente = clienteOuFalha(inteiro("ID do cliente"));
        List<Equipamento> lista = equipamentoDao.listarPorCliente(cliente.id());
        if (lista.isEmpty()) {
            System.out.println(cliente.nome() + " não possui equipamentos cadastrados.");
            return;
        }
        for (Equipamento e : lista) {
            System.out.printf("#%d | %s | Série: %s%n", e.id(), e.descricao(), nvl(e.numSerie()));
        }
    }

    // ------------------------------------------------------------------- OS

    private void abrirOS() throws SQLException {
        int equipamentoId = inteiro("ID do equipamento");
        String defeito = textoObrigatorio("Defeito relatado");
        int id = service.abrirOS(equipamentoId, defeito);
        System.out.println("OS #" + id + " aberta.");
    }

    private void listarOS() throws SQLException {
        System.out.println("Filtrar por status? (0 = todas)");
        StatusOS[] valores = StatusOS.values();
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ") " + valores[i]);
        }
        int escolha = inteiro("Opção");
        StatusOS filtro = (escolha >= 1 && escolha <= valores.length) ? valores[escolha - 1] : null;
        imprimirResumos(service.listarOS(filtro, null));
    }

    private void historicoCliente() throws SQLException {
        Cliente cliente = clienteOuFalha(inteiro("ID do cliente"));
        System.out.println("OS de " + cliente.nome() + ":");
        imprimirResumos(service.listarOS(null, cliente.id()));
    }

    private void detalharOS() throws SQLException {
        int id = inteiro("ID da OS");
        OrdemServico os = service.buscarOS(id);
        Equipamento eq = equipamentoDao.buscarPorId(os.equipamentoId()).orElseThrow();
        Cliente cliente = clienteOuFalha(eq.clienteId());

        System.out.printf("%nOS #%d | %s | aberta em %s%n", os.id(), os.status(), formatar(os.dataAbertura()));
        if (os.dataFechamento() != null) {
            System.out.println("Encerrada em: " + formatar(os.dataFechamento()));
        }
        System.out.println("Cliente: " + cliente.nome() + " | Equipamento: " + eq.descricao());
        System.out.println("Defeito relatado: " + os.defeitoRelatado());
        System.out.println("Diagnóstico: " + (os.diagnostico() == null ? "(pendente)" : os.diagnostico()));

        System.out.println("\nOrçamento:");
        List<Servico> servicos = service.listarServicos(id);
        if (servicos.isEmpty()) {
            System.out.println("  (sem serviços)");
        }
        for (Servico s : servicos) {
            System.out.printf("  - %s: %s%n", s.descricao(), MOEDA.format(s.valor()));
        }
        BigDecimal total = service.totalOrcamento(id);
        BigDecimal pago = service.totalPago(id);
        System.out.println("  Total: " + MOEDA.format(total));

        System.out.println("\nPagamentos:");
        List<Pagamento> pagamentos = service.listarPagamentos(id);
        if (pagamentos.isEmpty()) {
            System.out.println("  (nenhum)");
        }
        for (Pagamento p : pagamentos) {
            System.out.printf("  - %s | %s | %s%n", formatar(p.dataPagamento()), p.forma(), MOEDA.format(p.valor()));
        }
        System.out.println("  Pago: " + MOEDA.format(pago) + " | Saldo: " + MOEDA.format(total.subtract(pago)));

        System.out.println("\nHistórico de status:");
        for (HistoricoStatus h : service.listarHistorico(id)) {
            System.out.printf("  %s | %s -> %s%s%n", formatar(h.dataMudanca()),
                    h.statusAnterior() == null ? "-" : h.statusAnterior(), h.statusNovo(),
                    h.observacao() == null ? "" : " (" + h.observacao() + ")");
        }
    }

    private void registrarDiagnostico() throws SQLException {
        int id = inteiro("ID da OS");
        service.registrarDiagnostico(id, textoObrigatorio("Diagnóstico"));
        System.out.println("Diagnóstico registrado.");
    }

    private void adicionarServico() throws SQLException {
        int id = inteiro("ID da OS");
        String descricao = textoObrigatorio("Descrição do serviço");
        BigDecimal valor = decimal("Valor (R$)");
        service.adicionarServico(id, descricao, valor);
        System.out.println("Serviço adicionado. Total do orçamento: " + MOEDA.format(service.totalOrcamento(id)));
    }

    private void alterarStatus() throws SQLException {
        int id = inteiro("ID da OS");
        OrdemServico os = service.buscarOS(id);
        List<StatusOS> opcoes = os.status().proximosPermitidos();
        if (opcoes.isEmpty()) {
            System.out.println("A OS está em " + os.status() + " (estado final).");
            return;
        }
        System.out.println("Status atual: " + os.status());
        StatusOS novo = escolher("Novo status", opcoes);

        String observacao = texto("Observação (opcional)");
        if (os.status() == StatusOS.AGUARDANDO_APROVACAO && novo == StatusOS.EM_MANUTENCAO) {
            System.out.println("Orçamento: " + MOEDA.format(service.totalOrcamento(id)));
            if (!confirmar("O cliente aprovou o orçamento?")) {
                System.out.println("Operação cancelada.");
                return;
            }
            if (observacao.isBlank()) {
                observacao = "Orçamento aprovado pelo cliente";
            }
        }
        service.alterarStatus(id, novo, observacao);
        System.out.println("Status alterado para " + novo + ".");
    }

    private void registrarPagamento() throws SQLException {
        int id = inteiro("ID da OS");
        BigDecimal saldo = service.totalOrcamento(id).subtract(service.totalPago(id));
        System.out.println("Saldo devedor: " + MOEDA.format(saldo));
        BigDecimal valor = decimal("Valor pago (R$)");
        FormaPagamento forma = escolher("Forma de pagamento", List.of(FormaPagamento.values()));
        service.registrarPagamento(id, valor, forma);
        System.out.println("Pagamento registrado.");
    }

    // ------------------------------------------------------------ auxiliares

    private void imprimirResumos(List<OrdemServicoResumo> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma OS encontrada.");
            return;
        }
        for (OrdemServicoResumo r : lista) {
            System.out.printf("OS #%d | %-22s | %s | %s | %s%n", r.id(), r.status(), r.cliente(),
                    r.equipamento(), formatar(r.dataAbertura()));
        }
    }

    private Cliente clienteOuFalha(int id) throws SQLException {
        return clienteDao.buscarPorId(id)
                .orElseThrow(() -> new RegraNegocioException("Cliente #" + id + " não encontrado."));
    }

    private void executar(Acao acao) {
        try {
            acao.executar();
        } catch (RegraNegocioException e) {
            System.out.println("Atenção: " + e.getMessage());
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Erro: violação de integridade (CPF duplicado ou registro inexistente).");
        } catch (SQLException e) {
            System.out.println("Erro de banco de dados: " + e.getMessage());
        }
    }

    private <T> T escolher(String titulo, List<T> opcoes) {
        for (int i = 0; i < opcoes.size(); i++) {
            System.out.println((i + 1) + ") " + opcoes.get(i));
        }
        while (true) {
            int n = inteiro(titulo);
            if (n >= 1 && n <= opcoes.size()) {
                return opcoes.get(n - 1);
            }
            System.out.println("Opção inválida.");
        }
    }

    private boolean confirmar(String pergunta) {
        return texto(pergunta + " (s/n)").equalsIgnoreCase("s");
    }

    private String texto(String rotulo) {
        System.out.print(rotulo + ": ");
        return in.nextLine().trim();
    }

    private String textoObrigatorio(String rotulo) {
        while (true) {
            String t = texto(rotulo);
            if (!t.isBlank()) {
                return t;
            }
            System.out.println("Campo obrigatório.");
        }
    }

    private int inteiro(String rotulo) {
        while (true) {
            try {
                return Integer.parseInt(texto(rotulo));
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro.");
            }
        }
    }

    private BigDecimal decimal(String rotulo) {
        while (true) {
            try {
                return new BigDecimal(texto(rotulo).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor numérico (ex.: 150,00).");
            }
        }
    }

    private static String nvl(String s) {
        return s == null ? "-" : s;
    }

    private static String formatar(LocalDateTime d) {
        return d == null ? "-" : d.format(DATA);
    }
}
