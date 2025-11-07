package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Mensaje;
import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.entity.CorreoProcesado;
import com.helpdeskpro.helpdesk.repository.MensajeRepository;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import com.helpdeskpro.helpdesk.repository.CorreoProcesadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeMessage;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class CorreoService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private CorreoProcesadoRepository correoProcesadoRepository;

    @Autowired
    private CorreoSalidaService correoSalidaService;

    public void revisarCorreos() {
        System.out.println("📨 Revisando correos entrantes...");

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props, null);

            Store store = session.getStore();
            store.connect("imap.gmail.com", "helpdeskprosoporte@gmail.com", "ycci ghbj orfr qmxt");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] mensajes = inbox.getMessages();

            for (Message mensaje : mensajes) {
                String messageId = ((MimeMessage) mensaje).getMessageID();

                // Evitar reprocesar correos ya guardados
                if (correoProcesadoRepository.findByMessageId(messageId).isPresent()) {
                    continue;
                }

                // Guardar registro del correo procesado
                correoProcesadoRepository.save(new CorreoProcesado(messageId));

                Address[] remitentes = mensaje.getFrom();
                if (remitentes == null || remitentes.length == 0) continue;

                final String correoRemitente = ((InternetAddress) remitentes[0]).getAddress();
                final String nombreRemitente = ((InternetAddress) remitentes[0]).getPersonal() != null
                        ? ((InternetAddress) remitentes[0]).getPersonal()
                        : "Usuario sin nombre";

                final String asunto = mensaje.getSubject() != null ? mensaje.getSubject() : "(Sin asunto)";
                final String contenido = obtenerTextoDeMensaje(mensaje);

                // Clasificación automática
                final Ticket.Tipo tipo = clasificarTipo(asunto + " " + contenido);

                // Buscar o crear usuario
                Usuario cliente = usuarioRepository.findByCorreo(correoRemitente).orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setCorreo(correoRemitente);
                    nuevo.setNombre(nombreRemitente);
                    nuevo.setPassword("1234");
                    nuevo.setRol(Usuario.Rol.CLIENTE);
                    return authService.registrarUsuario(nuevo);
                });

                //  Buscar si el correo pertenece a un hilo existente (RE:)
                Ticket ticketExistente = buscarTicketPorAsunto(asunto, correoRemitente);

                if (ticketExistente != null) {
                    // Agregar mensaje al ticket existente
                    Mensaje nuevoMensaje = new Mensaje();
                    nuevoMensaje.setTicket(ticketExistente);
                    nuevoMensaje.setAutor(cliente);
                    nuevoMensaje.setTexto(limpiarTextoCorreo(contenido));
                    mensajeRepository.save(nuevoMensaje);

                    // Actualizar fecha del ticket
                    ticketExistente.setFechaActualizacion(java.time.LocalDateTime.now());
                    ticketRepository.save(ticketExistente);

                    System.out.println("📩 Respuesta agregada al ticket existente ID: " + ticketExistente.getId());
                } else {
                    // Crear un nuevo ticket
                    Ticket ticket = new Ticket();
                    ticket.setTitulo(asunto);
                    ticket.setDescripcion(contenido);
                    ticket.setTipo(tipo);
                    ticket.setEstado(Ticket.Estado.PENDIENTE);
                    ticket.setCliente(cliente);

                    // Asignación automática de técnico
                    List<Usuario> tecnicos = usuarioRepository.findByRol(Usuario.Rol.TECNICO);
                    if (!tecnicos.isEmpty()) {
                        Usuario tecnicoAsignado = tecnicos.stream()
                                .min(Comparator.comparingLong(t ->
                                        ticketRepository.countByTecnicoAndEstadoNot(t, Ticket.Estado.RESUELTO)))
                                .orElse(tecnicos.get(0));

                        ticket.setTecnico(tecnicoAsignado);
                        System.out.println("👨‍🔧 Ticket asignado a técnico: " + tecnicoAsignado.getNombre());
                    } else {
                        System.out.println("⚠️ No hay técnicos disponibles para asignar el ticket.");
                    }

                    // Guardar ticket y mensaje
                    ticket = ticketRepository.save(ticket);

                    Mensaje nuevoMensaje = new Mensaje();
                    nuevoMensaje.setTicket(ticket);
                    nuevoMensaje.setAutor(cliente);
                    nuevoMensaje.setTexto(contenido);
                    mensajeRepository.save(nuevoMensaje);

                    //  Enviar confirmación al cliente
                    correoSalidaService.enviarConfirmacionCreacionTicket(ticket);

                    System.out.println(" Ticket nuevo creado desde correo de: " + nombreRemitente + " <" + correoRemitente + ">");
                }
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println(" Error revisando correos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //  Buscar ticket existente por asunto (detecta "RE:")
    private Ticket buscarTicketPorAsunto(String asunto, String correoCliente) {
        String asuntoLimpio = asunto.replaceAll("(?i)^re:\\s*", "").trim();

        return ticketRepository.findAll().stream()
                .filter(t ->
                        t.getCliente() != null &&
                                correoCliente.equalsIgnoreCase(t.getCliente().getCorreo()) &&
                                (t.getTitulo().equalsIgnoreCase(asunto) ||
                                        t.getTitulo().equalsIgnoreCase(asuntoLimpio)))
                .findFirst()
                .orElse(null);
    }

    private String obtenerTextoDeMensaje(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return part.getContent().toString();
        } else if (part.isMimeType("text/html")) {
            // Solo usamos HTML si no hay parte de texto plano
            return Jsoup.parse(part.getContent().toString()).text();
        } else if (part.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) part.getContent();

            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);

                // Busca primero la parte de texto plano
                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                }

                // Si no encuentra texto plano, toma el HTML y lo limpia
                if (bodyPart.isMimeType("text/html")) {
                    return Jsoup.parse(bodyPart.getContent().toString()).text();
                }
            }
        }
        return "";
    }


    private Ticket.Tipo clasificarTipo(String texto) {
        texto = texto.toLowerCase();
        if (Pattern.compile("red|wifi|conex").matcher(texto).find()) {
            return Ticket.Tipo.RED;
        } else if (Pattern.compile("impresora|hardware|perif").matcher(texto).find()) {
            return Ticket.Tipo.HARDWARE;
        } else if (Pattern.compile("software|app|sistema").matcher(texto).find()) {
            return Ticket.Tipo.SOFTWARE;
        } else {
            return Ticket.Tipo.SIN_CLASIFICAR;
        }
    }

    private String limpiarTextoCorreo(String texto) {
        if (texto == null) return "";
        texto = texto.replaceAll("(?is)on\\s.+?(wrote|escribi[oó]|ha escrito)\\s*:.*", "");
        texto = texto.replaceAll("(?m)^>.*", "");
        texto = texto.replaceAll("(?i)helpdeskpro.*", "");
        texto = texto.replaceAll("(?i)gmail\\.com.*", "");
        texto = texto.replaceAll("(?i)ha recibido una respuesta.*", "");
        texto = texto.replaceAll("(?i)©.*helpdeskpro.*", "");
        texto = texto.replaceAll("\\n{2,}", "\n").trim();
        return texto;
    }

}
