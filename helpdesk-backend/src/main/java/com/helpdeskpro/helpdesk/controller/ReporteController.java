package com.helpdeskpro.helpdesk.controller;

import com.helpdeskpro.helpdesk.entity.Ticket;
import com.helpdeskpro.helpdesk.entity.Usuario;
import com.helpdeskpro.helpdesk.repository.TicketRepository;
import com.helpdeskpro.helpdesk.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ================================
    // 📋 1️⃣ Exportar Tickets
    // ================================
    @GetMapping("/tickets")
    public ResponseEntity<byte[]> exportarTickets(@RequestParam String formato) throws IOException {
        List<Ticket> tickets = ticketRepository.findAll();

        if (formato.equalsIgnoreCase("json")) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(tickets);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        if (formato.equalsIgnoreCase("pdf")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Image logo = Image.getInstance(getClass().getResource("/static/logoHelpDeskProo.png"));
            logo.scaleToFit(400, 400);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
            document.add(new Paragraph(" "));


            document.add(new Paragraph("📊 Reporte de Tickets - HelpDeskPro", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Fecha de generación: " + LocalDate.now()));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Título");
            table.addCell("Cliente");
            table.addCell("Técnico");
            table.addCell("Estado");

            for (Ticket t : tickets) {
                table.addCell(String.valueOf(t.getId()));
                table.addCell(t.getTitulo());
                table.addCell(t.getCliente() != null ? t.getCliente().getNombre() : "N/A");
                table.addCell(t.getTecnico() != null ? t.getTecnico().getNombre() : "Sin asignar");
                table.addCell(t.getEstado().toString());
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        }

        throw new IllegalArgumentException("Formato no soportado. Usa 'pdf' o 'json'.");
    }

    // ================================
    // 👥 2️⃣ Exportar Usuarios
    // ================================
    @GetMapping("/usuarios")
    public ResponseEntity<byte[]> exportarUsuarios(@RequestParam String formato) throws IOException {
        List<Usuario> usuarios = usuarioRepository.findAll();

        if (formato.equalsIgnoreCase("json")) {
            ObjectMapper mapper = new ObjectMapper();
            byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(usuarios);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        if (formato.equalsIgnoreCase("pdf")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Image logo = Image.getInstance(getClass().getResource("/static/logoHelpDeskProo.png"));
            logo.scaleToFit(120, 120);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
            document.add(new Paragraph(" "));


            document.add(new Paragraph("👤 Reporte de Usuarios - HelpDeskPro", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Fecha de generación: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.addCell("ID");
            table.addCell("Nombre");
            table.addCell("Correo");
            table.addCell("Rol");

            for (Usuario u : usuarios) {
                table.addCell(String.valueOf(u.getId()));
                table.addCell(u.getNombre());
                table.addCell(u.getCorreo());
                table.addCell(u.getRol().toString());
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        }

        throw new IllegalArgumentException("Formato no soportado. Usa 'pdf' o 'json'.");
    }

    // ================================
    // 🧑‍🔧 3️⃣ Exportar Técnicos con desempeño (tickets resueltos)
    // ================================
    @GetMapping("/tecnicos")
    public ResponseEntity<byte[]> exportarTecnicos(@RequestParam String formato) throws IOException {
        List<Usuario> tecnicos = usuarioRepository.findByRol(Usuario.Rol.TECNICO);

        if (formato.equalsIgnoreCase("json")) {
            ObjectMapper mapper = new ObjectMapper();
            byte[] json = mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(tecnicos);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tecnicos.json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }

        if (formato.equalsIgnoreCase("pdf")) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Image logo = Image.getInstance(getClass().getResource("/static/logoHelpDeskProo.png"));
            logo.scaleToFit(120, 120);
            logo.setAlignment(Image.ALIGN_CENTER);
            document.add(logo);
            document.add(new Paragraph(" "));


            document.add(new Paragraph("🧑‍🔧 Reporte de Técnicos - HelpDeskPro", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph("Fecha de generación: " + LocalDate.now()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.addCell("Nombre");
            table.addCell("Correo");
            table.addCell("Tickets resueltos");

            for (Usuario t : tecnicos) {
                long ticketsResueltos = ticketRepository.countByTecnicoAndEstado(t, Ticket.Estado.RESUELTO);
                table.addCell(t.getNombre());
                table.addCell(t.getCorreo());
                table.addCell(String.valueOf(ticketsResueltos));
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tecnicos.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(baos.toByteArray());
        }

        throw new IllegalArgumentException("Formato no soportado. Usa 'pdf' o 'json'.");
    }
}
