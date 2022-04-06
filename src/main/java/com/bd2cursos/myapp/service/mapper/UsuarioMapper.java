package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Usuario;
import com.bd2cursos.myapp.service.dto.UsuarioDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Usuario} and its DTO {@link UsuarioDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface UsuarioMapper extends EntityMapper<UsuarioDTO, Usuario> {
    @Named("id")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UsuarioDTO toDtoId(Usuario usuario);
}
