package io.lrsystem.ServiceLog.mapper;

import io.lrsystem.ServiceLog.dto.UsuarioCreateUpdateDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.model.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    UsuarioResponseDTO usuariroToDto(Usuario usuario);

    List<UsuarioResponseDTO> usuariosToDto(List<Usuario> usuarios);

    @Mapping(target = "id", ignore = true)
    Usuario usuarioToEntity(UsuarioCreateUpdateDTO usuarioCreateUpdateDTO);

    Usuario toEntity(UsuarioResponseDTO usuarioResponseDTO);

    UsuarioCreateUpdateDTO usuarioToDto(Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizar(@MappingTarget Usuario usuario, UsuarioCreateUpdateDTO dto);

}
