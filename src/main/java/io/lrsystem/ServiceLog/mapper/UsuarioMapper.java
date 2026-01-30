package io.lrsystem.ServiceLog.mapper;

import io.lrsystem.ServiceLog.dto.UsuarioRequestDTO;
import io.lrsystem.ServiceLog.dto.UsuarioResponseDTO;
import io.lrsystem.ServiceLog.model.Usuario;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {


    UsuarioResponseDTO usuarioToDtoResp(Usuario usuario);

    List<UsuarioResponseDTO> usuariosToDto(List<Usuario> usuarios);

    @Mapping(target = "id", ignore = true)
    Usuario usuarioToEntity(UsuarioRequestDTO usuarioRequestDTO);

    Usuario toEntity(UsuarioResponseDTO usuarioResponseDTO);

    UsuarioRequestDTO usuarioToDto(Usuario usuario);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void atualizar(@MappingTarget Usuario usuario, UsuarioRequestDTO dto);



}
