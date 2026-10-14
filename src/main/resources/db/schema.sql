CREATE TABLE IF NOT EXISTS carteira (
    id           INT          NOT NULL AUTO_INCREMENT,
    nome_titular VARCHAR(100) NOT NULL,
    corretora    VARCHAR(100) NOT NULL,
    CONSTRAINT pk_carteira PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS movimentacao (
    id          INT            NOT NULL AUTO_INCREMENT,
    id_carteira INT            NOT NULL,
    data        DATE           NOT NULL,
    tipo        CHAR(1)        NOT NULL,
    -- FTCoins em compras (C) e vendas (V); reais em créditos (D) e saques (S)
    quantidade  DECIMAL(19, 8) NOT NULL,
    CONSTRAINT pk_movimentacao PRIMARY KEY (id),
    CONSTRAINT fk_movimentacao_carteira FOREIGN KEY (id_carteira) REFERENCES carteira (id),
    CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('C', 'V', 'D', 'S')),
    CONSTRAINT ck_movimentacao_quantidade CHECK (quantidade > 0),
    INDEX idx_movimentacao_carteira_data (id_carteira, data)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Bancos criados antes do crédito em reais só aceitavam C e V.
ALTER TABLE movimentacao DROP CONSTRAINT IF EXISTS ck_movimentacao_tipo;
ALTER TABLE movimentacao ADD CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('C', 'V', 'D', 'S'));

CREATE TABLE IF NOT EXISTS cotacao (
    data  DATE           NOT NULL,
    valor DECIMAL(19, 8) NOT NULL,
    CONSTRAINT pk_cotacao PRIMARY KEY (data),
    CONSTRAINT ck_cotacao_valor CHECK (valor > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
