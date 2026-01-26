package io.lrsystem.ServiceLog.mapper;

import io.lrsystem.ServiceLog.dto.UsuarioCreateUpdateDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    UsuarioResponseDTO usuariroToDto(Usuario usuario);

    List<UsuarioResponseDTO> usuariosToDto(List<Usuario> usuarios);

    @Mapping(target = "id", ignore = true)
    Usuario usuarioToEntity(UsuarioCreateUpdateDTO usuarioCreateUpdateDTO);

    Usuario toEntity(UsuarioResponseDTO usuarioResponseDTO);

}
