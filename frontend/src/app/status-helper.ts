export function statusClass(status: string): string {
  const map: Record<string, string> = {
    'RASCUNHO': 'badge-secondary',
    'DADOS_PESSOAIS_ENVIADOS': 'badge-info',
    'DOCUMENTOS_PENDENTES': 'badge-warning',
    'DOCUMENTOS_ENVIADOS': 'badge-info',
    'DOCUMENTOS_APROVADOS': 'badge-success',
    'DOCUMENTOS_REJEITADOS': 'badge-danger',
    'EM_ANALISE_COMPLIANCE': 'badge-info',
    'ANALISE_COMPLIANCE_APROVADA': 'badge-success',
    'ANALISE_COMPLIANCE_REPROVADA': 'badge-danger',
    'EM_ANALISE_RISCO': 'badge-info',
    'ANALISE_RISCO_APROVADA': 'badge-success',
    'ANALISE_RISCO_REPROVADA': 'badge-danger',
    'AGUARDANDO_CONSENTIMENTO_OF': 'badge-warning',
    'CONSENTIMENTO_OF_AUTORIZADO': 'badge-success',
    'CONSENTIMENTO_OF_REJEITADO': 'badge-danger',
    'EM_ANALISE_INTEGRACAO_NUCLEO': 'badge-info',
    'INTEGRACAO_NUCLEO_APROVADA': 'badge-success',
    'INTEGRACAO_NUCLEO_REPROVADA': 'badge-danger',
    'AGUARDANDO_CRIACAO_CONTA': 'badge-warning',
    'CONTA_CRIADA': 'badge-success',
    'CONTA_ATIVADA': 'badge-success',
    'FINALIZADO': 'badge-success',
    'CANCELADO': 'badge-danger'
  };
  return map[status] || 'badge-secondary';
}

export function statusLabel(status: string): string {
  const map: Record<string, string> = {
    'RASCUNHO': 'Rascunho',
    'DADOS_PESSOAIS_ENVIADOS': 'Dados Pessoais',
    'DOCUMENTOS_PENDENTES': 'Aguardando Docs',
    'DOCUMENTOS_ENVIADOS': 'Docs Enviados',
    'DOCUMENTOS_APROVADOS': 'Docs Aprovados',
    'DOCUMENTOS_REJEITADOS': 'Docs Rejeitados',
    'EM_ANALISE_COMPLIANCE': 'Análise Compliance',
    'ANALISE_COMPLIANCE_APROVADA': 'Compliance OK',
    'ANALISE_COMPLIANCE_REPROVADA': 'Compliance Reprovado',
    'EM_ANALISE_RISCO': 'Análise Risco',
    'ANALISE_RISCO_APROVADA': 'Risco OK',
    'ANALISE_RISCO_REPROVADA': 'Risco Reprovado',
    'AGUARDANDO_CONSENTIMENTO_OF': 'Aguardando Consentimento',
    'CONSENTIMENTO_OF_AUTORIZADO': 'Consentimento OK',
    'CONSENTIMENTO_OF_REJEITADO': 'Consentimento Rejeitado',
    'EM_ANALISE_INTEGRACAO_NUCLEO': 'Validação Núcleo',
    'INTEGRACAO_NUCLEO_APROVADA': 'Núcleo OK',
    'INTEGRACAO_NUCLEO_REPROVADA': 'Núcleo Reprovado',
    'AGUARDANDO_CRIACAO_CONTA': 'Criando Conta',
    'CONTA_CRIADA': 'Conta Criada',
    'CONTA_ATIVADA': 'Conta Ativada',
    'FINALIZADO': 'Finalizado',
    'CANCELADO': 'Cancelado'
  };
  return map[status] || status;
}

export function etapaProxima(etapaAtual: string): number {
  const etapas = ['DADOS_PESSOAIS', 'DOCUMENTOS', 'COMPLIANCE', 'ANALISE_RISCO',
    'CONSENTIMENTO_OPEN_FINANCE', 'INTEGRACAO_NUCLEO', 'CRIACAO_CONTA', 'ATIVACAO_CONTA', 'FINALIZADO'];
  const idx = etapas.indexOf(etapaAtual);
  return idx >= 0 ? Math.round((idx / (etapas.length - 1)) * 100) : 0;
}
