import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IProfessor, getProfessorIdentifier } from '../professor.model';

export type EntityResponseType = HttpResponse<IProfessor>;
export type EntityArrayResponseType = HttpResponse<IProfessor[]>;

@Injectable({ providedIn: 'root' })
export class ProfessorService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/professors');

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(professor: IProfessor): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(professor);
    return this.http
      .post<IProfessor>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  update(professor: IProfessor): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(professor);
    return this.http
      .put<IProfessor>(`${this.resourceUrl}/${getProfessorIdentifier(professor) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  partialUpdate(professor: IProfessor): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(professor);
    return this.http
      .patch<IProfessor>(`${this.resourceUrl}/${getProfessorIdentifier(professor) as number}`, copy, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<IProfessor>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map((res: EntityResponseType) => this.convertDateFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IProfessor[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map((res: EntityArrayResponseType) => this.convertDateArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addProfessorToCollectionIfMissing(
    professorCollection: IProfessor[],
    ...professorsToCheck: (IProfessor | null | undefined)[]
  ): IProfessor[] {
    const professors: IProfessor[] = professorsToCheck.filter(isPresent);
    if (professors.length > 0) {
      const professorCollectionIdentifiers = professorCollection.map(professorItem => getProfessorIdentifier(professorItem)!);
      const professorsToAdd = professors.filter(professorItem => {
        const professorIdentifier = getProfessorIdentifier(professorItem);
        if (professorIdentifier == null || professorCollectionIdentifiers.includes(professorIdentifier)) {
          return false;
        }
        professorCollectionIdentifiers.push(professorIdentifier);
        return true;
      });
      return [...professorsToAdd, ...professorCollection];
    }
    return professorCollection;
  }

  protected convertDateFromClient(professor: IProfessor): IProfessor {
    return Object.assign({}, professor, {
      dataNascimento: professor.dataNascimento?.isValid() ? professor.dataNascimento.format(DATE_FORMAT) : undefined,
      criacao: professor.criacao?.isValid() ? professor.criacao.toJSON() : undefined,
    });
  }

  protected convertDateFromServer(res: EntityResponseType): EntityResponseType {
    if (res.body) {
      res.body.dataNascimento = res.body.dataNascimento ? dayjs(res.body.dataNascimento) : undefined;
      res.body.criacao = res.body.criacao ? dayjs(res.body.criacao) : undefined;
    }
    return res;
  }

  protected convertDateArrayFromServer(res: EntityArrayResponseType): EntityArrayResponseType {
    if (res.body) {
      res.body.forEach((professor: IProfessor) => {
        professor.dataNascimento = professor.dataNascimento ? dayjs(professor.dataNascimento) : undefined;
        professor.criacao = professor.criacao ? dayjs(professor.criacao) : undefined;
      });
    }
    return res;
  }
}
