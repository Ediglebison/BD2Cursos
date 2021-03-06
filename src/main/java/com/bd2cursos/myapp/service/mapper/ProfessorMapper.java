package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Professor;
import com.bd2cursos.myapp.service.dto.ProfessorDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Professor} and its DTO {@link ProfessorDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProfessorMapper extends EntityMapper<ProfessorDTO, Professor> {}
