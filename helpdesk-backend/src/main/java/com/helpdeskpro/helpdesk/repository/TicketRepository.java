package com.helpdeskpro.helpdesk.repository;

import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCliente(Usuario cliente);
    List<Ticket> findByEstado(Ticket.Estado estado);
    List<Ticket> findByTipo(Ticket.Tipo tipo);
    long countByTecnicoAndEstadoNot(Usuario tecnico, Ticket.Estado estado);
    List<Ticket> findByTecnico(Usuario tecnico);
    long countByTecnicoAndEstado(Usuario tecnico, Ticket.Estado estado);

    @Modifying
    @Transactional
    @Query("UPDATE Ticket t SET t.tecnico = NULL WHERE t.tecnico.id = :tecnicoId")
    void desasignarTicketsDelTecnico(Long tecnicoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Ticket t WHERE t.cliente.id = :clienteId")
    void eliminarTicketsDeCliente(Long clienteId);
}
