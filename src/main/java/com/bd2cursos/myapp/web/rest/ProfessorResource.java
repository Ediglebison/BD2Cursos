package com.bd2cursos.myapp.web.rest;

import com.bd2cursos.myapp.repository.ProfessorRepository;
import com.bd2cursos.myapp.service.ProfessorQueryService;
import com.bd2cursos.myapp.service.ProfessorService;
import com.bd2cursos.myapp.service.criteria.ProfessorCriteria;
import com.bd2cursos.myapp.service.dto.ProfessorDTO;
import com.bd2cursos.myapp.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.bd2cursos.myapp.domain.Professor}.
 */
@RestController
@RequestMapping("/api")
public class ProfessorResource {

    private final Logger log = LoggerFactory.getLogger(ProfessorResource.class);

    private static final String ENTITY_NAME = "professor";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProfessorService professorService;

    private final ProfessorRepository professorRepository;

    private final ProfessorQueryService professorQueryService;

    public ProfessorResource(
        ProfessorService professorService,
        ProfessorRepository professorRepository,
        ProfessorQueryService professorQueryService
    ) {
        this.professorService = professorService;
        this.professorRepository = professorRepository;
        this.professorQueryService = professorQueryService;
    }

    /**
     * {@code POST  /professors} : Create a new professor.
     *
     * @param professorDTO the professorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new professorDTO, or with status {@code 400 (Bad Request)} if the professor has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/professors")
    public ResponseEntity<ProfessorDTO> createProfessor(@RequestBody ProfessorDTO professorDTO) throws URISyntaxException {
        log.debug("REST request to save Professor : {}", professorDTO);
        if (professorDTO.getId() != null) {
            throw new BadRequestAlertException("A new professor cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProfessorDTO result = professorService.save(professorDTO);
        return ResponseEntity
            .created(new URI("/api/professors/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /professors/:id} : Updates an existing professor.
     *
     * @param id the id of the professorDTO to save.
     * @param professorDTO the professorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professorDTO,
     * or with status {@code 400 (Bad Request)} if the professorDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the professorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/professors/{id}")
    public ResponseEntity<ProfessorDTO> updateProfessor(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ProfessorDTO professorDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Professor : {}, {}", id, professorDTO);
        if (professorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ProfessorDTO result = professorService.update(professorDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, professorDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /professors/:id} : Partial updates given fields of an existing professor, field will ignore if it is null
     *
     * @param id the id of the professorDTO to save.
     * @param professorDTO the professorDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated professorDTO,
     * or with status {@code 400 (Bad Request)} if the professorDTO is not valid,
     * or with status {@code 404 (Not Found)} if the professorDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the professorDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/professors/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<ProfessorDTO> partialUpdateProfessor(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody ProfessorDTO professorDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Professor partially : {}, {}", id, professorDTO);
        if (professorDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, professorDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!professorRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ProfessorDTO> result = professorService.partialUpdate(professorDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, professorDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /professors} : get all the professors.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of professors in body.
     */
    @GetMapping("/professors")
    public ResponseEntity<List<ProfessorDTO>> getAllProfessors(
        ProfessorCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get Professors by criteria: {}", criteria);
        Page<ProfessorDTO> page = professorQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /professors/count} : count all the professors.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/professors/count")
    public ResponseEntity<Long> countProfessors(ProfessorCriteria criteria) {
        log.debug("REST request to count Professors by criteria: {}", criteria);
        return ResponseEntity.ok().body(professorQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /professors/:id} : get the "id" professor.
     *
     * @param id the id of the professorDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the professorDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/professors/{id}")
    public ResponseEntity<ProfessorDTO> getProfessor(@PathVariable Long id) {
        log.debug("REST request to get Professor : {}", id);
        Optional<ProfessorDTO> professorDTO = professorService.findOne(id);
        return ResponseUtil.wrapOrNotFound(professorDTO);
    }

    /**
     * {@code DELETE  /professors/:id} : delete the "id" professor.
     *
     * @param id the id of the professorDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/professors/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        log.debug("REST request to delete Professor : {}", id);
        professorService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
