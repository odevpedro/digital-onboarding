import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PropostaService, Proposta, HistoricoEstado, Documento, ValidacaoCompliance, AnaliseRisco, ContaCriada } from '../proposta.service';
import { statusClass, statusLabel } from '../status-helper';

@Component({
  selector: 'app-proposta-detalhe',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="back-link">
      <a routerLink="/">&larr; Voltar</a>
    </div>

    <div class="alerts" *ngIf="erro">
      <div class="alert alert-danger">
        {{ erro }}
        <button class="alert-close" (click)="erro = undefined">&times;</button>
      </div>
    </div>

    <div class="proposta-header card" *ngIf="proposta">
      <div class="header-info">
        <h2>{{ proposta.nomeCompleto || 'Proposta' }}</h2>
        <p class="cpf">{{ proposta.cpfCnpj }}</p>
        <span class="badge {{ statusClass(proposta.status) }}">{{ statusLabel(proposta.status) }}</span>
        <span class="etapa" *ngIf="proposta.nivelRisco">Risco: {{ proposta.nivelRisco }}</span>
        <span class="etapa" *ngIf="proposta.scoreRisco != null">Score: {{ proposta.scoreRisco }}</span>
      </div>
      <div class="header-actions">
        <button class="btn btn-danger btn-sm" (click)="cancelar()" *ngIf="proposta.status !== 'FINALIZADO' && proposta.status !== 'CANCELADO'">
          Cancelar Proposta
        </button>
      </div>
    </div>

    <!-- Tabs -->
    <div class="tabs" *ngIf="proposta">
      <button *ngFor="let tab of tabs" class="tab" [class.active]="tabAtiva === tab.id" (click)="tabAtiva = tab.id">
        {{ tab.label }}
      </button>
    </div>

    <!-- Tab: Dados Pessoais -->
    <div class="card" *ngIf="tabAtiva === 'dados' && proposta">
      <h3>Dados Pessoais</h3>
      <form #dadosForm="ngForm">
      <div class="form-grid">
        <div class="form-group">
          <label>Nome Completo *</label>
          <input class="form-control" name="nomeCompleto" [(ngModel)]="proposta.nomeCompleto" placeholder="Nome completo" required #nomeCompleto="ngModel">
          <span class="field-error" *ngIf="nomeCompleto.invalid && (nomeCompleto.dirty || nomeCompleto.touched)">Campo obrigatorio</span>
        </div>
        <div class="form-group">
          <label>Email *</label>
          <input class="form-control" name="email" [(ngModel)]="proposta.email" placeholder="email@exemplo.com" required email #email="ngModel">
          <span class="field-error" *ngIf="email.invalid && (email.dirty || email.touched)">
            <ng-container *ngIf="email.errors?.['required']">Campo obrigatorio</ng-container>
            <ng-container *ngIf="email.errors?.['email']">Email invalido</ng-container>
          </span>
        </div>
        <div class="form-group">
          <label>Telefone</label>
          <input class="form-control" name="telefone" [(ngModel)]="proposta.telefone" placeholder="(11) 99999-9999">
        </div>
        <div class="form-group">
          <label>CEP *</label>
          <input class="form-control" name="cep" [(ngModel)]="proposta.cep" placeholder="00000-000" required pattern="\d{5}-?\d{3}" #cep="ngModel">
          <span class="field-error" *ngIf="cep.invalid && (cep.dirty || cep.touched)">
            <ng-container *ngIf="cep.errors?.['required']">Campo obrigatorio</ng-container>
            <ng-container *ngIf="cep.errors?.['pattern']">CEP deve ter 8 digitos</ng-container>
          </span>
        </div>
        <div class="form-group">
          <label>Logradouro *</label>
          <input class="form-control" name="logradouro" [(ngModel)]="proposta.logradouro" placeholder="Rua, Av..." required #logradouro="ngModel">
          <span class="field-error" *ngIf="logradouro.invalid && (logradouro.dirty || logradouro.touched)">Campo obrigatorio</span>
        </div>
        <div class="form-group">
          <label>Cidade / Estado *</label>
          <div class="input-row">
            <input class="form-control" name="cidade" [(ngModel)]="proposta.cidade" placeholder="Cidade" required #cidade="ngModel" style="flex:1">
            <input class="form-control" name="estado" [(ngModel)]="proposta.estado" placeholder="UF" required minlength="2" maxlength="2" #estado="ngModel" style="width:80px">
          </div>
          <span class="field-error" *ngIf="cidade.invalid && (cidade.dirty || cidade.touched)">Cidade obrigatoria</span>
          <span class="field-error" *ngIf="estado.invalid && (estado.dirty || estado.touched)">
            <ng-container *ngIf="estado.errors?.['required']">UF obrigatoria</ng-container>
            <ng-container *ngIf="estado.errors?.['minlength']">UF deve ter 2 letras</ng-container>
          </span>
        </div>
        <div class="form-group">
          <label>Tipo Pessoa *</label>
          <select class="form-control" name="tipoPessoa" [(ngModel)]="proposta.tipoPessoa" required #tipoPessoa="ngModel">
            <option value="">Selecione</option>
            <option value="PF">Pessoa Fisica</option>
            <option value="PJ">Pessoa Juridica</option>
          </select>
          <span class="field-error" *ngIf="tipoPessoa.invalid && (tipoPessoa.dirty || tipoPessoa.touched)">Selecione PF ou PJ</span>
        </div>
        <div class="form-group" *ngIf="proposta.tipoPessoa === 'PJ'">
          <label>Razao Social *</label>
          <input class="form-control" name="razaoSocial" [(ngModel)]="proposta.razaoSocial" placeholder="Razao Social" required #razaoSocial="ngModel">
          <span class="field-error" *ngIf="razaoSocial.invalid && (razaoSocial.dirty || razaoSocial.touched)">Campo obrigatorio para PJ</span>
        </div>
      </div>
      <button class="btn btn-primary" (click)="salvarDados()" [disabled]="dadosForm.invalid">Salvar Dados Pessoais</button>
      </form>
    </div>

    <!-- Tab: Documentos -->
    <div class="card" *ngIf="tabAtiva === 'documentos'">
      <h3>Documentos</h3>
      <div class="upload-area" *ngIf="proposta">
        <select class="form-control" [(ngModel)]="tipoDocumento" style="width:200px">
          <option value="RG">RG</option>
          <option value="CNH">CNH</option>
          <option value="CPF">CPF</option>
          <option value="COMPROVANTE_RESIDENCIA">Comprovante Residência</option>
          <option value="CONTRATO_SOCIAL">Contrato Social (PJ)</option>
        </select>
        <input type="file" (change)="onFileSelected($event)" accept="image/*,.pdf">
        <button class="btn btn-primary" (click)="enviarDocumento()" [disabled]="!arquivoSelecionado">Enviar</button>
      </div>
      <table>
        <thead>
          <tr><th>Tipo</th><th>Arquivo</th><th>Status</th><th>Data</th><th>Ações</th></tr>
        </thead>
        <tbody>
          <tr *ngFor="let doc of documentos">
            <td>{{ doc.tipo }}</td>
            <td>{{ doc.nomeArquivo }}</td>
            <td><span class="badge" [class.badge-success]="doc.status==='APROVADO'" [class.badge-danger]="doc.status==='REJEITADO'" [class.badge-warning]="doc.status==='PENDENTE'">{{ doc.status }}</span></td>
            <td>{{ doc.enviadoEm | date:'short' }}</td>
            <td>
              <button class="btn btn-success btn-sm" (click)="aprovarDoc(doc)" *ngIf="doc.status==='PENDENTE'">Aprovar</button>
              <button class="btn btn-danger btn-sm" (click)="rejeitarDoc(doc)" *ngIf="doc.status==='PENDENTE'">Rejeitar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Tab: Compliance -->
    <div class="card" *ngIf="tabAtiva === 'compliance'">
      <h3>Validações de Compliance</h3>
      <button class="btn btn-primary" (click)="executarCompliance()" [disabled]="executandoCompliance">Executar Validações</button>
      <table>
        <thead><tr><th>Tipo</th><th>Status</th><th>Resultado</th><th>Detalhes</th></tr></thead>
        <tbody>
          <tr *ngFor="let v of validacoes">
            <td>{{ v.tipoValidacao }}</td>
            <td><span class="badge" [class.badge-success]="v.status==='APROVADO'" [class.badge-danger]="v.status==='REPROVADO'">{{ v.status }}</span></td>
            <td>{{ v.resultado }}</td>
            <td>{{ v.detalhes }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Tab: Risco -->
    <div class="card" *ngIf="tabAtiva === 'risco'">
      <h3>Análise de Risco</h3>
      <div class="form-group">
        <label>Renda Informada (R$) *</label>
        <input class="form-control" type="number" name="rendaInformada" [(ngModel)]="rendaInformada" placeholder="5000.00" required min="0.01" [ngModelOptions]="{standalone: true}" #rendaInput="ngModel">
        <span class="field-error" *ngIf="rendaInput.invalid && (rendaInput.dirty || rendaInput.touched)">
          <ng-container *ngIf="rendaInput.errors?.['required']">Campo obrigatorio</ng-container>
          <ng-container *ngIf="rendaInput.errors?.['min']">Deve ser maior que zero</ng-container>
        </span>
      </div>
      <button class="btn btn-primary" (click)="analisarRisco()" [disabled]="analisandoRisco || rendaInput.invalid">Analisar Risco</button>
      <div *ngIf="analiseRisco" class="risco-resultado">
        <p><strong>Score:</strong> {{ analiseRisco.score }}</p>
        <p><strong>Nível:</strong> <span class="badge" [class.badge-success]="analiseRisco.nivelRisco==='BAIXO'" [class.badge-warning]="analiseRisco.nivelRisco==='MEDIO'" [class.badge-danger]="analiseRisco.nivelRisco==='ALTO'">{{ analiseRisco.nivelRisco }}</span></p>
        <p><strong>Restrição:</strong> {{ analiseRisco.possuiRestricao ? 'Sim' : 'Não' }}</p>
      </div>
    </div>

    <!-- Tab: Consentimento -->
    <div class="card" *ngIf="tabAtiva === 'consentimento'">
      <h3>Consentimento Open Finance</h3>
      <p class="help-text">Solicite o consentimento do cliente para compartilhamento de dados via Open Finance.</p>
      <button class="btn btn-primary" (click)="solicitarConsentimento()">Solicitar Consentimento</button>
    </div>

    <!-- Tab: Conta -->
    <div class="card" *ngIf="tabAtiva === 'conta'">
      <h3>Criação de Conta</h3>
      <div class="form-group">
        <label>Tipo de Conta</label>
        <select class="form-control" [(ngModel)]="tipoConta" style="width:200px">
          <option value="CORRENTE">Conta Corrente</option>
          <option value="POUPANCA">Conta Poupança</option>
          <option value="SALARIO">Conta Salário</option>
        </select>
      </div>
      <button class="btn btn-primary" (click)="criarConta()">Criar Conta</button>
      <div *ngIf="contaCriada" class="conta-info">
        <div class="conta-card">
          <p><strong>Agência:</strong> {{ contaCriada.agencia }}-{{ contaCriada.digito }}</p>
          <p><strong>Conta:</strong> {{ contaCriada.numeroConta }}-{{ contaCriada.digito }}</p>
          <p><strong>Chave Pix:</strong> {{ contaCriada.chavePix }}</p>
          <p><strong>Status:</strong> {{ contaCriada.status }}</p>
          <button class="btn btn-success" (click)="ativarConta()" *ngIf="contaCriada.status === 'ATIVA' || contaCriada.status === 'ATIVADA'">Ativar Conta</button>
        </div>
      </div>
    </div>

    <!-- Tab: Histórico -->
    <div class="card" *ngIf="tabAtiva === 'historico'">
      <h3>Histórico de Estados</h3>
      <table>
        <thead><tr><th>Data</th><th>De</th><th>Para</th><th>Etapa</th><th>Observação</th></tr></thead>
        <tbody>
          <tr *ngFor="let h of historico">
            <td>{{ h.criadoEm | date:'short' }}</td>
            <td>{{ h.estadoAnterior || '-' }}</td>
            <td><span class="badge {{ statusClass(h.estadoNovo) }}">{{ statusLabel(h.estadoNovo) }}</span></td>
            <td>{{ h.etapa }}</td>
            <td>{{ h.observacao }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .alerts { margin-bottom: 16px; }
    .alert { padding: 12px 16px; border-radius: 6px; font-size: 14px; display: flex; justify-content: space-between; align-items: center; }
    .alert-danger { background: #FEE; color: #C00; border: 1px solid #FCC; }
    .alert-close { background: none; border: none; font-size: 20px; cursor: pointer; color: inherit; padding: 0 0 0 12px; }
    .back-link { margin-bottom: 16px; }
    .back-link a { color: var(--color-primary); text-decoration: none; font-size: 14px; }
    .proposta-header { display: flex; justify-content: space-between; align-items: flex-start; }
    .header-info h2 { margin-bottom: 8px; }
    .cpf { color: var(--color-text-light); margin-bottom: 8px; }
    .etapa { margin-left: 12px; font-size: 13px; color: var(--color-text-light); }
    .tabs { display: flex; gap: 4px; margin-bottom: 16px; overflow-x: auto; }
    .tab { padding: 10px 20px; background: white; border: 1px solid var(--color-border); border-radius: 6px 6px 0 0; cursor: pointer; font-size: 13px; font-weight: 500; color: var(--color-text-light); transition: all 0.2s; }
    .tab.active { background: var(--color-primary); color: white; border-color: var(--color-primary); }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px; }
    .input-row { display: flex; gap: 8px; align-items: center; }
    .upload-area { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; padding: 16px; background: #FAFAFA; border-radius: 8px; }
    .help-text { color: var(--color-text-light); margin-bottom: 16px; }
    .risco-resultado, .conta-info { margin-top: 16px; }
    .conta-card { background: #F0F8FF; padding: 16px; border-radius: 8px; border: 1px solid #CCE5FF; }
    .conta-card p { margin-bottom: 4px; }
    .field-error { color: #C00; font-size: 12px; margin-top: 4px; display: block; }
    input.ng-invalid.ng-dirty, select.ng-invalid.ng-dirty { border-color: #C00; }
    input.ng-valid.ng-dirty, select.ng-valid.ng-dirty { border-color: #090; }
  `]
})
export class PropostaDetalheComponent implements OnInit {
  proposta?: Proposta;
  historico: HistoricoEstado[] = [];
  documentos: Documento[] = [];
  validacoes: ValidacaoCompliance[] = [];
  analiseRisco?: AnaliseRisco;
  contaCriada?: ContaCriada;

  tabAtiva = 'dados';
  tipoDocumento = 'RG';
  arquivoSelecionado?: File;
  rendaInformada = 5000;
  tipoConta = 'CORRENTE';
  executandoCompliance = false;
  analisandoRisco = false;
  erro?: string;

  tabs = [
    { id: 'dados', label: 'Dados Pessoais' },
    { id: 'documentos', label: 'Documentos' },
    { id: 'compliance', label: 'Compliance' },
    { id: 'risco', label: 'Risco' },
    { id: 'consentimento', label: 'Open Finance' },
    { id: 'conta', label: 'Conta' },
    { id: 'historico', label: 'Histórico' }
  ];

  constructor(private route: ActivatedRoute, private service: PropostaService) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.carregarProposta(id);
  }

  carregarProposta(id: string) {
    this.erro = undefined;
    this.service.buscar(id).subscribe({
      next: p => {
        this.proposta = p;
        this.tabAtiva = this.mapearTab(p.etapaAtual);
      },
      error: e => this.erro = 'Erro ao carregar proposta: ' + (e.error?.message || e.message)
    });
    this.service.historico(id).subscribe({
      next: h => this.historico = h,
      error: () => {}
    });
    this.service.listarDocumentos(id).subscribe({
      next: d => this.documentos = d,
      error: () => {}
    });
  }

  mapearTab(etapa: string): string {
    const map: Record<string, string> = {
      'DADOS_PESSOAIS': 'dados', 'DOCUMENTOS': 'documentos', 'COMPLIANCE': 'compliance',
      'ANALISE_RISCO': 'risco', 'CONSENTIMENTO_OPEN_FINANCE': 'consentimento',
      'INTEGRACAO_NUCLEO': 'conta', 'CRIACAO_CONTA': 'conta', 'ATIVACAO_CONTA': 'conta', 'FINALIZADO': 'historico'
    };
    return map[etapa] || 'dados';
  }

  salvarDados() {
    if (!this.proposta) return;
    this.erro = undefined;
    this.service.atualizarDadosPessoais(this.proposta.id, this.proposta).subscribe({
      next: p => this.carregarProposta(p.id),
      error: e => this.erro = 'Erro ao salvar dados: ' + (e.error?.message || e.message)
    });
  }

  onFileSelected(event: any) { this.arquivoSelecionado = event.target.files[0]; }

  enviarDocumento() {
    if (!this.proposta || !this.arquivoSelecionado) return;
    this.erro = undefined;
    this.service.uploadDocumento(this.proposta.id, this.tipoDocumento, this.arquivoSelecionado)
      .subscribe({
        next: () => { this.arquivoSelecionado = undefined; this.carregarProposta(this.proposta!.id); },
        error: e => this.erro = 'Erro ao enviar documento: ' + (e.error?.message || e.message)
      });
  }

  aprovarDoc(doc: Documento) {
    if (!this.proposta) return;
    this.erro = undefined;
    this.service.aprovarDocumento(this.proposta.id, doc.id).subscribe({
      next: () => this.carregarProposta(this.proposta!.id),
      error: e => this.erro = 'Erro ao aprovar documento: ' + (e.error?.message || e.message)
    });
  }

  rejeitarDoc(doc: Documento) {
    if (!this.proposta) return;
    const motivo = prompt('Motivo da rejeição:');
    if (!motivo) return;
    this.erro = undefined;
    this.service.rejeitarDocumento(this.proposta.id, doc.id, motivo).subscribe({
      next: () => this.carregarProposta(this.proposta!.id),
      error: e => this.erro = 'Erro ao rejeitar documento: ' + (e.error?.message || e.message)
    });
  }

  executarCompliance() {
    if (!this.proposta) return;
    this.erro = undefined;
    this.executandoCompliance = true;
    this.service.executarCompliance(this.proposta.id).subscribe({
      next: () => {
        this.executandoCompliance = false;
        this.service.listarValidacoes(this.proposta!.id).subscribe(v => this.validacoes = v);
        this.carregarProposta(this.proposta!.id);
      },
      error: e => {
        this.executandoCompliance = false;
        this.erro = 'Erro ao executar compliance: ' + (e.error?.message || e.message);
      }
    });
  }

  analisarRisco() {
    if (!this.proposta) return;
    this.erro = undefined;
    this.analisandoRisco = true;
    this.service.analisarRisco(this.proposta.id, this.rendaInformada).subscribe({
      next: r => {
        this.analiseRisco = r;
        this.analisandoRisco = false;
        this.carregarProposta(this.proposta!.id);
      },
      error: e => {
        this.analisandoRisco = false;
        this.erro = 'Erro ao analisar risco: ' + (e.error?.message || e.message);
      }
    });
  }

  solicitarConsentimento() {
    if (!this.proposta) return;
    this.erro = undefined;
    this.service.solicitarConsentimento(this.proposta.id).subscribe({
      next: () => this.carregarProposta(this.proposta!.id),
      error: e => this.erro = 'Erro ao solicitar consentimento: ' + (e.error?.message || e.message)
    });
  }

  criarConta() {
    if (!this.proposta) return;
    this.erro = undefined;
    this.service.criarConta(this.proposta.id, this.tipoConta).subscribe({
      next: c => {
        this.contaCriada = c;
        this.carregarProposta(this.proposta!.id);
      },
      error: e => this.erro = 'Erro ao criar conta: ' + (e.error?.message || e.message)
    });
  }

  ativarConta() {
    if (!this.proposta || !this.contaCriada) return;
    this.erro = undefined;
    this.service.ativarConta(this.proposta.id, this.contaCriada.id).subscribe({
      next: c => {
        this.contaCriada = c;
        this.carregarProposta(this.proposta!.id);
      },
      error: e => this.erro = 'Erro ao ativar conta: ' + (e.error?.message || e.message)
    });
  }

  cancelar() {
    if (!this.proposta) return;
    const motivo = prompt('Motivo do cancelamento:');
    if (!motivo) return;
    this.erro = undefined;
    this.service.cancelar(this.proposta.id, motivo).subscribe({
      next: () => this.carregarProposta(this.proposta!.id),
      error: e => this.erro = 'Erro ao cancelar proposta: ' + (e.error?.message || e.message)
    });
  }

  protected readonly statusClass = statusClass;
  protected readonly statusLabel = statusLabel;
}
