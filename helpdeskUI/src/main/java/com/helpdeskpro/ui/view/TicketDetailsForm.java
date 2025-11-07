package com.helpdeskpro.ui.view;

import com.google.gson.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class TicketDetailsForm extends JFrame {

    private final Long usuarioId;
    private final String nombreUsuario;
    private final String rolUsuario;
    private final Long ticketId;
    private final String ticketTitulo;
    private final String ticketEstado;

    private JPanel chatPanel;
    private JTextArea txtMensaje;
    private JScrollPane scrollPane;
    private Timer refreshTimer;
    private final MainFrame parentFrame;

    public TicketDetailsForm(Long usuarioId, String nombreUsuario, String rolUsuario,
                             Long ticketId, String ticketTitulo, String ticketEstado) {
        this(usuarioId, nombreUsuario, rolUsuario, ticketId, ticketTitulo, ticketEstado, null);
    }

    public TicketDetailsForm(Long usuarioId, String nombreUsuario, String rolUsuario,
                             Long ticketId, String ticketTitulo, String ticketEstado, MainFrame parentFrame) {
        this.usuarioId = usuarioId;
        this.nombreUsuario = nombreUsuario;
        this.rolUsuario = rolUsuario;
        this.ticketId = ticketId;
        this.ticketTitulo = ticketTitulo;
        this.ticketEstado = ticketEstado;
        this.parentFrame = parentFrame;

        setTitle("Detalles Ticket - HelpDeskPro");
        setSize(1200, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        inicializarUI();
        cargarMensajes();
        iniciarAutoRefresco();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentFrame != null) {
                    parentFrame.refrescarTickets();
                }
                if (refreshTimer != null) refreshTimer.cancel();
            }
        });
    }

    private void inicializarUI() {
        Color fondoClaro = new Color(249, 250, 251);
        Color azul = new Color(37, 99, 235);

        setLayout(new BorderLayout());
        getContentPane().setBackground(fondoClaro);

        //  header
        JPanel headerTop = new JPanel(new BorderLayout());
        headerTop.setBackground(Color.WHITE);
        headerTop.setPreferredSize(new Dimension(getWidth(), 70));
        headerTop.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        logoPanel.setBackground(Color.WHITE);
        JLabel lblLogo = new JLabel("<html><span style='color:black; font-size:18px;'>HelpDesk</span><span style='color:#2563eb; font-size:18px;'>Pro</span></html>");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoPanel.add(lblLogo);
        headerTop.add(logoPanel, BorderLayout.WEST);

        // Breadcrumb
        JPanel breadcrumbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 20));
        breadcrumbPanel.setBackground(Color.WHITE);
        JLabel lblBreadcrumb = new JLabel("Tickets  /  TICKET-" + ticketId);
        lblBreadcrumb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblBreadcrumb.setForeground(new Color(107, 114, 128));
        breadcrumbPanel.add(lblBreadcrumb);
        headerTop.add(breadcrumbPanel, BorderLayout.CENTER);

        add(headerTop, BorderLayout.NORTH);

        //  contenedor principal
        JPanel mainContainer = new JPanel(new BorderLayout(20, 0));
        mainContainer.setBackground(fondoClaro);
        mainContainer.setBorder(new EmptyBorder(25, 30, 25, 30));

        //  PANEL IZQUIERDO (Chat)
        JPanel leftPanel = new JPanel(new BorderLayout(0, 15));
        leftPanel.setBackground(fondoClaro);
        leftPanel.setPreferredSize(new Dimension(700, getHeight()));

        // Header del ticket
        JPanel ticketHeader = new JPanel(new BorderLayout(10, 8));
        ticketHeader.setBackground(Color.WHITE);
        ticketHeader.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        JLabel lblTicketId = new JLabel("TICKET-" + ticketId + ": " + ticketTitulo);
        lblTicketId.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTicketId.setForeground(new Color(17, 24, 39));
        ticketHeader.add(lblTicketId, BorderLayout.NORTH);

        JLabel lblSubtitle = new JLabel("Mensajes del ticket: ");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setForeground(new Color(107, 114, 128));
        ticketHeader.add(lblSubtitle, BorderLayout.CENTER);

        leftPanel.add(ticketHeader, BorderLayout.NORTH);

        // Panel de mensajes
        JPanel chatContainer = new JPanel(new BorderLayout());
        chatContainer.setBackground(Color.WHITE);
        chatContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(20, 25, 20, 25)
        ));

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        chatContainer.add(scrollPane, BorderLayout.CENTER);

        leftPanel.add(chatContainer, BorderLayout.CENTER);

        // Panel de input
        JPanel inputContainer = new JPanel(new BorderLayout(12, 0));
        inputContainer.setBackground(Color.WHITE);
        inputContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        txtMensaje = new JTextArea(3, 40);
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setLineWrap(true);
        txtMensaje.setWrapStyleWord(true);
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        txtMensaje.setBackground(new Color(249, 250, 251));

        JScrollPane inputScroll = new JScrollPane(txtMensaje);
        inputScroll.setBorder(null);
        inputContainer.add(inputScroll, BorderLayout.CENTER);

        JButton btnEnviar = new JButton("Send") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEnviar.setBackground(azul);
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setContentAreaFilled(false);
        btnEnviar.setPreferredSize(new Dimension(100, 50));
        btnEnviar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnviar.addActionListener(e -> enviarMensaje());

        btnEnviar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnEnviar.setBackground(new Color(29, 78, 216));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnEnviar.setBackground(azul);
            }
        });

        inputContainer.add(btnEnviar, BorderLayout.EAST);
        leftPanel.add(inputContainer, BorderLayout.SOUTH);

        mainContainer.add(leftPanel, BorderLayout.CENTER);

        //  PANEL DERECHO (Detalles)
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(fondoClaro);
        rightPanel.setPreferredSize(new Dimension(350, getHeight()));

        // Card de detalles
        JPanel detailsCard = new JPanel();
        detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));
        detailsCard.setBackground(Color.WHITE);
        detailsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        detailsCard.setMaximumSize(new Dimension(350, 400));

        JLabel lblDetailsTitle = new JLabel("Detalles del ticket");
        lblDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblDetailsTitle.setForeground(new Color(17, 24, 39));
        lblDetailsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailsCard.add(lblDetailsTitle);
        detailsCard.add(Box.createVerticalStrut(20));

        // Status
        detailsCard.add(crearDetailRow("Estado", ticketEstado, getColorEstado(ticketEstado)));
        detailsCard.add(Box.createVerticalStrut(15));


        // Assignee
        detailsCard.add(crearDetailRow("Asignado a:", nombreUsuario, null));
        detailsCard.add(Box.createVerticalStrut(15));




        rightPanel.add(detailsCard);
        rightPanel.add(Box.createVerticalStrut(20));

        // Card de acciones
        JPanel actionsCard = new JPanel();
        actionsCard.setLayout(new BoxLayout(actionsCard, BoxLayout.Y_AXIS));
        actionsCard.setBackground(Color.WHITE);
        actionsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(20, 20, 20, 20)
        ));
        actionsCard.setMaximumSize(new Dimension(350, 250));

        JLabel lblActionsTitle = new JLabel("Acciones");
        lblActionsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblActionsTitle.setForeground(new Color(17, 24, 39));
        lblActionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsCard.add(lblActionsTitle);
        actionsCard.add(Box.createVerticalStrut(15));




        // Botón Close Ticket (Rojo)
        JButton btnCloseTicket = crearActionButton("Marcar como resuelto", new Color(93, 215, 11));
        btnCloseTicket.addActionListener(e -> marcarComoResuelto());
        actionsCard.add(btnCloseTicket);

        rightPanel.add(actionsCard);

        mainContainer.add(rightPanel, BorderLayout.EAST);
        add(mainContainer, BorderLayout.CENTER);
    }

    private JPanel crearDetailRow(String label, String value, Color badgeColor) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Color.WHITE);
        row.setMaximumSize(new Dimension(310, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLabel.setForeground(new Color(107, 114, 128));
        row.add(lblLabel, BorderLayout.WEST);

        if (badgeColor != null) {
            JLabel lblBadge = new JLabel(value);
            lblBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblBadge.setOpaque(true);
            lblBadge.setBackground(badgeColor);
            lblBadge.setForeground(getTextColorForBadge(ticketEstado));
            lblBadge.setBorder(new EmptyBorder(4, 12, 4, 12));
            row.add(lblBadge, BorderLayout.EAST);
        } else {
            JLabel lblValue = new JLabel(value);
            lblValue.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblValue.setForeground(new Color(17, 24, 39));
            row.add(lblValue, BorderLayout.EAST);
        }

        return row;
    }

    private JButton crearActionButton(String text, Color bgColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setMaximumSize(new Dimension(310, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Color hoverColor = bgColor.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private Color getColorEstado(String estado) {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":

                return new Color(254, 243, 199);
            case "EN PROCESO":

                return new Color(219, 234, 254);
            case "RESUELTO":

                return new Color(220, 252, 231);
            default:
                return new Color(243, 244, 246);
        }
    }

    private Color getTextColorForBadge(String estado) {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":

                return new Color(146, 64, 14);
            case "EN PROCESO":

                return new Color(30, 64, 175);
            case "RESUELTO":

                return new Color(22, 101, 52);
            default:
                return new Color(55, 65, 81);
        }
    }

    private void cargarMensajes() {
        chatPanel.removeAll();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = "http://localhost:8080/api/mensajes/ticket/" + ticketId + "?usuarioId=" + usuarioId;
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    JsonArray mensajes = JsonParser.parseReader(reader).getAsJsonArray();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    for (JsonElement elem : mensajes) {
                        JsonObject m = elem.getAsJsonObject();
                        JsonObject autor = m.get("autor").getAsJsonObject();

                        String texto = m.get("texto").getAsString();
                        String nombre = autor.get("nombre").getAsString();
                        String rol = autor.get("rol").getAsString();
                        String fecha = m.get("fecha").getAsString().replace("T", " ").substring(0, 16);

                        JPanel bubble = crearBurbujaMensaje(texto, nombre, rol, fecha);
                        chatPanel.add(bubble);
                        chatPanel.add(Box.createVerticalStrut(15));
                    }
                    chatPanel.revalidate();
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel crearBurbujaMensaje(String texto, String autor, String rol, String fecha) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.setMaximumSize(new Dimension(650, Integer.MAX_VALUE));

        boolean esAutor = autor.equalsIgnoreCase(nombreUsuario);
        boolean esNotaInterna = texto.contains("Internal Note") || rol.contains("Internal");

        // Avatar
        JLabel avatar = new JLabel(esAutor ? "👤" : "👥");
        avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        avatar.setPreferredSize(new Dimension(45, 45));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.TOP);

        // Contenido del mensaje
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);

        // Header: Nombre y fecha
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setMaximumSize(new Dimension(550, 25));

        JLabel lblAutor = new JLabel(autor);
        lblAutor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAutor.setForeground(new Color(14, 16, 19));

        JLabel lblFecha = new JLabel(fecha);
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblFecha.setForeground(new Color(156, 163, 175));

        headerPanel.add(lblAutor, BorderLayout.WEST);
        headerPanel.add(lblFecha, BorderLayout.EAST);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(8));

        // Mensaje
        JTextArea txtArea = new JTextArea(texto);
        txtArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtArea.setEditable(false);
        txtArea.setLineWrap(true);
        txtArea.setWrapStyleWord(true);
        txtArea.setOpaque(true);
        txtArea.setBorder(new EmptyBorder(12, 15, 12, 15));

        if (esNotaInterna) {
            txtArea.setBackground(new Color(254, 243, 199));
            txtArea.setForeground(new Color(120, 53, 15));
        } else if (esAutor) {
            // Burbuja del técnico (usuario logueado) - Azul
            txtArea.setBackground(new Color(219, 234, 254));
            txtArea.setForeground(new Color(14, 18, 30));
        } else {
            // Burbuja del cliente - Gris
            txtArea.setBackground(new Color(249, 250, 251));
            txtArea.setForeground(new Color(55, 65, 81));
        }

        contentPanel.add(txtArea);



        panel.add(avatar, BorderLayout.WEST);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private void enviarMensaje() {
        String texto = limpiarTexto(txtMensaje.getText().trim());
        if (texto.isEmpty()) return;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("http://localhost:8080/api/mensajes/enviar?usuarioId=" + usuarioId);
            request.setHeader("Content-Type", "application/json");

            JsonObject mensaje = new JsonObject();
            JsonObject ticketObj = new JsonObject();
            ticketObj.addProperty("id", ticketId);
            mensaje.add("ticket", ticketObj);
            mensaje.addProperty("texto", texto);

            request.setEntity(new StringEntity(mensaje.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200 || response.getCode() == 201) {
                    txtMensaje.setText("");
                    cargarMensajes();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al enviar mensaje (" + response.getCode() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String limpiarTexto(String texto) {
        if (texto == null || texto.isEmpty()) return "";

        texto = texto.replace("\r", "")
                .replaceAll("\\u00A0|\\u202F|\\u200B|\\u200C|\\u200D|\\uFEFF", " ")
                .trim();

        int idx = -1;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?im)^\\s*on\\s.+?(wrote|escribi[oó]|ha escrito)\\s*:.*$");
        java.util.regex.Matcher m = p.matcher(texto);
        if (m.find()) {
            idx = m.start();
        }

        if (idx != -1) {
            texto = texto.substring(0, idx);
        }

        texto = texto.replaceAll("(?m)^>.*", "");
        texto = texto.replaceAll("(?i)helpdeskpro.*", "");
        texto = texto.replaceAll("(?i)gmail\\.com.*", "");
        texto = texto.replaceAll("(?i)ha recibido una respuesta.*", "");
        texto = texto.replaceAll("(?i)©.*helpdeskpro.*", "");
        texto = texto.replaceAll("(?i)\\[image:.*", "");
        texto = texto.replaceAll("(?i)t[ií]tulo:.*", "");
        texto = texto.replaceAll("(?i)t[eé]cnico:.*", "");
        texto = texto.replaceAll("(?i)id del ticket:.*", "");
        texto = texto.replaceAll("(?i)mensaje del t[eé]cnico:.*", "");
        texto = texto.replaceAll("\\n{2,}", "\n").trim();

        return texto.trim();
    }

    private void marcarComoResuelto() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = "http://localhost:8080/api/tickets/" + ticketId + "/cerrar?usuarioId=" + usuarioId;
            HttpPut request = new HttpPut(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200) {
                    JOptionPane.showMessageDialog(this, "Ticket marcado como resuelto.");
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        if (parentFrame != null) parentFrame.refrescarTickets();
                    });
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cerrar ticket (" + response.getCode() + ")");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciarAutoRefresco() {
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> cargarMensajes());
            }
        }, 5000, 7000);
    }
}