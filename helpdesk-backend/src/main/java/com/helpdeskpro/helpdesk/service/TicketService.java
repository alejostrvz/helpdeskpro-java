package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository, UsuarioRepository usuarioRepository) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Crear un ticket manual o desde correo
    public Ticket crearTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    // Actualizar estado de ticket
    public Ticket actualizarEstado(Long ticketId, Ticket.Estado estado) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
        if(optionalTicket.isPresent()) {
            Ticket ticket = optionalTicket.get();
            ticket.setEstado(estado);
            return ticketRepository.save(ticket);
        }
        return null;
    }

    // Asignar técnico
    public Ticket asignarTecnico(Long ticketId, Usuario tecnico) {
        Optional<Ticket> optionalTicket = ticketRepository.findById(ticketId);
        if(optionalTicket.isPresent()) {
            Ticket ticket = optionalTicket.get();
            ticket.setTecnico(tecnico);
            return ticketRepository.save(ticket);
        }
        return null;
    }

    // Buscar tickets por cliente
    public List<Ticket> obtenerTicketsPorCliente(Usuario cliente) {
        return ticketRepository.findByCliente(cliente);
    }

    // Buscar tickets por técnico
    public List<Ticket> obtenerTicketsPorTecnico(Usuario tecnico) {
        return ticketRepository.findByTecnico(tecnico);
    }

    // Buscar tickets por estado
    public List<Ticket> obtenerTicketsPorEstado(Ticket.Estado estado) {
        return ticketRepository.findByEstado(estado);
    }

    // Buscar tickets por tipo
    public List<Ticket> obtenerTicketsPorTipo(Ticket.Tipo tipo) {
        return ticketRepository.findByTipo(tipo);
    }



    // Obtener usuario por correo
    public Usuario obtenerUsuarioPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo).orElse(null);
    }

    // Crear usuario nuevo
    public Usuario crearUsuario(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Clasificar ticket automaticamente por palabras clave
    public Ticket.Tipo clasificarTicket(String texto) {
        texto = texto.toLowerCase();
        if (texto.contains("red") || texto.contains("internet") || texto.contains("wifi")) {
            return Ticket.Tipo.RED;
        } else if (texto.contains("hardware") || texto.contains("computadora") || texto.contains("monitor")) {
            return Ticket.Tipo.HARDWARE;
        } else if (texto.contains("software") || texto.contains("programa") || texto.contains("aplicación")) {
            return Ticket.Tipo.SOFTWARE;
        } else {
            return Ticket.Tipo.SIN_CLASIFICAR;
        }
    }
}
