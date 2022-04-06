package com.bd2cursos.myapp.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Curso.
 */
@Entity
@Table(name = "curso")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Curso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "curso")
    private String curso;

    @Column(name = "duracao_ch")
    private Double duracaoCH;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "valor")
    private Double valor;

    @Column(name = "criacao")
    private ZonedDateTime criacao;

    @ManyToOne
    private Professor professor;

    @ManyToOne
    private Usuario usuario;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Curso id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurso() {
        return this.curso;
    }

    public Curso curso(String curso) {
        this.setCurso(curso);
        return this;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Double getDuracaoCH() {
        return this.duracaoCH;
    }

    public Curso duracaoCH(Double duracaoCH) {
        this.setDuracaoCH(duracaoCH);
        return this;
    }

    public void setDuracaoCH(Double duracaoCH) {
        this.duracaoCH = duracaoCH;
    }

    public String getDescricao() {
        return this.descricao;
    }

    public Curso descricao(String descricao) {
        this.setDescricao(descricao);
        return this;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return this.valor;
    }

    public Curso valor(Double valor) {
        this.setValor(valor);
        return this;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public ZonedDateTime getCriacao() {
        return this.criacao;
    }

    public Curso criacao(ZonedDateTime criacao) {
        this.setCriacao(criacao);
        return this;
    }

    public void setCriacao(ZonedDateTime criacao) {
        this.criacao = criacao;
    }

    public Professor getProfessor() {
        return this.professor;
    }

    public void setProfessor(Professor professor) {
        this.professor = professor;
    }

    public Curso professor(Professor professor) {
        this.setProfessor(professor);
        return this;
    }

    public Usuario getUsuario() {
        return this.usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Curso usuario(Usuario usuario) {
        this.setUsuario(usuario);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Curso)) {
            return false;
        }
        return id != null && id.equals(((Curso) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Curso{" +
            "id=" + getId() +
            ", curso='" + getCurso() + "'" +
            ", duracaoCH=" + getDuracaoCH() +
            ", descricao='" + getDescricao() + "'" +
            ", valor=" + getValor() +
            ", criacao='" + getCriacao() + "'" +
            "}";
    }
}
