package com.helpdeskpro.helpdesk.controller;

import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import com.helpdeskpro.helpdesk.service.CorreoSalidaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CorreoSalidaService correoSalidaService;

    // Obtener todos los tickets (ADMIN o TECNICO)
    @GetMapping
    public List<Ticket> getAllTickets(@RequestParam Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        if (usuario.getRol() != Usuario.Rol.ADMIN && usuario.getRol() != Usuario.Rol.TECNICO) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }

        return ticketRepository.findAll();
    }

    //Obtener ticket por ID
    @GetMapping("/{id}")
    public Ticket getTicketById(@RequestParam Long usuarioId, @PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(id).orElseThrow();

        boolean autorizado = usuario.getRol() == Usuario.Rol.ADMIN ||
                (usuario.getRol() == Usuario.Rol.TECNICO && ticket.getTecnico() != null &&
                        ticket.getTecnico().getId().equals(usuario.getId()));

        if (!autorizado)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver este ticket");

        return ticket;
    }

    // Crear ticket manualmente (ADMIN o TECNICO)
    @PostMapping
    public Ticket createTicket(@RequestParam Long usuarioId, @RequestBody Ticket ticket) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        if (usuario.getRol() == Usuario.Rol.ADMIN || usuario.getRol() == Usuario.Rol.TECNICO) {
            return ticketRepository.save(ticket);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
    }

    //Actualizar ticket (ADMIN o TECNICO asignado)
    @PutMapping("/{id}")
    public Ticket updateTicket(@RequestParam Long usuarioId, @PathVariable Long id, @RequestBody Ticket update) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(id).orElseThrow();

        if (usuario.getRol() == Usuario.Rol.ADMIN ||
                (usuario.getRol() == Usuario.Rol.TECNICO && ticket.getTecnico() != null &&
                        ticket.getTecnico().getId().equals(usuario.getId()))) {

            if (update.getEstado() != null) ticket.setEstado(update.getEstado());
            if (update.getTipo() != null) ticket.setTipo(update.getTipo());
            return ticketRepository.save(ticket);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para actualizar este ticket");
    }

    // Eliminar ticket (ADMIN o TECNICO asignado)
    @DeleteMapping("/{id}")
    public void deleteTicket(@RequestParam Long usuarioId, @PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(id).orElseThrow();

        if (usuario.getRol() == Usuario.Rol.ADMIN ||
                (usuario.getRol() == Usuario.Rol.TECNICO && ticket.getTecnico() != null &&
                        ticket.getTecnico().getId().equals(usuario.getId()))) {
            ticketRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para eliminar este ticket");
        }
    }

    //Buscar tickets por cliente (solo ADMIN)
    @GetMapping("/cliente/{correo}")
    public List<Ticket> getTicketsByCliente(@RequestParam Long usuarioId, @PathVariable String correo) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        if (usuario.getRol() != Usuario.Rol.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede ver tickets de clientes");
        }

        Optional<Usuario> cliente = usuarioRepository.findByCorreo(correo);
        return cliente.map(ticketRepository::findByCliente).orElse(List.of());
    }

    // Filtrar por estado (solo ADMIN o TECNICO)
    @GetMapping("/estado/{estado}")
    public List<Ticket> getTicketsByEstado(@RequestParam Long usuarioId, @PathVariable Ticket.Estado estado) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        if (usuario.getRol() == Usuario.Rol.ADMIN || usuario.getRol() == Usuario.Rol.TECNICO) {
            return ticketRepository.findByEstado(estado);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }

    //Filtrar por tipo (solo ADMIN o TECNICO)
    @GetMapping("/tipo/{tipo}")
    public List<Ticket> getTicketsByTipo(@RequestParam Long usuarioId, @PathVariable Ticket.Tipo tipo) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        if (usuario.getRol() == Usuario.Rol.ADMIN || usuario.getRol() == Usuario.Rol.TECNICO) {
            return ticketRepository.findByTipo(tipo);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
    }

    // Actualizar estado del ticket
    @PutMapping("/{id}/estado")
    public Ticket actualizarEstado(@RequestParam Long usuarioId, @PathVariable Long id, @RequestParam Ticket.Estado nuevoEstado) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        Ticket ticket = ticketRepository.findById(id).orElseThrow();

        boolean autorizado = usuario.getRol() == Usuario.Rol.ADMIN ||
                (usuario.getRol() == Usuario.Rol.TECNICO && ticket.getTecnico() != null &&
                        ticket.getTecnico().getId().equals(usuario.getId()));

        if (!autorizado)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para cambiar el estado");

        ticket.setEstado(nuevoEstado);
        ticketRepository.save(ticket);

        //  Si se cierra el ticket, enviar correo HTML al cliente
        if (nuevoEstado == Ticket.Estado.RESUELTO) {
            correoSalidaService.enviarCorreoCierreTicket(ticket);
        }

        return ticket;
    }

    //  Cerrar ticket
    @PutMapping("/{id}/cerrar")
    public Ticket cerrarTicket(@RequestParam Long usuarioId, @PathVariable Long id) {
        return actualizarEstado(usuarioId, id, Ticket.Estado.RESUELTO);
    }

    // Listar tickets por técnico asignado
    @GetMapping("/tecnico/{id}")
    public List<Ticket> getTicketsByTecnico(@RequestParam Long usuarioId, @PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();

        // Solo ADMIN o el técnico dueño pueden consultar sus tickets
        if (usuario.getRol() == Usuario.Rol.ADMIN ||
                (usuario.getRol() == Usuario.Rol.TECNICO && usuario.getId().equals(id))) {
            Usuario tecnico = usuarioRepository.findById(id).orElseThrow();
            return ticketRepository.findByTecnico(tecnico);
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado para ver estos tickets");
    }
}
