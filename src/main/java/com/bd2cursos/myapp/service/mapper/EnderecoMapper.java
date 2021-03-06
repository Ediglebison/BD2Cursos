package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Endereco;
import com.bd2cursos.myapp.domain.Usuario;
import com.bd2cursos.myapp.service.dto.EnderecoDTO;
import com.bd2cursos.myapp.service.dto.UsuarioDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Endereco} and its DTO {@link EnderecoDTO}.
 */
@Mapper(componentModel = "spring")
public interface EnderecoMapper extends EntityMapper<EnderecoDTO, Endereco> {
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "usuarioId")
    EnderecoDTO toDto(Endereco s);

    @Named("usuarioId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UsuarioDTO toDtoUsuarioId(Usuario usuario);
}
