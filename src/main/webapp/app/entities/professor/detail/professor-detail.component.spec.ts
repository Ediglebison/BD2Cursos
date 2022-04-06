import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';

import { ProfessorDetailComponent } from './professor-detail.component';

describe('Professor Management Detail Component', () => {
  let comp: ProfessorDetailComponent;
  let fixture: ComponentFixture<ProfessorDetailComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ProfessorDetailComponent],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: { data: of({ professor: { id: 123 } }) },
        },
      ],
    })
      .overrideTemplate(ProfessorDetailComponent, '')
      .compileComponents();
    fixture = TestBed.createComponent(ProfessorDetailComponent);
    comp = fixture.componentInstance;
  });

  describe('OnInit', () => {
    it('Should load professor on init', () => {
      // WHEN
      comp.ngOnInit();

      // THEN
      expect(comp.professor).toEqual(expect.objectContaining({ id: 123 }));
    });
  });
});
