import dayjs from 'dayjs/esm';
import { ICurso } from 'app/entities/curso/curso.model';
import { IUsuario } from 'app/entities/usuario/usuario.model';
import { EstadoTransacao } from 'app/entities/enumerations/estado-transacao.model';

export interface ICompra {
  id?: number;
  percentualDesconto?: number | null;
  valorFinal?: number | null;
  dataCriacao?: dayjs.Dayjs | null;
  estado?: EstadoTransacao | null;
  curso?: ICurso | null;
  usuario?: IUsuario | null;
}

export class Compra implements ICompra {
  constructor(
    public id?: number,
    public percentualDesconto?: number | null,
    public valorFinal?: number | null,
    public dataCriacao?: dayjs.Dayjs | null,
    public estado?: EstadoTransacao | null,
    public curso?: ICurso | null,
    public usuario?: IUsuario | null
  ) {}
}

export function getCompraIdentifier(compra: ICompra): number | undefined {
  return compra.id;
}
