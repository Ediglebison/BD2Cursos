import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, Subject, from } from 'rxjs';

import { CursoService } from '../service/curso.service';
import { ICurso, Curso } from '../curso.model';
import { IProfessor } from 'app/entities/professor/professor.model';
import { ProfessorService } from 'app/entities/professor/service/professor.service';
import { IUsuario } from 'app/entities/usuario/usuario.model';
import { UsuarioService } from 'app/entities/usuario/service/usuario.service';

import { CursoUpdateComponent } from './curso-update.component';

describe('Curso Management Update Component', () => {
  let comp: CursoUpdateComponent;
  let fixture: ComponentFixture<CursoUpdateComponent>;
  let activatedRoute: ActivatedRoute;
  let cursoService: CursoService;
  let professorService: ProfessorService;
  let usuarioService: UsuarioService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [CursoUpdateComponent],
      providers: [
        FormBuilder,
        {
          provide: ActivatedRoute,
          useValue: {
            params: from([{}]),
          },
        },
      ],
    })
      .overrideTemplate(CursoUpdateComponent, '')
      .compileComponents();

    fixture = TestBed.createComponent(CursoUpdateComponent);
    activatedRoute = TestBed.inject(ActivatedRoute);
    cursoService = TestBed.inject(CursoService);
    professorService = TestBed.inject(ProfessorService);
    usuarioService = TestBed.inject(UsuarioService);

    comp = fixture.componentInstance;
  });

  describe('ngOnInit', () => {
    it('Should call Professor query and add missing value', () => {
      const curso: ICurso = { id: 456 };
      const professor: IProfessor = { id: 85189 };
      curso.professor = professor;

      const professorCollection: IProfessor[] = [{ id: 42138 }];
      jest.spyOn(professorService, 'query').mockReturnValue(of(new HttpResponse({ body: professorCollection })));
      const additionalProfessors = [professor];
      const expectedCollection: IProfessor[] = [...additionalProfessors, ...professorCollection];
      jest.spyOn(professorService, 'addProfessorToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      expect(professorService.query).toHaveBeenCalled();
      expect(professorService.addProfessorToCollectionIfMissing).toHaveBeenCalledWith(professorCollection, ...additionalProfessors);
      expect(comp.professorsSharedCollection).toEqual(expectedCollection);
    });

    it('Should call Usuario query and add missing value', () => {
      const curso: ICurso = { id: 456 };
      const usuario: IUsuario = { id: 88725 };
      curso.usuario = usuario;

      const usuarioCollection: IUsuario[] = [{ id: 16792 }];
      jest.spyOn(usuarioService, 'query').mockReturnValue(of(new HttpResponse({ body: usuarioCollection })));
      const additionalUsuarios = [usuario];
      const expectedCollection: IUsuario[] = [...additionalUsuarios, ...usuarioCollection];
      jest.spyOn(usuarioService, 'addUsuarioToCollectionIfMissing').mockReturnValue(expectedCollection);

      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      expect(usuarioService.query).toHaveBeenCalled();
      expect(usuarioService.addUsuarioToCollectionIfMissing).toHaveBeenCalledWith(usuarioCollection, ...additionalUsuarios);
      expect(comp.usuariosSharedCollection).toEqual(expectedCollection);
    });

    it('Should update editForm', () => {
      const curso: ICurso = { id: 456 };
      const professor: IProfessor = { id: 26749 };
      curso.professor = professor;
      const usuario: IUsuario = { id: 85434 };
      curso.usuario = usuario;

      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      expect(comp.editForm.value).toEqual(expect.objectContaining(curso));
      expect(comp.professorsSharedCollection).toContain(professor);
      expect(comp.usuariosSharedCollection).toContain(usuario);
    });
  });

  describe('save', () => {
    it('Should call update service on save for existing entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Curso>>();
      const curso = { id: 123 };
      jest.spyOn(cursoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: curso }));
      saveSubject.complete();

      // THEN
      expect(comp.previousState).toHaveBeenCalled();
      expect(cursoService.update).toHaveBeenCalledWith(curso);
      expect(comp.isSaving).toEqual(false);
    });

    it('Should call create service on save for new entity', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Curso>>();
      const curso = new Curso();
      jest.spyOn(cursoService, 'create').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.next(new HttpResponse({ body: curso }));
      saveSubject.complete();

      // THEN
      expect(cursoService.create).toHaveBeenCalledWith(curso);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).toHaveBeenCalled();
    });

    it('Should set isSaving to false on error', () => {
      // GIVEN
      const saveSubject = new Subject<HttpResponse<Curso>>();
      const curso = { id: 123 };
      jest.spyOn(cursoService, 'update').mockReturnValue(saveSubject);
      jest.spyOn(comp, 'previousState');
      activatedRoute.data = of({ curso });
      comp.ngOnInit();

      // WHEN
      comp.save();
      expect(comp.isSaving).toEqual(true);
      saveSubject.error('This is an error!');

      // THEN
      expect(cursoService.update).toHaveBeenCalledWith(curso);
      expect(comp.isSaving).toEqual(false);
      expect(comp.previousState).not.toHaveBeenCalled();
    });
  });

  describe('Tracking relationships identifiers', () => {
    describe('trackProfessorById', () => {
      it('Should return tracked Professor primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackProfessorById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });

    describe('trackUsuarioById', () => {
      it('Should return tracked Usuario primary key', () => {
        const entity = { id: 123 };
        const trackResult = comp.trackUsuarioById(0, entity);
        expect(trackResult).toEqual(entity.id);
      });
    });
  });
});
