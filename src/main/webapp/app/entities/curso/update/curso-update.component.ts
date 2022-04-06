import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { ICurso, Curso } from '../curso.model';
import { CursoService } from '../service/curso.service';
import { IProfessor } from 'app/entities/professor/professor.model';
import { ProfessorService } from 'app/entities/professor/service/professor.service';
import { IUsuario } from 'app/entities/usuario/usuario.model';
import { UsuarioService } from 'app/entities/usuario/service/usuario.service';

@Component({
  selector: 'jhi-curso-update',
  templateUrl: './curso-update.component.html',
})
export class CursoUpdateComponent implements OnInit {
  isSaving = false;

  professorsSharedCollection: IProfessor[] = [];
  usuariosSharedCollection: IUsuario[] = [];

  editForm = this.fb.group({
    id: [],
    curso: [],
    duracaoCH: [],
    descricao: [],
    valor: [],
    criacao: [],
    professor: [],
    usuario: [],
  });

  constructor(
    protected cursoService: CursoService,
    protected professorService: ProfessorService,
    protected usuarioService: UsuarioService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ curso }) => {
      if (curso.id === undefined) {
        const today = dayjs().startOf('day');
        curso.criacao = today;
      }

      this.updateForm(curso);

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const curso = this.createFromForm();
    if (curso.id !== undefined) {
      this.subscribeToSaveResponse(this.cursoService.update(curso));
    } else {
      this.subscribeToSaveResponse(this.cursoService.create(curso));
    }
  }

  trackProfessorById(index: number, item: IProfessor): number {
    return item.id!;
  }

  trackUsuarioById(index: number, item: IUsuario): number {
    return item.id!;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICurso>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(curso: ICurso): void {
    this.editForm.patchValue({
      id: curso.id,
      curso: curso.curso,
      duracaoCH: curso.duracaoCH,
      descricao: curso.descricao,
      valor: curso.valor,
      criacao: curso.criacao ? curso.criacao.format(DATE_TIME_FORMAT) : null,
      professor: curso.professor,
      usuario: curso.usuario,
    });

    this.professorsSharedCollection = this.professorService.addProfessorToCollectionIfMissing(
      this.professorsSharedCollection,
      curso.professor
    );
    this.usuariosSharedCollection = this.usuarioService.addUsuarioToCollectionIfMissing(this.usuariosSharedCollection, curso.usuario);
  }

  protected loadRelationshipsOptions(): void {
    this.professorService
      .query()
      .pipe(map((res: HttpResponse<IProfessor[]>) => res.body ?? []))
      .pipe(
        map((professors: IProfessor[]) =>
          this.professorService.addProfessorToCollectionIfMissing(professors, this.editForm.get('professor')!.value)
        )
      )
      .subscribe((professors: IProfessor[]) => (this.professorsSharedCollection = professors));

    this.usuarioService
      .query()
      .pipe(map((res: HttpResponse<IUsuario[]>) => res.body ?? []))
      .pipe(
        map((usuarios: IUsuario[]) => this.usuarioService.addUsuarioToCollectionIfMissing(usuarios, this.editForm.get('usuario')!.value))
      )
      .subscribe((usuarios: IUsuario[]) => (this.usuariosSharedCollection = usuarios));
  }

  protected createFromForm(): ICurso {
    return {
      ...new Curso(),
      id: this.editForm.get(['id'])!.value,
      curso: this.editForm.get(['curso'])!.value,
      duracaoCH: this.editForm.get(['duracaoCH'])!.value,
      descricao: this.editForm.get(['descricao'])!.value,
      valor: this.editForm.get(['valor'])!.value,
      criacao: this.editForm.get(['criacao'])!.value ? dayjs(this.editForm.get(['criacao'])!.value, DATE_TIME_FORMAT) : undefined,
      professor: this.editForm.get(['professor'])!.value,
      usuario: this.editForm.get(['usuario'])!.value,
    };
  }
}
