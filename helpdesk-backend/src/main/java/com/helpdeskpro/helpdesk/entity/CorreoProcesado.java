package com.helpdeskpro.helpdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "correos_procesados")
public class CorreoProcesado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false)
    private String messageId;

    public CorreoProcesado() {}

    public CorreoProcesado(String messageId) {
        this.messageId = messageId;
    }

    public Long getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
