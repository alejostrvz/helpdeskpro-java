package com.helpdeskpro.helpdesk.repository;

import com.helpdeskpro.helpdesk.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por correo
    Optional<Usuario> findByCorreo(String correo);

    // Verificar si existe usuario por correo
    boolean existsByCorreo(String correo);

    List<Usuario> findByRol(Usuario.Rol rol);
}
