CREATE TABLE propostas_onboarding (
    id VARCHAR(36) PRIMARY KEY,
    cpf_cnpj VARCHAR(20) NOT NULL,
    nome_completo VARCHAR(255),
    nome_social VARCHAR(255),
    data_nascimento DATE,
    genero VARCHAR(50),
    nacionalidade VARCHAR(100),
    nome_mae VARCHAR(255),
    estado_civil VARCHAR(50),
    email VARCHAR(255),
    telefone VARCHAR(50),
    cep VARCHAR(10),
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(255),
    bairro VARCHAR(150),
    cidade VARCHAR(150),
    estado VARCHAR(50),
    tipo_pessoa VARCHAR(2),
    razao_social VARCHAR(255),
    nome_fantasia VARCHAR(255),
    porte VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'RASCUNHO',
    etapa_atual VARCHAR(50) NOT NULL DEFAULT 'DADOS_PESSOAIS',
    score_risco INTEGER,
    nivel_risco VARCHAR(50),
    id_operacao VARCHAR(100),
    correlation_id VARCHAR(36),
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE documentos (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    nome_arquivo VARCHAR(255),
    mime_type VARCHAR(100),
    tamanho_bytes BIGINT,
    minio_object_key VARCHAR(500),
    hash_sha256 VARCHAR(64),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    motivo_rejeicao TEXT,
    enviado_em TIMESTAMP,
    aprovado_em TIMESTAMP,
    correlation_id VARCHAR(36),
    CONSTRAINT fk_documentos_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE validacoes_compliance (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    tipo_validacao VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDENTE',
    resultado VARCHAR(255),
    detalhes TEXT,
    realizado_em TIMESTAMP,
    analisado_em TIMESTAMP,
    analista_responsavel VARCHAR(255),
    correlation_id VARCHAR(36),
    CONSTRAINT fk_compliance_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE analises_risco (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    score INTEGER,
    nivel_risco VARCHAR(50),
    renda_informada DECIMAL(15,2),
    renda_confirmada DECIMAL(15,2),
    patrimonio_estimado DECIMAL(15,2),
    possui_restricao BOOLEAN DEFAULT FALSE,
    detalhes_restricao TEXT,
    pep_identificado BOOLEAN DEFAULT FALSE,
    cargo_pep VARCHAR(255),
    pais_alto_risco BOOLEAN DEFAULT FALSE,
    pais VARCHAR(100),
    analise_realizada_em TIMESTAMP,
    correlation_id VARCHAR(36),
    CONSTRAINT fk_risco_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE consentimentos_open_finance (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    consent_id VARCHAR(100) UNIQUE,
    cpf_cnpj VARCHAR(20) NOT NULL,
    status VARCHAR(50) NOT NULL,
    permissoes TEXT,
    data_criacao TIMESTAMPTZ,
    data_expiracao TIMESTAMPTZ,
    data_autorizacao TIMESTAMPTZ,
    correlation_id VARCHAR(36),
    CONSTRAINT fk_consentimento_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE contas_criadas (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    agencia VARCHAR(10),
    numero_conta VARCHAR(20),
    digito VARCHAR(5),
    tipo_conta VARCHAR(50),
    ispb VARCHAR(20),
    status VARCHAR(50) NOT NULL DEFAULT 'ATIVA',
    chave_pix VARCHAR(100),
    tipo_chave_pix VARCHAR(50),
    ativada_em TIMESTAMP,
    correlation_id VARCHAR(36),
    CONSTRAINT fk_conta_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE historico_estados_proposta (
    id VARCHAR(36) PRIMARY KEY,
    proposta_id VARCHAR(36) NOT NULL,
    estado_anterior VARCHAR(50),
    estado_novo VARCHAR(50) NOT NULL,
    etapa VARCHAR(50),
    usuario_responsavel VARCHAR(255),
    observacao TEXT,
    correlation_id VARCHAR(36),
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_historico_proposta FOREIGN KEY (proposta_id) REFERENCES propostas_onboarding(id)
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    response_body TEXT,
    response_status INTEGER NOT NULL,
    request_method VARCHAR(10),
    request_path VARCHAR(500),
    created_at TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_propostas_cpf_cnpj ON propostas_onboarding(cpf_cnpj);
CREATE INDEX idx_propostas_status ON propostas_onboarding(status);
CREATE INDEX idx_documentos_proposta ON documentos(proposta_id);
CREATE INDEX idx_compliance_proposta ON validacoes_compliance(proposta_id);
CREATE INDEX idx_risco_proposta ON analises_risco(proposta_id);
CREATE INDEX idx_consentimento_proposta ON consentimentos_open_finance(proposta_id);
CREATE INDEX idx_consentimento_consent_id ON consentimentos_open_finance(consent_id);
CREATE INDEX idx_conta_proposta ON contas_criadas(proposta_id);
CREATE INDEX idx_historico_proposta ON historico_estados_proposta(proposta_id);
CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
CREATE INDEX idx_outbox_status ON outbox_events(status);
