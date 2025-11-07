package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import com.helpdeskpro.helpdesk.repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final MensajeRepository mensajeRepository;

    //  Constructor corrige la inicialización de ambos repositorios
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, TicketRepository ticketRepository, MensajeRepository mensajeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.mensajeRepository = mensajeRepository;
    }

    // Obtener todos los usuarios
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Obtener usuario por ID
    public Usuario obtenerUsuarioPorId(Long id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        return usuario.orElse(null);
    }

    // Obtener usuario por correo
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    // Eliminar usuario
    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminar mensajes del usuario antes de borrar
        mensajeRepository.deleteByUsuarioId(id);

        // Si el usuario es técnico, desasignar sus tickets
        if (usuario.getRol() == Usuario.Rol.TECNICO) {
            ticketRepository.desasignarTicketsDelTecnico(id);
        }

        // Si el usuario es cliente, eliminar todos sus tickets
        if (usuario.getRol() == Usuario.Rol.CLIENTE) {
            ticketRepository.eliminarTicketsDeCliente(id);
        }

        usuarioRepository.deleteById(id);
    }
}
