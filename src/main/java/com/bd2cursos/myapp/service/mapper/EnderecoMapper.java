package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Endereco;
import com.bd2cursos.myapp.service.dto.EnderecoDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Endereco} and its DTO {@link EnderecoDTO}.
 */
@Mapper(componentModel = "spring", uses = { UsuarioMapper.class, ProfessorMapper.class })
public interface EnderecoMapper extends EntityMapper<EnderecoDTO, Endereco> {
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "id")
    @Mapping(target = "professor", source = "professor", qualifiedByName = "id")
    EnderecoDTO toDto(Endereco s);
}
