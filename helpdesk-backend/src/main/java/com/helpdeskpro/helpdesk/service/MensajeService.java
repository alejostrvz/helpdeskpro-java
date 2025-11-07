package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Mensaje;
import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.repository.MensajeRepository;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MensajeService {

    private final MensajeRepository mensajeRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public MensajeService(MensajeRepository mensajeRepository, TicketRepository ticketRepository) {
        this.mensajeRepository = mensajeRepository;
        this.ticketRepository = ticketRepository;
    }

    // Crear mensaje
    public Mensaje crearMensaje(Mensaje mensaje) {
        return mensajeRepository.save(mensaje);
    }

    // Listar mensajes por ticket
    public List<Mensaje> obtenerMensajesPorTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null) {
            return mensajeRepository.findByTicket(ticket);
        }
        return List.of(); // retornar lista vacía si no existe el ticket
    }
}
