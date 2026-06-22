import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Proposta {
  id: string;
  cpfCnpj: string;
  nomeCompleto?: string;
  nomeSocial?: string;
  razaoSocial?: string;
  nomeFantasia?: string;
  dataNascimento?: string;
  genero?: string;
  nacionalidade?: string;
  nomeMae?: string;
  estadoCivil?: string;
  email?: string;
  telefone?: string;
  cep?: string;
  logradouro?: string;
  numero?: string;
  complemento?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
  tipoPessoa?: string;
  porte?: string;
  status: string;
  etapaAtual: string;
  scoreRisco?: number;
  nivelRisco?: string;
  criadoEm: string;
  atualizadoEm: string;
  correlationId?: string;
}

export interface HistoricoEstado {
  id: string;
  propostaId: string;
  estadoAnterior: string;
  estadoNovo: string;
  etapa: string;
  usuarioResponsavel: string;
  observacao: string;
  criadoEm: string;
}

export interface Documento {
  id: string;
  propostaId: string;
  tipo: string;
  nomeArquivo: string;
  status: string;
  motivoRejeicao?: string;
  enviadoEm: string;
}

export interface ValidacaoCompliance {
  id: string;
  propostaId: string;
  tipoValidacao: string;
  status: string;
  resultado: string;
  detalhes?: string;
  realizadoEm: string;
}

export interface AnaliseRisco {
  id: string;
  propostaId: string;
  score: number;
  nivelRisco: string;
  rendaInformada: number;
  possuiRestricao: boolean;
  pepIdentificado: boolean;
  analiseRealizadaEm: string;
}

export interface ContaCriada {
  id: string;
  propostaId: string;
  agencia: string;
  numeroConta: string;
  digito: string;
  tipoConta: string;
  status: string;
  chavePix?: string;
  ativadaEm?: string;
}

@Injectable({ providedIn: 'root' })
export class PropostaService {
  private api = '/api/propostas';

  constructor(private http: HttpClient) {}

  private headers(correlationId?: string): HttpHeaders {
    let h = new HttpHeaders({ 'Content-Type': 'application/json' });
    if (correlationId) h = h.set('X-Correlation-Id', correlationId);
    return h;
  }

  listar(): Observable<Proposta[]> {
    return this.http.get<Proposta[]>(this.api);
  }

  buscar(id: string): Observable<Proposta> {
    return this.http.get<Proposta>(`${this.api}/${id}`);
  }

  criar(cpfCnpj: string): Observable<Proposta> {
    return this.http.post<Proposta>(this.api, { cpfCnpj }, { headers: this.headers() });
  }

  atualizarDadosPessoais(id: string, dados: any): Observable<Proposta> {
    return this.http.put<Proposta>(`${this.api}/${id}/dados-pessoais`, dados, { headers: this.headers() });
  }

  cancelar(id: string, motivo: string): Observable<Proposta> {
    return this.http.post<Proposta>(`${this.api}/${id}/cancelar`, { motivo });
  }

  historico(id: string): Observable<HistoricoEstado[]> {
    return this.http.get<HistoricoEstado[]>(`${this.api}/${id}/historico`);
  }

  transicoesPermitidas(id: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/${id}/transicoes-permitidas`);
  }

  avancarStatus(id: string, novoStatus: string, observacao?: string): Observable<Proposta> {
    return this.http.post<Proposta>(`${this.api}/${id}/avancar`, { novoStatus, observacao });
  }

  listarDocumentos(propostaId: string): Observable<Documento[]> {
    return this.http.get<Documento[]>(`${this.api}/${propostaId}/documentos`);
  }

  uploadDocumento(propostaId: string, tipo: string, arquivo: File): Observable<Documento> {
    const formData = new FormData();
    formData.append('tipo', tipo);
    formData.append('arquivo', arquivo);
    return this.http.post<Documento>(`${this.api}/${propostaId}/documentos`, formData);
  }

  aprovarDocumento(propostaId: string, documentoId: string): Observable<Documento> {
    return this.http.post<Documento>(`${this.api}/${propostaId}/documentos/${documentoId}/aprovar`, {});
  }

  rejeitarDocumento(propostaId: string, documentoId: string, motivo: string): Observable<Documento> {
    return this.http.post<Documento>(`${this.api}/${propostaId}/documentos/${documentoId}/rejeitar`, { motivo });
  }

  executarCompliance(propostaId: string): Observable<any> {
    return this.http.post(`${this.api}/${propostaId}/compliance/executar`, {});
  }

  listarValidacoes(propostaId: string): Observable<ValidacaoCompliance[]> {
    return this.http.get<ValidacaoCompliance[]>(`${this.api}/${propostaId}/compliance`);
  }

  analisarRisco(propostaId: string, rendaInformada: number): Observable<AnaliseRisco> {
    return this.http.post<AnaliseRisco>(`${this.api}/${propostaId}/risco/analisar`, { rendaInformada });
  }

  solicitarConsentimento(propostaId: string): Observable<any> {
    return this.http.post(`${this.api}/${propostaId}/consentimento/solicitar`, {});
  }

  criarConta(propostaId: string, tipoConta: string): Observable<ContaCriada> {
    return this.http.post<ContaCriada>(`${this.api}/${propostaId}/conta/criar`, { tipoConta });
  }

  ativarConta(propostaId: string, contaId: string): Observable<ContaCriada> {
    return this.http.post<ContaCriada>(`${this.api}/${propostaId}/conta/${contaId}/ativar`, {});
  }
}
