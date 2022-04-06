import dayjs from 'dayjs/esm';

export interface IProfessor {
  id?: number;
  nome?: string | null;
  cpf?: string | null;
  dataNascimento?: dayjs.Dayjs | null;
  criacao?: dayjs.Dayjs | null;
}

export class Professor implements IProfessor {
  constructor(
    public id?: number,
    public nome?: string | null,
    public cpf?: string | null,
    public dataNascimento?: dayjs.Dayjs | null,
    public criacao?: dayjs.Dayjs | null
  ) {}
}

export function getProfessorIdentifier(professor: IProfessor): number | undefined {
  return professor.id;
}
