package com.helpdeskpro.helpdesk.repository;

import com.helpdeskpro.helpdesk.entity.Mensaje;
import com.helpdeskpro.helpdesk.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByTicket(Ticket ticket);
    List<Mensaje> findByTicketIdOrderByFechaAsc(Long ticketId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Mensaje m WHERE m.autor.id = :usuarioId")
    void deleteByUsuarioId(Long usuarioId);
}
