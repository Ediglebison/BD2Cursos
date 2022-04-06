import dayjs from 'dayjs/esm';

export interface IUsuario {
  id?: number;
  nome?: string | null;
  cpf?: string | null;
  dataNascimento?: dayjs.Dayjs | null;
  criacao?: dayjs.Dayjs | null;
}

export class Usuario implements IUsuario {
  constructor(
    public id?: number,
    public nome?: string | null,
    public cpf?: string | null,
    public dataNascimento?: dayjs.Dayjs | null,
    public criacao?: dayjs.Dayjs | null
  ) {}
}

export function getUsuarioIdentifier(usuario: IUsuario): number | undefined {
  return usuario.id;
}
