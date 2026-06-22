import { TestBed } from '@angular/core/testing';
import { HttpClient } from '@angular/common/http';
import { HttpTestingController, HttpClientTestingModule } from '@angular/common/http/testing';
import { PropostaService } from './proposta.service';

describe('PropostaService', () => {
  let service: PropostaService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PropostaService]
    });
    service = TestBed.inject(PropostaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve criar proposta via POST para /api/propostas', () => {
    service.criar('52998224725').subscribe(res => {
      expect(res).toEqual(jasmine.objectContaining({ cpfCnpj: '52998224725' }));
    });

    const req = httpMock.expectOne('/api/propostas');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ cpfCnpj: '52998224725' });
    req.flush({ id: '1', cpfCnpj: '52998224725' });
  });

  it('deve buscar proposta por id via GET', () => {
    service.buscar('1').subscribe(res => {
      expect(res.id).toBe('1');
    });

    const req = httpMock.expectOne('/api/propostas/1');
    expect(req.request.method).toBe('GET');
    req.flush({ id: '1' });
  });

  it('deve atualizar dados pessoais via PUT', () => {
    const dados = { nomeCompleto: 'Joao' };
    service.atualizarDadosPessoais('1', dados as any).subscribe();

    const req = httpMock.expectOne('/api/propostas/1/dados-pessoais');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(dados);
    req.flush({ id: '1', nomeCompleto: 'Joao' });
  });

  it('deve listar todas as propostas via GET', () => {
    service.listar().subscribe(res => expect(res.length).toBe(2));

    const req = httpMock.expectOne('/api/propostas');
    expect(req.request.method).toBe('GET');
    req.flush([{ id: '1' }, { id: '2' }]);
  });

  it('deve chamar criar conta via POST', () => {
    service.criarConta('1', 'CORRENTE').subscribe();

    const req = httpMock.expectOne('/api/propostas/1/conta/criar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ tipoConta: 'CORRENTE' });
    req.flush({ id: 'c1', agencia: '1234' });
  });

  it('deve enviar documento via POST multipart', () => {
    const file = new File(['test'], 'doc.pdf', { type: 'application/pdf' });
    service.uploadDocumento('1', 'RG', file).subscribe();

    const req = httpMock.expectOne('/api/propostas/1/documentos');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'd1' });
  });

  it('deve aprovar documento via POST', () => {
    service.aprovarDocumento('1', 'd1').subscribe();

    const req = httpMock.expectOne('/api/propostas/1/documentos/d1/aprovar');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 'd1', status: 'APROVADO' });
  });

  it('deve solicitar consentimento via POST', () => {
    service.solicitarConsentimento('1').subscribe();

    const req = httpMock.expectOne('/api/propostas/1/consentimento/solicitar');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    req.flush({ consentId: 'c1' });
  });
});
