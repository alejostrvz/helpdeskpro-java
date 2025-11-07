package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Registrar usuario (con hash)
    public Usuario registrarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword())); // encripta
        return usuarioRepository.save(usuario);
    }

    //  Verificar login
    public Usuario login(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return usuario; // retorno el usuario si las credenciales son correctas
    }
}
