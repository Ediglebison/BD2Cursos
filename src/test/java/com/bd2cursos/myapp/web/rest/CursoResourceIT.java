package com.bd2cursos.myapp.web.rest;

import static com.bd2cursos.myapp.web.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.bd2cursos.myapp.IntegrationTest;
import com.bd2cursos.myapp.domain.Curso;
import com.bd2cursos.myapp.domain.Professor;
import com.bd2cursos.myapp.domain.Usuario;
import com.bd2cursos.myapp.repository.CursoRepository;
import com.bd2cursos.myapp.service.criteria.CursoCriteria;
import com.bd2cursos.myapp.service.dto.CursoDTO;
import com.bd2cursos.myapp.service.mapper.CursoMapper;
import java.time.Instant;
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
 * Integration tests for the {@link CursoResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CursoResourceIT {

    private static final String DEFAULT_CURSO = "AAAAAAAAAA";
    private static final String UPDATED_CURSO = "BBBBBBBBBB";

    private static final Double DEFAULT_DURACAO_CH = 1D;
    private static final Double UPDATED_DURACAO_CH = 2D;
    private static final Double SMALLER_DURACAO_CH = 1D - 1D;

    private static final String DEFAULT_DESCRICAO = "AAAAAAAAAA";
    private static final String UPDATED_DESCRICAO = "BBBBBBBBBB";

    private static final Double DEFAULT_VALOR = 1D;
    private static final Double UPDATED_VALOR = 2D;
    private static final Double SMALLER_VALOR = 1D - 1D;

    private static final ZonedDateTime DEFAULT_CRIACAO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CRIACAO = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);
    private static final ZonedDateTime SMALLER_CRIACAO = ZonedDateTime.ofInstant(Instant.ofEpochMilli(-1L), ZoneOffset.UTC);

    private static final String ENTITY_API_URL = "/api/cursos";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private CursoMapper cursoMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCursoMockMvc;

    private Curso curso;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Curso createEntity(EntityManager em) {
        Curso curso = new Curso()
            .curso(DEFAULT_CURSO)
            .duracaoCH(DEFAULT_DURACAO_CH)
            .descricao(DEFAULT_DESCRICAO)
            .valor(DEFAULT_VALOR)
            .criacao(DEFAULT_CRIACAO);
        return curso;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Curso createUpdatedEntity(EntityManager em) {
        Curso curso = new Curso()
            .curso(UPDATED_CURSO)
            .duracaoCH(UPDATED_DURACAO_CH)
            .descricao(UPDATED_DESCRICAO)
            .valor(UPDATED_VALOR)
            .criacao(UPDATED_CRIACAO);
        return curso;
    }

    @BeforeEach
    public void initTest() {
        curso = createEntity(em);
    }

    @Test
    @Transactional
    void createCurso() throws Exception {
        int databaseSizeBeforeCreate = cursoRepository.findAll().size();
        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);
        restCursoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cursoDTO)))
            .andExpect(status().isCreated());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeCreate + 1);
        Curso testCurso = cursoList.get(cursoList.size() - 1);
        assertThat(testCurso.getCurso()).isEqualTo(DEFAULT_CURSO);
        assertThat(testCurso.getDuracaoCH()).isEqualTo(DEFAULT_DURACAO_CH);
        assertThat(testCurso.getDescricao()).isEqualTo(DEFAULT_DESCRICAO);
        assertThat(testCurso.getValor()).isEqualTo(DEFAULT_VALOR);
        assertThat(testCurso.getCriacao()).isEqualTo(DEFAULT_CRIACAO);
    }

    @Test
    @Transactional
    void createCursoWithExistingId() throws Exception {
        // Create the Curso with an existing ID
        curso.setId(1L);
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        int databaseSizeBeforeCreate = cursoRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCursoMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cursoDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void getAllCursos() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList
        restCursoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(curso.getId().intValue())))
            .andExpect(jsonPath("$.[*].curso").value(hasItem(DEFAULT_CURSO)))
            .andExpect(jsonPath("$.[*].duracaoCH").value(hasItem(DEFAULT_DURACAO_CH.doubleValue())))
            .andExpect(jsonPath("$.[*].descricao").value(hasItem(DEFAULT_DESCRICAO)))
            .andExpect(jsonPath("$.[*].valor").value(hasItem(DEFAULT_VALOR.doubleValue())))
            .andExpect(jsonPath("$.[*].criacao").value(hasItem(sameInstant(DEFAULT_CRIACAO))));
    }

    @Test
    @Transactional
    void getCurso() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get the curso
        restCursoMockMvc
            .perform(get(ENTITY_API_URL_ID, curso.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(curso.getId().intValue()))
            .andExpect(jsonPath("$.curso").value(DEFAULT_CURSO))
            .andExpect(jsonPath("$.duracaoCH").value(DEFAULT_DURACAO_CH.doubleValue()))
            .andExpect(jsonPath("$.descricao").value(DEFAULT_DESCRICAO))
            .andExpect(jsonPath("$.valor").value(DEFAULT_VALOR.doubleValue()))
            .andExpect(jsonPath("$.criacao").value(sameInstant(DEFAULT_CRIACAO)));
    }

    @Test
    @Transactional
    void getCursosByIdFiltering() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        Long id = curso.getId();

        defaultCursoShouldBeFound("id.equals=" + id);
        defaultCursoShouldNotBeFound("id.notEquals=" + id);

        defaultCursoShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultCursoShouldNotBeFound("id.greaterThan=" + id);

        defaultCursoShouldBeFound("id.lessThanOrEqual=" + id);
        defaultCursoShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllCursosByCursoIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso equals to DEFAULT_CURSO
        defaultCursoShouldBeFound("curso.equals=" + DEFAULT_CURSO);

        // Get all the cursoList where curso equals to UPDATED_CURSO
        defaultCursoShouldNotBeFound("curso.equals=" + UPDATED_CURSO);
    }

    @Test
    @Transactional
    void getAllCursosByCursoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso not equals to DEFAULT_CURSO
        defaultCursoShouldNotBeFound("curso.notEquals=" + DEFAULT_CURSO);

        // Get all the cursoList where curso not equals to UPDATED_CURSO
        defaultCursoShouldBeFound("curso.notEquals=" + UPDATED_CURSO);
    }

    @Test
    @Transactional
    void getAllCursosByCursoIsInShouldWork() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso in DEFAULT_CURSO or UPDATED_CURSO
        defaultCursoShouldBeFound("curso.in=" + DEFAULT_CURSO + "," + UPDATED_CURSO);

        // Get all the cursoList where curso equals to UPDATED_CURSO
        defaultCursoShouldNotBeFound("curso.in=" + UPDATED_CURSO);
    }

    @Test
    @Transactional
    void getAllCursosByCursoIsNullOrNotNull() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso is not null
        defaultCursoShouldBeFound("curso.specified=true");

        // Get all the cursoList where curso is null
        defaultCursoShouldNotBeFound("curso.specified=false");
    }

    @Test
    @Transactional
    void getAllCursosByCursoContainsSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso contains DEFAULT_CURSO
        defaultCursoShouldBeFound("curso.contains=" + DEFAULT_CURSO);

        // Get all the cursoList where curso contains UPDATED_CURSO
        defaultCursoShouldNotBeFound("curso.contains=" + UPDATED_CURSO);
    }

    @Test
    @Transactional
    void getAllCursosByCursoNotContainsSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where curso does not contain DEFAULT_CURSO
        defaultCursoShouldNotBeFound("curso.doesNotContain=" + DEFAULT_CURSO);

        // Get all the cursoList where curso does not contain UPDATED_CURSO
        defaultCursoShouldBeFound("curso.doesNotContain=" + UPDATED_CURSO);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH equals to DEFAULT_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.equals=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH equals to UPDATED_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.equals=" + UPDATED_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH not equals to DEFAULT_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.notEquals=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH not equals to UPDATED_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.notEquals=" + UPDATED_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsInShouldWork() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH in DEFAULT_DURACAO_CH or UPDATED_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.in=" + DEFAULT_DURACAO_CH + "," + UPDATED_DURACAO_CH);

        // Get all the cursoList where duracaoCH equals to UPDATED_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.in=" + UPDATED_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsNullOrNotNull() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH is not null
        defaultCursoShouldBeFound("duracaoCH.specified=true");

        // Get all the cursoList where duracaoCH is null
        defaultCursoShouldNotBeFound("duracaoCH.specified=false");
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH is greater than or equal to DEFAULT_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.greaterThanOrEqual=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH is greater than or equal to UPDATED_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.greaterThanOrEqual=" + UPDATED_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH is less than or equal to DEFAULT_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.lessThanOrEqual=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH is less than or equal to SMALLER_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.lessThanOrEqual=" + SMALLER_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsLessThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH is less than DEFAULT_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.lessThan=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH is less than UPDATED_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.lessThan=" + UPDATED_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDuracaoCHIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where duracaoCH is greater than DEFAULT_DURACAO_CH
        defaultCursoShouldNotBeFound("duracaoCH.greaterThan=" + DEFAULT_DURACAO_CH);

        // Get all the cursoList where duracaoCH is greater than SMALLER_DURACAO_CH
        defaultCursoShouldBeFound("duracaoCH.greaterThan=" + SMALLER_DURACAO_CH);
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao equals to DEFAULT_DESCRICAO
        defaultCursoShouldBeFound("descricao.equals=" + DEFAULT_DESCRICAO);

        // Get all the cursoList where descricao equals to UPDATED_DESCRICAO
        defaultCursoShouldNotBeFound("descricao.equals=" + UPDATED_DESCRICAO);
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao not equals to DEFAULT_DESCRICAO
        defaultCursoShouldNotBeFound("descricao.notEquals=" + DEFAULT_DESCRICAO);

        // Get all the cursoList where descricao not equals to UPDATED_DESCRICAO
        defaultCursoShouldBeFound("descricao.notEquals=" + UPDATED_DESCRICAO);
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoIsInShouldWork() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao in DEFAULT_DESCRICAO or UPDATED_DESCRICAO
        defaultCursoShouldBeFound("descricao.in=" + DEFAULT_DESCRICAO + "," + UPDATED_DESCRICAO);

        // Get all the cursoList where descricao equals to UPDATED_DESCRICAO
        defaultCursoShouldNotBeFound("descricao.in=" + UPDATED_DESCRICAO);
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoIsNullOrNotNull() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao is not null
        defaultCursoShouldBeFound("descricao.specified=true");

        // Get all the cursoList where descricao is null
        defaultCursoShouldNotBeFound("descricao.specified=false");
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoContainsSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao contains DEFAULT_DESCRICAO
        defaultCursoShouldBeFound("descricao.contains=" + DEFAULT_DESCRICAO);

        // Get all the cursoList where descricao contains UPDATED_DESCRICAO
        defaultCursoShouldNotBeFound("descricao.contains=" + UPDATED_DESCRICAO);
    }

    @Test
    @Transactional
    void getAllCursosByDescricaoNotContainsSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where descricao does not contain DEFAULT_DESCRICAO
        defaultCursoShouldNotBeFound("descricao.doesNotContain=" + DEFAULT_DESCRICAO);

        // Get all the cursoList where descricao does not contain UPDATED_DESCRICAO
        defaultCursoShouldBeFound("descricao.doesNotContain=" + UPDATED_DESCRICAO);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor equals to DEFAULT_VALOR
        defaultCursoShouldBeFound("valor.equals=" + DEFAULT_VALOR);

        // Get all the cursoList where valor equals to UPDATED_VALOR
        defaultCursoShouldNotBeFound("valor.equals=" + UPDATED_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor not equals to DEFAULT_VALOR
        defaultCursoShouldNotBeFound("valor.notEquals=" + DEFAULT_VALOR);

        // Get all the cursoList where valor not equals to UPDATED_VALOR
        defaultCursoShouldBeFound("valor.notEquals=" + UPDATED_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsInShouldWork() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor in DEFAULT_VALOR or UPDATED_VALOR
        defaultCursoShouldBeFound("valor.in=" + DEFAULT_VALOR + "," + UPDATED_VALOR);

        // Get all the cursoList where valor equals to UPDATED_VALOR
        defaultCursoShouldNotBeFound("valor.in=" + UPDATED_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsNullOrNotNull() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor is not null
        defaultCursoShouldBeFound("valor.specified=true");

        // Get all the cursoList where valor is null
        defaultCursoShouldNotBeFound("valor.specified=false");
    }

    @Test
    @Transactional
    void getAllCursosByValorIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor is greater than or equal to DEFAULT_VALOR
        defaultCursoShouldBeFound("valor.greaterThanOrEqual=" + DEFAULT_VALOR);

        // Get all the cursoList where valor is greater than or equal to UPDATED_VALOR
        defaultCursoShouldNotBeFound("valor.greaterThanOrEqual=" + UPDATED_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor is less than or equal to DEFAULT_VALOR
        defaultCursoShouldBeFound("valor.lessThanOrEqual=" + DEFAULT_VALOR);

        // Get all the cursoList where valor is less than or equal to SMALLER_VALOR
        defaultCursoShouldNotBeFound("valor.lessThanOrEqual=" + SMALLER_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsLessThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor is less than DEFAULT_VALOR
        defaultCursoShouldNotBeFound("valor.lessThan=" + DEFAULT_VALOR);

        // Get all the cursoList where valor is less than UPDATED_VALOR
        defaultCursoShouldBeFound("valor.lessThan=" + UPDATED_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByValorIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where valor is greater than DEFAULT_VALOR
        defaultCursoShouldNotBeFound("valor.greaterThan=" + DEFAULT_VALOR);

        // Get all the cursoList where valor is greater than SMALLER_VALOR
        defaultCursoShouldBeFound("valor.greaterThan=" + SMALLER_VALOR);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao equals to DEFAULT_CRIACAO
        defaultCursoShouldBeFound("criacao.equals=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao equals to UPDATED_CRIACAO
        defaultCursoShouldNotBeFound("criacao.equals=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsNotEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao not equals to DEFAULT_CRIACAO
        defaultCursoShouldNotBeFound("criacao.notEquals=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao not equals to UPDATED_CRIACAO
        defaultCursoShouldBeFound("criacao.notEquals=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsInShouldWork() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao in DEFAULT_CRIACAO or UPDATED_CRIACAO
        defaultCursoShouldBeFound("criacao.in=" + DEFAULT_CRIACAO + "," + UPDATED_CRIACAO);

        // Get all the cursoList where criacao equals to UPDATED_CRIACAO
        defaultCursoShouldNotBeFound("criacao.in=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsNullOrNotNull() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao is not null
        defaultCursoShouldBeFound("criacao.specified=true");

        // Get all the cursoList where criacao is null
        defaultCursoShouldNotBeFound("criacao.specified=false");
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao is greater than or equal to DEFAULT_CRIACAO
        defaultCursoShouldBeFound("criacao.greaterThanOrEqual=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao is greater than or equal to UPDATED_CRIACAO
        defaultCursoShouldNotBeFound("criacao.greaterThanOrEqual=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao is less than or equal to DEFAULT_CRIACAO
        defaultCursoShouldBeFound("criacao.lessThanOrEqual=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao is less than or equal to SMALLER_CRIACAO
        defaultCursoShouldNotBeFound("criacao.lessThanOrEqual=" + SMALLER_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsLessThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao is less than DEFAULT_CRIACAO
        defaultCursoShouldNotBeFound("criacao.lessThan=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao is less than UPDATED_CRIACAO
        defaultCursoShouldBeFound("criacao.lessThan=" + UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByCriacaoIsGreaterThanSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        // Get all the cursoList where criacao is greater than DEFAULT_CRIACAO
        defaultCursoShouldNotBeFound("criacao.greaterThan=" + DEFAULT_CRIACAO);

        // Get all the cursoList where criacao is greater than SMALLER_CRIACAO
        defaultCursoShouldBeFound("criacao.greaterThan=" + SMALLER_CRIACAO);
    }

    @Test
    @Transactional
    void getAllCursosByProfessorIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);
        Professor professor;
        if (TestUtil.findAll(em, Professor.class).isEmpty()) {
            professor = ProfessorResourceIT.createEntity(em);
            em.persist(professor);
            em.flush();
        } else {
            professor = TestUtil.findAll(em, Professor.class).get(0);
        }
        em.persist(professor);
        em.flush();
        curso.setProfessor(professor);
        cursoRepository.saveAndFlush(curso);
        Long professorId = professor.getId();

        // Get all the cursoList where professor equals to professorId
        defaultCursoShouldBeFound("professorId.equals=" + professorId);

        // Get all the cursoList where professor equals to (professorId + 1)
        defaultCursoShouldNotBeFound("professorId.equals=" + (professorId + 1));
    }

    @Test
    @Transactional
    void getAllCursosByUsuarioIsEqualToSomething() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);
        Usuario usuario;
        if (TestUtil.findAll(em, Usuario.class).isEmpty()) {
            usuario = UsuarioResourceIT.createEntity(em);
            em.persist(usuario);
            em.flush();
        } else {
            usuario = TestUtil.findAll(em, Usuario.class).get(0);
        }
        em.persist(usuario);
        em.flush();
        curso.setUsuario(usuario);
        cursoRepository.saveAndFlush(curso);
        Long usuarioId = usuario.getId();

        // Get all the cursoList where usuario equals to usuarioId
        defaultCursoShouldBeFound("usuarioId.equals=" + usuarioId);

        // Get all the cursoList where usuario equals to (usuarioId + 1)
        defaultCursoShouldNotBeFound("usuarioId.equals=" + (usuarioId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultCursoShouldBeFound(String filter) throws Exception {
        restCursoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(curso.getId().intValue())))
            .andExpect(jsonPath("$.[*].curso").value(hasItem(DEFAULT_CURSO)))
            .andExpect(jsonPath("$.[*].duracaoCH").value(hasItem(DEFAULT_DURACAO_CH.doubleValue())))
            .andExpect(jsonPath("$.[*].descricao").value(hasItem(DEFAULT_DESCRICAO)))
            .andExpect(jsonPath("$.[*].valor").value(hasItem(DEFAULT_VALOR.doubleValue())))
            .andExpect(jsonPath("$.[*].criacao").value(hasItem(sameInstant(DEFAULT_CRIACAO))));

        // Check, that the count call also returns 1
        restCursoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultCursoShouldNotBeFound(String filter) throws Exception {
        restCursoMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restCursoMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingCurso() throws Exception {
        // Get the curso
        restCursoMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewCurso() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();

        // Update the curso
        Curso updatedCurso = cursoRepository.findById(curso.getId()).get();
        // Disconnect from session so that the updates on updatedCurso are not directly saved in db
        em.detach(updatedCurso);
        updatedCurso
            .curso(UPDATED_CURSO)
            .duracaoCH(UPDATED_DURACAO_CH)
            .descricao(UPDATED_DESCRICAO)
            .valor(UPDATED_VALOR)
            .criacao(UPDATED_CRIACAO);
        CursoDTO cursoDTO = cursoMapper.toDto(updatedCurso);

        restCursoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cursoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cursoDTO))
            )
            .andExpect(status().isOk());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
        Curso testCurso = cursoList.get(cursoList.size() - 1);
        assertThat(testCurso.getCurso()).isEqualTo(UPDATED_CURSO);
        assertThat(testCurso.getDuracaoCH()).isEqualTo(UPDATED_DURACAO_CH);
        assertThat(testCurso.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testCurso.getValor()).isEqualTo(UPDATED_VALOR);
        assertThat(testCurso.getCriacao()).isEqualTo(UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void putNonExistingCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, cursoDTO.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cursoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(cursoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(cursoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCursoWithPatch() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();

        // Update the curso using partial update
        Curso partialUpdatedCurso = new Curso();
        partialUpdatedCurso.setId(curso.getId());

        partialUpdatedCurso.curso(UPDATED_CURSO).duracaoCH(UPDATED_DURACAO_CH).descricao(UPDATED_DESCRICAO);

        restCursoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCurso.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCurso))
            )
            .andExpect(status().isOk());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
        Curso testCurso = cursoList.get(cursoList.size() - 1);
        assertThat(testCurso.getCurso()).isEqualTo(UPDATED_CURSO);
        assertThat(testCurso.getDuracaoCH()).isEqualTo(UPDATED_DURACAO_CH);
        assertThat(testCurso.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testCurso.getValor()).isEqualTo(DEFAULT_VALOR);
        assertThat(testCurso.getCriacao()).isEqualTo(DEFAULT_CRIACAO);
    }

    @Test
    @Transactional
    void fullUpdateCursoWithPatch() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();

        // Update the curso using partial update
        Curso partialUpdatedCurso = new Curso();
        partialUpdatedCurso.setId(curso.getId());

        partialUpdatedCurso
            .curso(UPDATED_CURSO)
            .duracaoCH(UPDATED_DURACAO_CH)
            .descricao(UPDATED_DESCRICAO)
            .valor(UPDATED_VALOR)
            .criacao(UPDATED_CRIACAO);

        restCursoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCurso.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedCurso))
            )
            .andExpect(status().isOk());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
        Curso testCurso = cursoList.get(cursoList.size() - 1);
        assertThat(testCurso.getCurso()).isEqualTo(UPDATED_CURSO);
        assertThat(testCurso.getDuracaoCH()).isEqualTo(UPDATED_DURACAO_CH);
        assertThat(testCurso.getDescricao()).isEqualTo(UPDATED_DESCRICAO);
        assertThat(testCurso.getValor()).isEqualTo(UPDATED_VALOR);
        assertThat(testCurso.getCriacao()).isEqualTo(UPDATED_CRIACAO);
    }

    @Test
    @Transactional
    void patchNonExistingCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, cursoDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cursoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(cursoDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCurso() throws Exception {
        int databaseSizeBeforeUpdate = cursoRepository.findAll().size();
        curso.setId(count.incrementAndGet());

        // Create the Curso
        CursoDTO cursoDTO = cursoMapper.toDto(curso);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCursoMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(cursoDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Curso in the database
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCurso() throws Exception {
        // Initialize the database
        cursoRepository.saveAndFlush(curso);

        int databaseSizeBeforeDelete = cursoRepository.findAll().size();

        // Delete the curso
        restCursoMockMvc
            .perform(delete(ENTITY_API_URL_ID, curso.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Curso> cursoList = cursoRepository.findAll();
        assertThat(cursoList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
