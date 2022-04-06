package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Curso;
import com.bd2cursos.myapp.service.dto.CursoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Curso} and its DTO {@link CursoDTO}.
 */
@Mapper(componentModel = "spring", uses = { ProfessorMapper.class, UsuarioMapper.class })
public interface CursoMapper extends EntityMapper<CursoDTO, Curso> {
    @Mapping(target = "professor", source = "professor", qualifiedByName = "id")
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "id")
    CursoDTO toDto(Curso s);
}
