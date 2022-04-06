package com.bd2cursos.myapp.service;

import com.bd2cursos.myapp.domain.*; // for static metamodels
import com.bd2cursos.myapp.domain.Professor;
import com.bd2cursos.myapp.repository.ProfessorRepository;
import com.bd2cursos.myapp.service.criteria.ProfessorCriteria;
import com.bd2cursos.myapp.service.dto.ProfessorDTO;
import com.bd2cursos.myapp.service.mapper.ProfessorMapper;
import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Professor} entities in the database.
 * The main input is a {@link ProfessorCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ProfessorDTO} or a {@link Page} of {@link ProfessorDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ProfessorQueryService extends QueryService<Professor> {

    private final Logger log = LoggerFactory.getLogger(ProfessorQueryService.class);

    private final ProfessorRepository professorRepository;

    private final ProfessorMapper professorMapper;

    public ProfessorQueryService(ProfessorRepository professorRepository, ProfessorMapper professorMapper) {
        this.professorRepository = professorRepository;
        this.professorMapper = professorMapper;
    }

    /**
     * Return a {@link List} of {@link ProfessorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ProfessorDTO> findByCriteria(ProfessorCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Professor> specification = createSpecification(criteria);
        return professorMapper.toDto(professorRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link ProfessorDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ProfessorDTO> findByCriteria(ProfessorCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Professor> specification = createSpecification(criteria);
        return professorRepository.findAll(specification, page).map(professorMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ProfessorCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<Professor> specification = createSpecification(criteria);
        return professorRepository.count(specification);
    }

    /**
     * Function to convert {@link ProfessorCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Professor> createSpecification(ProfessorCriteria criteria) {
        Specification<Professor> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), Professor_.id));
            }
            if (criteria.getNome() != null) {
                specification = specification.and(buildStringSpecification(criteria.getNome(), Professor_.nome));
            }
            if (criteria.getCpf() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCpf(), Professor_.cpf));
            }
            if (criteria.getDataNascimento() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDataNascimento(), Professor_.dataNascimento));
            }
            if (criteria.getCriacao() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCriacao(), Professor_.criacao));
            }
        }
        return specification;
    }
}
