package com.helpdeskpro.helpdesk.scheduler;

import com.helpdeskpro.helpdesk.service.CorreoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CorreoScheduler {

    private final CorreoService correoService;

    @Autowired
    public CorreoScheduler(CorreoService correoService) {
        this.correoService = correoService;
    }

    // Cada 30 segundos revisar correos
    @Scheduled(fixedRate = 30000)
    public void ejecutarTareaRevisarCorreos() {
        System.out.println("Revisando correos entrantes...");
        correoService.revisarCorreos();
    }
}
