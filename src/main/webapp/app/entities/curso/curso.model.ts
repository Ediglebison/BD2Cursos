import dayjs from 'dayjs/esm';
import { IProfessor } from 'app/entities/professor/professor.model';
import { IUsuario } from 'app/entities/usuario/usuario.model';

export interface ICurso {
  id?: number;
  curso?: string | null;
  duracaoCH?: number | null;
  descricao?: string | null;
  valor?: number | null;
  criacao?: dayjs.Dayjs | null;
  professor?: IProfessor | null;
  usuario?: IUsuario | null;
}

export class Curso implements ICurso {
  constructor(
    public id?: number,
    public curso?: string | null,
    public duracaoCH?: number | null,
    public descricao?: string | null,
    public valor?: number | null,
    public criacao?: dayjs.Dayjs | null,
    public professor?: IProfessor | null,
    public usuario?: IUsuario | null
  ) {}
}

export function getCursoIdentifier(curso: ICurso): number | undefined {
  return curso.id;
}
