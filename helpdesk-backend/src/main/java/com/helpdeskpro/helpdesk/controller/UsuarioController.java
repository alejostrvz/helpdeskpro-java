package com.helpdeskpro.helpdesk.controller;

import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.service.UsuarioService;
import com.helpdeskpro.helpdesk.service.AuthService;
import com.helpdeskpro.helpdesk.config.RoleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    // Crear usuario (registro libre o automático)
    @PostMapping
    public Usuario crearUsuario(@RequestBody Usuario usuario) {
        return authService.registrarUsuario(usuario);
    }

    // Listar todos los usuarios (solo ADMIN)
    @GetMapping
    public List<Usuario> listarUsuarios(@RequestParam Long adminId) {
        Usuario admin = usuarioService.obtenerUsuarioPorId(adminId);
        RoleValidator.requireAdmin(admin);
        return usuarioService.obtenerTodosUsuarios();
    }

    // Obtener usuario por ID (ADMIN o el mismo usuario)
    @GetMapping("/{id}")
    public Usuario obtenerUsuario(@RequestParam Long solicitanteId, @PathVariable Long id) {
        Usuario solicitante = usuarioService.obtenerUsuarioPorId(solicitanteId);
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario == null) throw new RuntimeException("Usuario no encontrado con ID: " + id);

        if (!solicitante.getRol().equals(Usuario.Rol.ADMIN) &&
                !solicitante.getId().equals(usuario.getId())) {
            throw new RuntimeException("No autorizado para ver este usuario");
        }

        return usuario;
    }

    // Crear técnico (solo ADMIN)
    @PostMapping("/crear-tecnico")
    public Usuario crearTecnico(@RequestParam Long adminId, @RequestBody Usuario tecnico) {
        Usuario admin = usuarioService.obtenerUsuarioPorId(adminId);
        RoleValidator.requireAdmin(admin);

        tecnico.setRol(Usuario.Rol.TECNICO);
        return authService.registrarUsuario(tecnico);
    }

    // Eliminar usuario (solo ADMIN)
    @DeleteMapping("/{id}/eliminar")
    public void eliminarUsuario(@RequestParam Long adminId, @PathVariable Long id) {
        Usuario admin = usuarioService.obtenerUsuarioPorId(adminId);
        RoleValidator.requireAdmin(admin);
        usuarioService.eliminarUsuario(id);
    }
}
