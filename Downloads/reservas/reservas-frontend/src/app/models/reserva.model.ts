export interface Reserva {
  id?: number;
  nomeCliente: string;
  emailCliente: string;
  dataCheckIn: string;
  dataCheckOut: string;
  quantidadePessoas: number;
  observacoes?: string;
  dataCriacao?: string;
}
