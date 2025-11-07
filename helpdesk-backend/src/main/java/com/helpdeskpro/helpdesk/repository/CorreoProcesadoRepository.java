package com.helpdeskpro.helpdesk.repository;

import com.helpdeskpro.helpdesk.entity.CorreoProcesado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CorreoProcesadoRepository extends JpaRepository<CorreoProcesado, Long> {
    Optional<CorreoProcesado> findByMessageId(String messageId);
}
