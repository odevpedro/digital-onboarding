import { Routes } from '@angular/router';
import { PropostaListaComponent } from './proposta-lista/proposta-lista.component';
import { PropostaDetalheComponent } from './proposta-detalhe/proposta-detalhe.component';

export const routes: Routes = [
  { path: '', component: PropostaListaComponent },
  { path: 'proposta/:id', component: PropostaDetalheComponent },
  { path: '**', redirectTo: '' }
];
