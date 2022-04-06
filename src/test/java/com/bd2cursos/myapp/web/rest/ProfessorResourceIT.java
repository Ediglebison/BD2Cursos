package com.bd2cursos.myapp.web.rest;

import static com.bd2cursos.myapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bd2cursos.myapp.IntegrationTest;
import com.bd2cursos.myapp.domain.Professor;
import com.bd2cursos.myapp.repository.ProfessorRepository;
import com.bd2cursos.myapp.service.criteria.ProfessorCriteria;
import com.bd2cursos.myapp.service.dto.ProfessorDTO;
import com.bd2cursos.myapp.service.mapper.ProfessorMapper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link ProfessorResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class ProfessorResourceIT {

    private static final String DEFAULT_NOME = "AAAAAAAAAA";
    private static final String UPDATED_NOME = "BBBBBBBBBB";

    private static final String DEFAULT_CPF = "AAAAAAAAAA";
    private static final String UPDATED_CPF = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATA_NASCIMENTO = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATA_NASCIMENTO = LocalDate.now(ZoneId.systemDefault());
    private static final LocalDate SMALLER_DATA_NASCIMENTO = LocalDate.ofEpochDay(-1L);

    private static final ZonedDateTime DEFAULT_CRIACAO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CRIACAO = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_CRIACAO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/professors";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private ProfessorMapper professorMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restProfessorMockMvc;

    private Professor professor;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professor createEntity(EntityManager em) {
        Professor professor = new Professor()
            .nome(DEFAULT_NOME)
            .cpf(DEFAULT_CPF)
            .dataNascimento(DEFAULT_DATA_NASCIMENTO)
            .criacao(DEFAULT_CRIACAO);
        return professor;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Professor createUpdatedEntity(EntityManager em) {
        Professor professor = new Professor()
            .nome(UPDATED_NOME)
            .cpf(UPDATED_CPF)
            .dataNascimento(UPDATED_DATA_NASCIMENTO)
            .criacao(UPDATED_CRIACAO);
        return professor;
    }

    @BeforeEach
    public void initTest() {
        professor = createEntity(em);
    }

    @Test
    @Transactional
    void createProfessor() throws Exception {
        int databaseSizeBeforeCreate = professorRepository.findAll().size();
        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);
        restProfessorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(professorDTO)))
            .andExpect(status().isCreated());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeCreate + 1);
        Professor testProfessor = professorList.get(professorList.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(DEFAULT_NOME);
        assertThat(testProfessor.getCpf()).isEqualTo(DEFAULT_CPF);
        assertThat(testProfessor.getDataNascimento()).isEqualTo(DEFAULT_DATA_NASCIMENTO);
        assertThat(testProfessor.getCriacao()).isEqualTo(DEFAULT_CRIACAO);
    }

    @Test
    @Transactional
    void createProfessorWithExistingId() throws Exception {
        // Create the Professor with an existing ID
        professor.setId(1L);
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        int databaseSizeBeforeCreate = professorRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restProfessorMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(professorDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllProfessors() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(professor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].cpf").value(hasItem(DEFAULT_CPF)))
            .andExpect(jsonPath("$.[*].dataNascimento").value(hasItem(DEFAULT_DATA_NASCIMENTO.toString())))
            .andExpect(jsonPath("$.[*].criacao").value(hasItem(sameInstant(DEFAULT_CRIACAO))));
    }

    @Test
    @Transactional
    void getProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get the professor
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL_ID, professor.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(professor.getId().intValue()))
            .andExpect(jsonPath("$.nome").value(DEFAULT_NOME))
            .andExpect(jsonPath("$.cpf").value(DEFAULT_CPF))
            .andExpect(jsonPath("$.dataNascimento").value(DEFAULT_DATA_NASCIMENTO.toString()))
            .andExpect(jsonPath("$.criacao").value(sameInstant(DEFAULT_CRIACAO)));
    }

    @Test
    @Transactional
    void getProfessorsByIdFiltering() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        Long id = professor.getId();

        defaultProfessorShouldBeFound("id.equals=" + id);
        defaultProfessorShouldNotBeFound("id.notEquals=" + id);

        defaultProfessorShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProfessorShouldNotBeFound("id.greaterThan=" + id);

        defaultProfessorShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProfessorShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeIsEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome equals to DEFAULT_NOME
        defaultProfessorShouldBeFound("nome.equals=" + DEFAULT_NOME);

        // Get all the professorList where nome equals to UPDATED_NOME
        defaultProfessorShouldNotBeFound("nome.equals=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome not equals to DEFAULT_NOME
        defaultProfessorShouldNotBeFound("nome.notEquals=" + DEFAULT_NOME);

        // Get all the professorList where nome not equals to UPDATED_NOME
        defaultProfessorShouldBeFound("nome.notEquals=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeIsInShouldWork() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome in DEFAULT_NOME or UPDATED_NOME
        defaultProfessorShouldBeFound("nome.in=" + DEFAULT_NOME + "," + UPDATED_NOME);

        // Get all the professorList where nome equals to UPDATED_NOME
        defaultProfessorShouldNotBeFound("nome.in=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeIsNullOrNotNull() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome is not null
        defaultProfessorShouldBeFound("nome.specified=true");

        // Get all the professorList where nome is null
        defaultProfessorShouldNotBeFound("nome.specified=false");
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeContainsSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome contains DEFAULT_NOME
        defaultProfessorShouldBeFound("nome.contains=" + DEFAULT_NOME);

        // Get all the professorList where nome contains UPDATED_NOME
        defaultProfessorShouldNotBeFound("nome.contains=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllProfessorsByNomeNotContainsSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where nome does not contain DEFAULT_NOME
        defaultProfessorShouldNotBeFound("nome.doesNotContain=" + DEFAULT_NOME);

        // Get all the professorList where nome does not contain UPDATED_NOME
        defaultProfessorShouldBeFound("nome.doesNotContain=" + UPDATED_NOME);
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfIsEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf equals to DEFAULT_CPF
        defaultProfessorShouldBeFound("cpf.equals=" + DEFAULT_CPF);

        // Get all the professorList where cpf equals to UPDATED_CPF
        defaultProfessorShouldNotBeFound("cpf.equals=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfIsNotEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf not equals to DEFAULT_CPF
        defaultProfessorShouldNotBeFound("cpf.notEquals=" + DEFAULT_CPF);

        // Get all the professorList where cpf not equals to UPDATED_CPF
        defaultProfessorShouldBeFound("cpf.notEquals=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfIsInShouldWork() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf in DEFAULT_CPF or UPDATED_CPF
        defaultProfessorShouldBeFound("cpf.in=" + DEFAULT_CPF + "," + UPDATED_CPF);

        // Get all the professorList where cpf equals to UPDATED_CPF
        defaultProfessorShouldNotBeFound("cpf.in=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfIsNullOrNotNull() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf is not null
        defaultProfessorShouldBeFound("cpf.specified=true");

        // Get all the professorList where cpf is null
        defaultProfessorShouldNotBeFound("cpf.specified=false");
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfContainsSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf contains DEFAULT_CPF
        defaultProfessorShouldBeFound("cpf.contains=" + DEFAULT_CPF);

        // Get all the professorList where cpf contains UPDATED_CPF
        defaultProfessorShouldNotBeFound("cpf.contains=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllProfessorsByCpfNotContainsSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where cpf does not contain DEFAULT_CPF
        defaultProfessorShouldNotBeFound("cpf.doesNotContain=" + DEFAULT_CPF);

        // Get all the professorList where cpf does not contain UPDATED_CPF
        defaultProfessorShouldBeFound("cpf.doesNotContain=" + UPDATED_CPF);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento equals to DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.equals=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento equals to UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.equals=" + UPDATED_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento not equals to DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.notEquals=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento not equals to UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.notEquals=" + UPDATED_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsInShouldWork() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento in DEFAULT_DATA_NASCIMENTO or UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.in=" + DEFAULT_DATA_NASCIMENTO + "," + UPDATED_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento equals to UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.in=" + UPDATED_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsNullOrNotNull() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento is not null
        defaultProfessorShouldBeFound("dataNascimento.specified=true");

        // Get all the professorList where dataNascimento is null
        defaultProfessorShouldNotBeFound("dataNascimento.specified=false");
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento is greater than or equal to DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.greaterThanOrEqual=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento is greater than or equal to UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.greaterThanOrEqual=" + UPDATED_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento is less than or equal to DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.lessThanOrEqual=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento is less than or equal to SMALLER_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.lessThanOrEqual=" + SMALLER_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsLessThanSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento is less than DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.lessThan=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento is less than UPDATED_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.lessThan=" + UPDATED_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByDataNascimentoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where dataNascimento is greater than DEFAULT_DATA_NASCIMENTO
        defaultProfessorShouldNotBeFound("dataNascimento.greaterThan=" + DEFAULT_DATA_NASCIMENTO);

        // Get all the professorList where dataNascimento is greater than SMALLER_DATA_NASCIMENTO
        defaultProfessorShouldBeFound("dataNascimento.greaterThan=" + SMALLER_DATA_NASCIMENTO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao equals to DEFAULT_CRIACAO
        defaultProfessorShouldBeFound("criacao.equals=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao equals to UPDATED_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.equals=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao not equals to DEFAULT_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.notEquals=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao not equals to UPDATED_CRIACAO
        defaultProfessorShouldBeFound("criacao.notEquals=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsInShouldWork() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao in DEFAULT_CRIACAO or UPDATED_CRIACAO
        defaultProfessorShouldBeFound("criacao.in=" + DEFAULT_CRIACAO + "," + UPDATED_CRIACAO);

        // Get all the professorList where criacao equals to UPDATED_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.in=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsNullOrNotNull() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao is not null
        defaultProfessorShouldBeFound("criacao.specified=true");

        // Get all the professorList where criacao is null
        defaultProfessorShouldNotBeFound("criacao.specified=false");
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao is greater than or equal to DEFAULT_CRIACAO
        defaultProfessorShouldBeFound("criacao.greaterThanOrEqual=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao is greater than or equal to UPDATED_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.greaterThanOrEqual=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao is less than or equal to DEFAULT_CRIACAO
        defaultProfessorShouldBeFound("criacao.lessThanOrEqual=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao is less than or equal to SMALLER_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.lessThanOrEqual=" + SMALLER_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsLessThanSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao is less than DEFAULT_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.lessThan=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao is less than UPDATED_CRIACAO
        defaultProfessorShouldBeFound("criacao.lessThan=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllProfessorsByCriacaoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        // Get all the professorList where criacao is greater than DEFAULT_CRIACAO
        defaultProfessorShouldNotBeFound("criacao.greaterThan=" + DEFAULT_CRIACAO);

        // Get all the professorList where criacao is greater than SMALLER_CRIACAO
        defaultProfessorShouldBeFound("criacao.greaterThan=" + SMALLER_CRIACAO);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProfessorShouldBeFound(String filter) throws Exception {
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(professor.getId().intValue())))
            .andExpect(jsonPath("$.[*].nome").value(hasItem(DEFAULT_NOME)))
            .andExpect(jsonPath("$.[*].cpf").value(hasItem(DEFAULT_CPF)))
            .andExpect(jsonPath("$.[*].dataNascimento").value(hasItem(DEFAULT_DATA_NASCIMENTO.toString())))
            .andExpect(jsonPath("$.[*].criacao").value(hasItem(sameInstant(DEFAULT_CRIACAO))));

        // Check, that the count call also returns 1
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProfessorShouldNotBeFound(String filter) throws Exception {
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restProfessorMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingProfessor() throws Exception {
        // Get the professor
        restProfessorMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        int databaseSizeBeforeUpdate = professorRepository.findAll().size();

        // Update the professor
        Professor updatedProfessor = professorRepository.findById(professor.getId()).get();
        // Disconnect from session so that the updates on updatedProfessor are not directly saved in db
        em.detach(updatedProfessor);
        updatedProfessor.nome(UPDATED_NOME).cpf(UPDATED_CPF).dataNascimento(UPDATED_DATA_NASCIMENTO).criacao(UPDATED_CRIACAO);
        ProfessorDTO professorDTO = professorMapper.toDto(updatedProfessor);

        restProfessorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isOk());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
        Professor testProfessor = professorList.get(professorList.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProfessor.getCpf()).isEqualTo(UPDATED_CPF);
        assertThat(testProfessor.getDataNascimento()).isEqualTo(UPDATED_DATA_NASCIMENTO);
        assertThat(testProfessor.getCriacao()).isEqualTo(UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void putNonExistingProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, professorDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(professorDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateProfessorWithPatch() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        int databaseSizeBeforeUpdate = professorRepository.findAll().size();

        // Update the professor using partial update
        Professor partialUpdatedProfessor = new Professor();
        partialUpdatedProfessor.setId(professor.getId());

        partialUpdatedProfessor.nome(UPDATED_NOME).cpf(UPDATED_CPF).criacao(UPDATED_CRIACAO);

        restProfessorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProfessor))
            )
            .andExpect(status().isOk());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
        Professor testProfessor = professorList.get(professorList.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProfessor.getCpf()).isEqualTo(UPDATED_CPF);
        assertThat(testProfessor.getDataNascimento()).isEqualTo(DEFAULT_DATA_NASCIMENTO);
        assertThat(testProfessor.getCriacao()).isEqualTo(UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void fullUpdateProfessorWithPatch() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        int databaseSizeBeforeUpdate = professorRepository.findAll().size();

        // Update the professor using partial update
        Professor partialUpdatedProfessor = new Professor();
        partialUpdatedProfessor.setId(professor.getId());

        partialUpdatedProfessor.nome(UPDATED_NOME).cpf(UPDATED_CPF).dataNascimento(UPDATED_DATA_NASCIMENTO).criacao(UPDATED_CRIACAO);

        restProfessorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedProfessor.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedProfessor))
            )
            .andExpect(status().isOk());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
        Professor testProfessor = professorList.get(professorList.size() - 1);
        assertThat(testProfessor.getNome()).isEqualTo(UPDATED_NOME);
        assertThat(testProfessor.getCpf()).isEqualTo(UPDATED_CPF);
        assertThat(testProfessor.getDataNascimento()).isEqualTo(UPDATED_DATA_NASCIMENTO);
        assertThat(testProfessor.getCriacao()).isEqualTo(UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void patchNonExistingProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, professorDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamProfessor() throws Exception {
        int databaseSizeBeforeUpdate = professorRepository.findAll().size();
        professor.setId(count.incrementAndGet());

        // Create the Professor
        ProfessorDTO professorDTO = professorMapper.toDto(professor);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restProfessorMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(professorDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Professor in the database
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteProfessor() throws Exception {
        // Initialize the database
        professorRepository.saveAndFlush(professor);

        int databaseSizeBeforeDelete = professorRepository.findAll().size();

        // Delete the professor
        restProfessorMockMvc
            .perform(delete(ENTITY_API_URL_ID, professor.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Professor> professorList = professorRepository.findAll();
        assertThat(professorList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
