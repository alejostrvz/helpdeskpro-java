package com.helpdeskpro.helpdesk.controller;

import com.helpdeskpro.helpdesk.entity.Mensaje;
import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.MensajeRepository;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import com.helpdeskpro.helpdesk.service.CorreoSalidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/mensajes")
@CrossOrigin(origins = "*")
public class MensajeController {

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CorreoSalidaService correoSalidaService;

    // Obtener mensajes de un ticket
    @GetMapping("/ticket/{ticketId}")
    public List<Mensaje> getMensajesByTicket(@RequestParam Long usuarioId, @PathVariable Long ticketId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();

        // Solo ADMIN o TÉCNICO asignado pueden ver los mensajes
        if (usuario.getRol() != Usuario.Rol.ADMIN &&
                (usuario.getRol() != Usuario.Rol.TECNICO ||
                        ticket.getTecnico() == null ||
                        !ticket.getTecnico().getId().equals(usuario.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver los mensajes de este ticket");
        }

        return mensajeRepository.findByTicketIdOrderByFechaAsc(ticketId);
    }

    //  Enviar mensaje (solo ADMIN o TÉCNICO asignado)
    @PostMapping("/enviar")
    public Mensaje enviarMensaje(@RequestParam Long usuarioId, @RequestBody Mensaje mensaje) {
        Usuario autor = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(mensaje.getTicket().getId()).orElseThrow();

        // Validar permisos
        if (autor.getRol() != Usuario.Rol.ADMIN &&
                (autor.getRol() != Usuario.Rol.TECNICO ||
                        ticket.getTecnico() == null ||
                        !ticket.getTecnico().getId().equals(autor.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para enviar mensajes en este ticket");
        }

        // Validar si el ticket no está cerrado
        if (ticket.getEstado() == Ticket.Estado.RESUELTO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El ticket está cerrado y no acepta más mensajes");
        }

        // Crear y guardar el mensaje
        mensaje.setTicket(ticket);
        mensaje.setAutor(autor);
        mensaje.setFecha(LocalDateTime.now());
        Mensaje nuevo = mensajeRepository.save(mensaje);

        // Si el autor es un técnico, enviar correo al cliente
        if (autor.getRol() == Usuario.Rol.TECNICO && ticket.getCliente() != null) {
            correoSalidaService.enviarRespuestaAlCliente(ticket, mensaje.getTexto(), autor);
        }

        return nuevo;
    }
}
