package com.helpdeskpro.helpdesk.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tipo tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne
    @JoinColumn(name = "tecnico_id", nullable = true)
    private Usuario tecnico;

    // Relación con Mensaje, elimina en cascada y evita recursión infinita
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Mensaje> mensajes;

    public enum Tipo {
        SOFTWARE,
        HARDWARE,
        RED,
        SIN_CLASIFICAR
    }

    public enum Estado {
        PENDIENTE,
        EN_PROCESO,
        RESUELTO
    }

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
        if (estado == null) estado = Estado.PENDIENTE;
    }

    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Usuario getTecnico() {
        return tecnico;
    }

    public void setTecnico(Usuario tecnico) {
        this.tecnico = tecnico;
    }

    public List<Mensaje> getMensajes() {
        return mensajes;
    }

    public void setMensajes(List<Mensaje> mensajes) {
        this.mensajes = mensajes;
    }
}
