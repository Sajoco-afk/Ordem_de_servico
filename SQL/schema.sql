-- Sistema de Ordem de Serviço - Assistência Técnica
-- Uso: mysql -u root -p < sql/schema.sql

CREATE DATABASE IF NOT EXISTS assistencia_tecnica
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE assistencia_tecnica;

CREATE TABLE IF NOT EXISTS cliente (
  id        INT AUTO_INCREMENT PRIMARY KEY,
  nome      VARCHAR(100) NOT NULL,
  cpf       VARCHAR(14)  UNIQUE,
  telefone  VARCHAR(20),
  email     VARCHAR(100)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS equipamento (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  cliente_id  INT NOT NULL,
  tipo        VARCHAR(50) NOT NULL,
  marca       VARCHAR(50),
  modelo      VARCHAR(50),
  num_serie   VARCHAR(80),
  CONSTRAINT fk_equipamento_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS ordem_servico (
  id                INT AUTO_INCREMENT PRIMARY KEY,
  equipamento_id    INT NOT NULL,
  defeito_relatado  TEXT NOT NULL,
  diagnostico       TEXT,
  status            ENUM('ABERTA','EM_ANALISE','AGUARDANDO_APROVACAO',
                         'EM_MANUTENCAO','FINALIZADA','ENTREGUE','CANCELADA')
                    NOT NULL DEFAULT 'ABERTA',
  data_abertura     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  data_fechamento   DATETIME NULL,
  CONSTRAINT fk_os_equipamento FOREIGN KEY (equipamento_id) REFERENCES equipamento(id),
  INDEX idx_os_status (status)
) ENGINE=InnoDB;

-- O orçamento de uma OS é a soma dos seus serviços.
CREATE TABLE IF NOT EXISTS servico_os (
  id         INT AUTO_INCREMENT PRIMARY KEY,
  os_id      INT NOT NULL,
  descricao  VARCHAR(150) NOT NULL,
  valor      DECIMAL(10,2) NOT NULL CHECK (valor > 0),
  CONSTRAINT fk_servico_os FOREIGN KEY (os_id) REFERENCES ordem_servico(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagamento (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  os_id           INT NOT NULL,
  valor           DECIMAL(10,2) NOT NULL CHECK (valor > 0),
  forma           ENUM('DINHEIRO','PIX','CARTAO_CREDITO','CARTAO_DEBITO') NOT NULL,
  data_pagamento  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pagamento_os FOREIGN KEY (os_id) REFERENCES ordem_servico(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS historico_status (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  os_id            INT NOT NULL,
  status_anterior  VARCHAR(30),
  status_novo      VARCHAR(30) NOT NULL,
  data_mudanca     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  observacao       VARCHAR(255),
  CONSTRAINT fk_historico_os FOREIGN KEY (os_id) REFERENCES ordem_servico(id)
) ENGINE=InnoDB;
