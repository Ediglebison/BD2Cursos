package com.bd2cursos.myapp.service.mapper;

import com.bd2cursos.myapp.domain.Compra;
import com.bd2cursos.myapp.domain.Curso;
import com.bd2cursos.myapp.domain.Usuario;
import com.bd2cursos.myapp.service.dto.CompraDTO;
import com.bd2cursos.myapp.service.dto.CursoDTO;
import com.bd2cursos.myapp.service.dto.UsuarioDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Compra} and its DTO {@link CompraDTO}.
 */
@Mapper(componentModel = "spring")
public interface CompraMapper extends EntityMapper<CompraDTO, Compra> {
    @Mapping(target = "curso", source = "curso", qualifiedByName = "cursoTitulo")
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "usuarioNome")
    CompraDTO toDto(Compra s);

    @Named("cursoTitulo")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "titulo", source = "titulo")
    CursoDTO toDtoCursoTitulo(Curso curso);

    @Named("usuarioNome")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nome", source = "nome")
    UsuarioDTO toDtoUsuarioNome(Usuario usuario);
}
