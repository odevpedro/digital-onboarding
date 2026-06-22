import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PropostaService, Proposta } from '../proposta.service';
import { statusClass, statusLabel, etapaProxima } from '../status-helper';

@Component({
  selector: 'app-proposta-lista',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="alerts" *ngIf="erro">
      <div class="alert alert-danger">
        {{ erro }}
        <button class="alert-close" (click)="erro = undefined">&times;</button>
      </div>
    </div>

    <div class="page-header">
      <h1>Propostas de Onboarding</h1>
      <button class="btn btn-primary" (click)="mostrarForm = !mostrarForm">
        {{ mostrarForm ? 'Cancelar' : 'Nova Proposta' }}
      </button>
    </div>

    <div class="card" *ngIf="mostrarForm">
      <h3>Nova Proposta</h3>
      <div class="form-group">
        <label>CPF ou CNPJ</label>
        <input class="form-control" [(ngModel)]="novoCpfCnpj" placeholder="000.000.000-00 ou 00.000.000/0000-00">
      </div>
      <button class="btn btn-success" (click)="criar()" [disabled]="!novoCpfCnpj">Criar Proposta</button>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>Cliente</th>
            <th>Documento</th>
            <th>Status</th>
            <th>Progresso</th>
            <th>Criado em</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let p of propostas">
            <td>{{ p.nomeCompleto || '-' }}</td>
            <td>{{ p.cpfCnpj }}</td>
            <td><span class="badge {{ statusClass(p.status) }}">{{ statusLabel(p.status) }}</span></td>
            <td>
              <div class="progress-bar">
                <div class="progress-fill" [style.width.%]="etapaProxima(p.etapaAtual)"></div>
              </div>
            </td>
            <td>{{ p.criadoEm | date:'short' }}</td>
            <td>
              <a [routerLink]="['/proposta', p.id]" class="btn btn-outline btn-sm">Detalhes</a>
            </td>
          </tr>
          <tr *ngIf="propostas.length === 0">
            <td colspan="6" style="text-align:center;padding:40px;color:#999;">
              Nenhuma proposta encontrada. Crie uma nova proposta para começar.
            </td>
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
    .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
    .page-header h1 { font-size: 24px; font-weight: 600; }
    .btn-sm { padding: 6px 14px; font-size: 12px; }
    .progress-bar { width: 120px; height: 6px; background: #E0E0E0; border-radius: 3px; overflow: hidden; }
    .progress-fill { height: 100%; background: var(--color-accent); transition: width 0.3s; }
  `]
})
export class PropostaListaComponent implements OnInit {
  propostas: Proposta[] = [];
  mostrarForm = false;
  novoCpfCnpj = '';
  erro?: string;

  constructor(private service: PropostaService) {}

  ngOnInit() { this.carregar(); }

  carregar() {
    this.erro = undefined;
    this.service.listar().subscribe({
      next: d => this.propostas = d,
      error: e => this.erro = 'Erro ao carregar propostas: ' + (e.error?.message || e.message || 'Erro desconhecido')
    });
  }

  criar() {
    if (!this.novoCpfCnpj) return;
    this.erro = undefined;
    this.service.criar(this.novoCpfCnpj).subscribe({
      next: () => {
        this.novoCpfCnpj = '';
        this.mostrarForm = false;
        this.carregar();
      },
      error: e => this.erro = 'Erro ao criar proposta: ' + (e.error?.message || e.message || 'Erro desconhecido')
    });
  }

  protected readonly statusClass = statusClass;
  protected readonly statusLabel = statusLabel;
  protected readonly etapaProxima = etapaProxima;
}
