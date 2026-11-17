CREATE TABLE IF NOT EXISTS CARTEIRA (
    IdCarteira INT         NOT NULL AUTO_INCREMENT,
    Titular    VARCHAR(50) NOT NULL,
    Corretora  VARCHAR(50) NOT NULL,
    PRIMARY KEY (IdCarteira)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS MOVIMENTACAO (
    IdCarteira   INT NOT NULL,
    IdMovimento  INT NOT NULL AUTO_INCREMENT,
    Data         DATE,
    TipoOperacao CHAR(1),
    Quantidade   DECIMAL(10, 3),
    PRIMARY KEY (IdMovimento),
    FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA (IdCarteira)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS COTACAO (
    Data  DATE           NOT NULL,
    Valor DECIMAL(19, 8) NOT NULL,
    PRIMARY KEY (Data),
    CHECK (Valor > 0)
) ENGINE = InnoDB;
