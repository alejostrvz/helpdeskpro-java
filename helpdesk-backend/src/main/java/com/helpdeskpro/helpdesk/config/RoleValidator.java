package com.helpdeskpro.helpdesk.config;

import com.helpdeskpro.helpdesk.entity.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RoleValidator {

    public static void requireAdmin(Usuario usuario) {
        if (usuario == null || usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado: se requiere rol ADMIN");
        }
    }

    public static void requireTecnico(Usuario usuario) {
        if (usuario == null || usuario.getRol() != Usuario.Rol.TECNICO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado: se requiere rol TECNICO");
        }
    }

    public static void requireAdminOrTecnico(Usuario usuario) {
        if (usuario == null ||
                (usuario.getRol() != Usuario.Rol.ADMIN && usuario.getRol() != Usuario.Rol.TECNICO)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado: se requiere ADMIN o TECNICO");
        }
    }
}
