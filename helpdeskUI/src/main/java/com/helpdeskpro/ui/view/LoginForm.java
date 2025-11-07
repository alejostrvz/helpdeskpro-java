package com.helpdeskpro.ui.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class LoginForm extends JFrame {

    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JLabel lblStatus;

    public LoginForm() {
        FlatLightLaf.setup();
        setTitle("Login - HelpDeskPro");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color fondo = new Color(245, 246, 250);
        Color azul = new Color(37, 99, 235);
        Color azulHover = new Color(25, 85, 210);

        JPanel fondoPanel = new JPanel(new GridBagLayout());
        fondoPanel.setBackground(fondo);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        //  Panel principal
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(520, 600));
        card.setBorder(new CompoundBorder(
                new EmptyBorder(50, 70, 50, 70),
                new RoundedBorder(25)
        ));

        //  Logo texto - CENTRADO
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoPanel.setBackground(Color.WHITE);
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel lblLogo = new JLabel("<html><span style='color:black;'>HelpDesk</span><span style='color:#2563eb;'>Pro</span></html>");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        logoPanel.add(lblLogo);
        card.add(logoPanel);

        card.add(Box.createVerticalStrut(25));

        //  Título
        JLabel lblTitulo = new JLabel("Bienvenido de nuevo");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitulo);

        JLabel lblSubtitulo = new JLabel("Inicia sesión en tu cuenta para continuar");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(120, 120, 120));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSubtitulo);

        card.add(Box.createVerticalStrut(30));

        //  Campo Correo - ETIQUETA A LA DERECHA
        JPanel correoLabelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        correoLabelPanel.setBackground(Color.WHITE);
        correoLabelPanel.setMaximumSize(new Dimension(380, 25));
        JLabel lblCorreo = new JLabel("Correo electrónico");
        lblCorreo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCorreo.setForeground(new Color(90, 90, 90));
        correoLabelPanel.add(lblCorreo);
        card.add(correoLabelPanel);

        JPanel correoPanel = crearCampoConIcono("📧", "tu@email.com");
        txtCorreo = (JTextField) correoPanel.getComponent(1);
        card.add(correoPanel);

        card.add(Box.createVerticalStrut(20));

        //  Campo Contraseña - ETIQUETA A LA DERECHA
        JPanel passLabelPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        passLabelPanel.setBackground(Color.WHITE);
        passLabelPanel.setMaximumSize(new Dimension(380, 25));
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblPass.setForeground(new Color(90, 90, 90));
        passLabelPanel.add(lblPass);
        card.add(passLabelPanel);

        JPanel passPanel = crearCampoConIcono("🔒", "Introduce tu contraseña");
        txtPassword = (JPasswordField) passPanel.getComponent(1);
        card.add(passPanel);

        //  Olvidaste tu contraseña
        JPanel olvidoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        olvidoPanel.setBackground(Color.WHITE);
        olvidoPanel.setMaximumSize(new Dimension(380, 30));
        JLabel lblOlvido = new JLabel("¿Olvidaste tu contraseña?");
        lblOlvido.setForeground(azul);
        lblOlvido.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblOlvido.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        olvidoPanel.add(lblOlvido);
        card.add(olvidoPanel);

        card.add(Box.createVerticalStrut(25));

        //  Botón con Border Radius ===
        JButton btnLogin = new JButton("Iniciar sesión") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setBackground(azul);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setOpaque(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setPreferredSize(new Dimension(380, 45));
        btnLogin.setMaximumSize(new Dimension(380, 45));

        // Hover elegante
        btnLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnLogin.setBackground(azulHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnLogin.setBackground(azul);
            }
        });
        btnLogin.addActionListener(e -> autenticarUsuario());
        card.add(btnLogin);

        card.add(Box.createVerticalStrut(25));

        //  Mensaje de estado
        lblStatus = new JLabel("", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblStatus);

        card.add(Box.createVerticalStrut(25));

        //  Footer
        JLabel lblFooter = new JLabel("© 2025 HelpDeskPro. Todos los derechos reservados.");
        lblFooter.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(130, 130, 130));
        lblFooter.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblFooter);

        fondoPanel.add(card, gbc);
        add(fondoPanel);
    }

    private JPanel crearCampoConIcono(String icono, String placeholder) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        panel.setMaximumSize(new Dimension(380, 38));
        panel.setPreferredSize(new Dimension(380, 38));

        JLabel iconLabel = new JLabel(icono);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));

        JComponent field;
        if (placeholder.toLowerCase().contains("contraseña")) {
            JPasswordField passwordField = new JPasswordField();
            passwordField.putClientProperty("JTextField.placeholderText", placeholder);
            passwordField.setBorder(null);
            field = passwordField;
        } else {
            JTextField textField = new JTextField();
            textField.putClientProperty("JTextField.placeholderText", placeholder);
            textField.setBorder(null);
            field = textField;
        }

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);

        return panel;
    }

    private void autenticarUsuario() {
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (correo.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Ingrese correo y contraseña.");
            return;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("http://localhost:8080/api/auth/login");
            request.setHeader("Content-Type", "application/json");

            JsonObject json = new JsonObject();
            json.addProperty("correo", correo);
            json.addProperty("password", password);

            StringEntity entity = new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
            request.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getCode();

                if (status == 200) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                    JsonObject result = JsonParser.parseReader(reader).getAsJsonObject();

                    String nombre = result.get("nombre").getAsString();
                    String rol = result.get("rol").getAsString();
                    Long usuarioId = result.get("id").getAsLong();

                    dispose();
                    new MainFrame(nombre, rol, usuarioId).setVisible(true);

                } else {
                    lblStatus.setText("Credenciales incorrectas (" + status + ")");
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatus.setText("Error de conexión con el servidor.");
        }
    }

    // Clase para bordes redondeados
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        RoundedBorder(int radius) { this.radius = radius; }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(230, 230, 230));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}