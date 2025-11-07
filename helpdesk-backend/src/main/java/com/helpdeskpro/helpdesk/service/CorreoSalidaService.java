package com.helpdeskpro.helpdesk.service;

import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class CorreoSalidaService {

    @Value("${spring.mail.username}")
    private String correoOrigen;

    @Value("${spring.mail.password}")
    private String contrasena;


    public void enviarConfirmacionCreacionTicket(Ticket ticket) {
        try {
            String destinatario = ticket.getCliente().getCorreo();
            String asunto = "📬 Ticket recibido: #" + ticket.getId();

            String tecnico = (ticket.getTecnico() != null)
                    ? ticket.getTecnico().getNombre()
                    : "Sin asignar";

            String html = """
            <html>
              <body style="font-family: 'Segoe UI', sans-serif; background-color: #f4f6f8; padding: 20px;">
                <div style="text-align: center; margin-bottom: 10px;">
                    <img src="https://i.imgur.com/QUKWNYb.png"\s
                    alt="HelpDeskPro - Soluciones de TI"
                    style="width: 300px; height: auto;">
                </div>
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 3px 10px rgba(0,0,0,0.1);">
                  <div style="background-color: #1976d2; color: white; padding: 16px 24px;">
                    <h2 style="margin: 0;">HelpDeskPro - Confirmación de Ticket</h2>
                  </div>
                  <div style="padding: 20px;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Hemos recibido correctamente tu solicitud. Nuestro equipo técnico atenderá tu caso lo antes posible.</p>
                    <div style="background-color: #f1f8ff; border-left: 4px solid #1976d2; padding: 10px 15px; margin: 15px 0;">
                      <p><strong>ID del ticket:</strong> #%d</p>
                      <p><strong>Estado:</strong> 🟡 Pendiente</p>
                      <p><strong>Técnico asignado:</strong> %s</p>
                    </div>
                    <p>Gracias por comunicarte con el soporte técnico de <strong>HelpDeskPro</strong>.</p>
                  </div>
                  <div style="background-color: #f9fafb; text-align: center; color: #666; font-size: 12px; padding: 12px;">
                    <p>© 2025 HelpDeskPro. Todos los derechos reservados.</p>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(ticket.getCliente().getNombre(), ticket.getId(), tecnico);

            enviarCorreoHTML(destinatario, asunto, html);
            System.out.println(" Correo de confirmación enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println(" Error al enviar correo de confirmación: " + e.getMessage());
        }
    }


    public void enviarRespuestaAlCliente(Ticket ticket, String mensajeTexto, Usuario tecnico) {
        try {
            String destinatario = ticket.getCliente().getCorreo();
            String asunto = "Re: " + ticket.getTitulo();

            String html = """
            <html>
              <body style="font-family: 'Segoe UI', sans-serif; background-color: #f4f6f8; padding: 20px;">
                <div style="text-align: center; margin-bottom: 10px;">
                    <img src="https://i.imgur.com/QUKWNYb.png"\s
                    alt="HelpDeskPro - Soluciones de TI"
                    style="width: 300px; height: auto;">
                </div>
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 3px 10px rgba(0,0,0,0.1);">
                  <div style="background-color: #0288d1; color: white; padding: 16px 24px;">
                    <h2 style="margin: 0;">HelpDeskPro - Respuesta de Soporte</h2>
                  </div>
                  <div style="padding: 20px;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Has recibido una respuesta de nuestro equipo técnico para tu ticket:</p>
                    <div style="background-color: #f1f8ff; border-left: 4px solid #0288d1; padding: 10px 15px; margin: 15px 0;">
                      <p><strong>ID del ticket:</strong> #%d</p>
                      <p><strong>Título:</strong> %s</p>
                      <p><strong>Técnico:</strong> %s</p>
                    </div>
                    <p><strong>Mensaje del técnico:</strong></p>
                    <blockquote style="border-left: 4px solid #0288d1; padding-left: 12px; color: #333; margin: 10px 0;">
                      %s
                    </blockquote>
                    <p>Si deseas responder, puedes hacerlo contestando a este correo.</p>
                  </div>
                  <div style="background-color: #f9fafb; text-align: center; color: #666; font-size: 12px; padding: 12px;">
                    <p>© 2025 HelpDeskPro. Todos los derechos reservados.</p>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(ticket.getCliente().getNombre(), ticket.getId(), ticket.getTitulo(),
                    tecnico.getNombre(), mensajeTexto.replaceAll("\n", "<br>"));

            enviarCorreoHTML(destinatario, asunto, html);
            System.out.println(" Correo de respuesta enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println(" Error al enviar respuesta: " + e.getMessage());
        }
    }


    public void enviarCorreoCierreTicket(Ticket ticket) {
        try {
            String destinatario = ticket.getCliente().getCorreo();
            String asunto = "✅ Ticket #" + ticket.getId() + " - Caso cerrado con éxito";

            String tecnico = (ticket.getTecnico() != null)
                    ? ticket.getTecnico().getNombre()
                    : "Equipo HelpDeskPro";

            String html = """
            <html>
              <body style="font-family: 'Segoe UI', sans-serif; background-color: #f4f6f8; padding: 20px;">
                <div style="text-align: center; margin-bottom: 10px;">
                    <img src="https://i.imgur.com/QUKWNYb.png"\s
                    alt="HelpDeskPro - Soluciones de TI"
                    style="width: 300px; height: auto;">
                </div>
                <div style="max-width: 600px; margin: auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 3px 10px rgba(0,0,0,0.1);">
                  <div style="background-color: #2e7d32; color: white; padding: 16px 24px;">
                    <h2 style="margin: 0;">HelpDeskPro - Ticket Resuelto ✅</h2>
                  </div>
                  <div style="padding: 20px;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Nos complace informarte que tu ticket ha sido resuelto exitosamente.</p>
                    <div style="background-color: #e8f5e9; border-left: 4px solid #2e7d32; padding: 10px 15px; margin: 15px 0;">
                      <p><strong>ID del ticket:</strong> #%d</p>
                      <p><strong>Título:</strong> %s</p>
                      <p><strong>Estado:</strong> 🟢 Resuelto</p>
                      <p><strong>Técnico a cargo:</strong> %s</p>
                    </div>
                    <p>Gracias por usar el servicio de soporte técnico de <strong>HelpDeskPro</strong>.</p>
                    <p style="font-size: 14px; color: #555;">Si tienes otra consulta, puedes escribirnos en cualquier momento respondiendo a este correo.</p>
                  </div>
                  <div style="background-color: #f9fafb; text-align: center; color: #666; font-size: 12px; padding: 12px;">
                    <p>© 2025 HelpDeskPro. Todos los derechos reservados.</p>
                  </div>
                </div>
              </body>
            </html>
            """.formatted(ticket.getCliente().getNombre(), ticket.getId(),
                    ticket.getTitulo(), tecnico);

            enviarCorreoHTML(destinatario, asunto, html);
            System.out.println("✅ Correo de cierre enviado a " + destinatario);

        } catch (Exception e) {
            System.err.println(" Error al enviar correo de cierre: " + e.getMessage());
        }
    }


    private void enviarCorreoHTML(String destinatario, String asunto, String contenidoHTML) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(correoOrigen, contrasena);
            }
        });

        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress(correoOrigen, "HelpDeskPro Soporte"));
        } catch (java.io.UnsupportedEncodingException e) {
            message.setFrom(new InternetAddress(correoOrigen));
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
        message.setSubject(asunto);

        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(contenidoHTML, "text/html; charset=utf-8");

        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}
