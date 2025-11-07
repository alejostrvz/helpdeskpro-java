package com.helpdeskpro.helpdesk.controller;

import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    // Registro con hash
    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario) {
        return authService.registrarUsuario(usuario);
    }

    // Login (verificación de credenciales)
    @PostMapping("/login")
    public Usuario login(@RequestBody Usuario loginRequest) {
        return authService.login(loginRequest.getCorreo(), loginRequest.getPassword());
    }
}
