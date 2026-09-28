-- Dados de exemplo (opcional). Execute depois do schema.sql:
-- mysql -u root -p assistencia_tecnica < sql/dados-exemplo.sql

INSERT INTO cliente (nome, cpf, telefone, email) VALUES
  ('Maria Souza',  '111.111.111-11', '(41) 99999-1111', 'maria@email.com'),
  ('João Pereira', '222.222.222-22', '(41) 98888-2222', 'joao@email.com');

INSERT INTO equipamento (cliente_id, tipo, marca, modelo, num_serie) VALUES
  (1, 'Notebook',   'Dell',    'Inspiron 15', 'SN-DELL-001'),
  (1, 'Smartphone', 'Samsung', 'Galaxy A54',  'SN-SAM-002'),
  (2, 'Impressora', 'HP',      'DeskJet 2774', 'SN-HP-003');

INSERT INTO ordem_servico (equipamento_id, defeito_relatado) VALUES
  (1, 'Não liga após queda de energia'),
  (3, 'Papel enroscando na entrada');

INSERT INTO historico_status (os_id, status_anterior, status_novo, observacao) VALUES
  (1, NULL, 'ABERTA', 'OS aberta'),
  (2, NULL, 'ABERTA', 'OS aberta');
