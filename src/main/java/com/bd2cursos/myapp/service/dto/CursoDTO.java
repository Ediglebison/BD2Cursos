package com.bd2cursos.myapp.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {@link com.bd2cursos.myapp.domain.Curso} entity.
 */
public class CursoDTO implements Serializable {

    private Long id;

    private String curso;

    private Double duracaoCH;

    private String descricao;

    private Double valor;

    private ZonedDateTime criacao;

    private ProfessorDTO professor;

    private UsuarioDTO usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public Double getDuracaoCH() {
        return duracaoCH;
    }

    public void setDuracaoCH(Double duracaoCH) {
        this.duracaoCH = duracaoCH;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public ZonedDateTime getCriacao() {
        return criacao;
    }

    public void setCriacao(ZonedDateTime criacao) {
        this.criacao = criacao;
    }

    public ProfessorDTO getProfessor() {
        return professor;
    }

    public void setProfessor(ProfessorDTO professor) {
        this.professor = professor;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CursoDTO)) {
            return false;
        }

        CursoDTO cursoDTO = (CursoDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, cursoDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CursoDTO{" +
            "id=" + getId() +
            ", curso='" + getCurso() + "'" +
            ", duracaoCH=" + getDuracaoCH() +
            ", descricao='" + getDescricao() + "'" +
            ", valor=" + getValor() +
            ", criacao='" + getCriacao() + "'" +
            ", professor=" + getProfessor() +
            ", usuario=" + getUsuario() +
            "}";
    }
}
