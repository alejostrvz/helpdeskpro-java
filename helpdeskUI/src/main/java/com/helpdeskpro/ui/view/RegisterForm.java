package com.helpdeskpro.ui.view;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class RegisterForm extends JFrame {

    private static final String API_URL = "http://localhost:8080/api/auth/register";

    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRol;

    public RegisterForm() {
        FlatLightLaf.setup();
        setTitle("Registrar Usuario - HelpDeskPro");
        setSize(420, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        inicializarUI();
    }

    private void inicializarUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 40, 25, 40));
        add(panel);

        // Título
        JLabel lblTitulo = new JLabel("<html><span style='color:black;'>Registrar nuevo usuario</span></html>");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Formulario
        JPanel form = new JPanel(new GridLayout(8, 1, 10, 10));
        form.setBackground(Color.WHITE);
        panel.add(form, BorderLayout.CENTER);

        JLabel lblNombre = new JLabel("Nombre completo:");
        txtNombre = new JTextField();

        JLabel lblCorreo = new JLabel("Correo electrónico:");
        txtCorreo = new JTextField();

        JLabel lblPassword = new JLabel("Contraseña:");
        txtPassword = new JPasswordField();

        JLabel lblRol = new JLabel("Rol:");
        cmbRol = new JComboBox<>(new String[]{"ADMIN", "TECNICO"});

        JButton btnRegistrar = new JButton("Registrar usuario");
        btnRegistrar.setBackground(new Color(37, 99, 235));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> registrarUsuario());

        form.add(lblNombre);
        form.add(txtNombre);
        form.add(lblCorreo);
        form.add(txtCorreo);
        form.add(lblPassword);
        form.add(txtPassword);
        form.add(lblRol);
        form.add(cmbRol);

        panel.add(btnRegistrar, BorderLayout.SOUTH);
    }

    private void registrarUsuario() {
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String rol = (String) cmbRol.getSelectedItem();

        if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos.");
            return;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API_URL);
            request.setHeader("Content-Type", "application/json");

            String json = String.format(
                    "{\"nombre\": \"%s\", \"correo\": \"%s\", \"password\": \"%s\", \"rol\": \"%s\"}",
                    nombre, correo, password, rol
            );

            request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(request)) {
                int status = response.getCode();
                if (status == 200 || status == 201) {
                    JOptionPane.showMessageDialog(this, "✅ Usuario registrado correctamente.");
                    dispose();
                } else {
                    InputStream input = response.getEntity().getContent();
                    String error = new String(input.readAllBytes(), StandardCharsets.UTF_8);
                    JOptionPane.showMessageDialog(this, "⚠️ Error al registrar: " + error);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegisterForm().setVisible(true));
    }
}
