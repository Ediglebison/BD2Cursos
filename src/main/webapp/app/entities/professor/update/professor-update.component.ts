import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';

import { IProfessor, Professor } from '../professor.model';
import { ProfessorService } from '../service/professor.service';

@Component({
  selector: 'jhi-professor-update',
  templateUrl: './professor-update.component.html',
})
export class ProfessorUpdateComponent implements OnInit {
  isSaving = false;

  editForm = this.fb.group({
    id: [],
    nome: [],
    cpf: [],
    dataNascimento: [],
    criacao: [],
  });

  constructor(protected professorService: ProfessorService, protected activatedRoute: ActivatedRoute, protected fb: FormBuilder) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ professor }) => {
      if (professor.id === undefined) {
        const today = dayjs().startOf('day');
        professor.criacao = today;
      }

      this.updateForm(professor);
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const professor = this.createFromForm();
    if (professor.id !== undefined) {
      this.subscribeToSaveResponse(this.professorService.update(professor));
    } else {
      this.subscribeToSaveResponse(this.professorService.create(professor));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IProfessor>>): void {
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

  protected updateForm(professor: IProfessor): void {
    this.editForm.patchValue({
      id: professor.id,
      nome: professor.nome,
      cpf: professor.cpf,
      dataNascimento: professor.dataNascimento,
      criacao: professor.criacao ? professor.criacao.format(DATE_TIME_FORMAT) : null,
    });
  }

  protected createFromForm(): IProfessor {
    return {
      ...new Professor(),
      id: this.editForm.get(['id'])!.value,
      nome: this.editForm.get(['nome'])!.value,
      cpf: this.editForm.get(['cpf'])!.value,
      dataNascimento: this.editForm.get(['dataNascimento'])!.value,
      criacao: this.editForm.get(['criacao'])!.value ? dayjs(this.editForm.get(['criacao'])!.value, DATE_TIME_FORMAT) : undefined,
    };
  }
}
