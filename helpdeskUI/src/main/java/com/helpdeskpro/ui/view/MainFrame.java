package com.helpdeskpro.ui.view;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.gson.*;
import net.miginfocom.swing.MigLayout;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Objects;


public class MainFrame extends JFrame {

    //  Config API
    private static final String API_BASE = "http://localhost:8080";
    private static final String API_TICKETS_ADMIN = API_BASE + "/api/tickets?usuarioId=%d";
    private static final String API_TICKETS_TECNICO = API_BASE + "/api/tickets/tecnico/%d?usuarioId=%d";
    private static final String API_TICKET_DELETE = API_BASE + "/api/tickets/%d?usuarioId=%d";

    private static final String API_USUARIOS_LIST = API_BASE + "/api/usuarios?adminId=%d";
    private static final String API_USUARIO_DELETE = API_BASE + "/api/usuarios/%d/eliminar?adminId=%d";

    private static final String API_REPORTE_TICKETS  = API_BASE + "/api/reportes/tickets?usuarioId=%d";
    private static final String API_REPORTE_TECNICOS = API_BASE + "/api/reportes/tecnicos?usuarioId=%d";
    private static final String API_REPORTE_USUARIOS = API_BASE + "/api/reportes/usuarios?usuarioId=%d";

    //  Estado sesión
    private final String nombreUsuario;
    private final String rolUsuario;  // "ADMIN" | "TECNICO"
    private final Long   usuarioId;

    // UI base
    private JPanel panelContenido;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblTitulo;
    private JComboBox<String> cmbEstado;
    private JComboBox<String> cmbTipo;
    private JButton btnFiltrar;
    private JTextField searchField;
    // Vista actual
    private enum Vista { TICKETS, TECNICOS, USUARIOS }
    private Vista vistaActual = Vista.TICKETS;

    //  Botones del menu
    private JButton btnTickets;
    private JButton btnTecnicos;
    private JButton btnUsuarios;

    public MainFrame(String nombreUsuario, String rolUsuario, Long usuarioId) {
        this.nombreUsuario = nombreUsuario;
        this.rolUsuario = rolUsuario;
        this.usuarioId = usuarioId;

        FlatLightLaf.setup();
        setTitle("HelpDeskPro - Panel Principal");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        inicializarUI();
        // Carga inicial según rol
        if (isAdmin()) {
            setVista(Vista.TICKETS);
        } else {
            // Técnico: solo sus tickets
            setVista(Vista.TICKETS);
        }

        // Refresca el MainFrame para ver nuevos tickets
        Timer autoRefreshTimer = new Timer(30000, e -> {
            if (vistaActual == Vista.TICKETS) {
                cargarTicketsDesdeAPI();
            }
        });
        autoRefreshTimer.start();
    }

    private boolean isAdmin()   { return "ADMIN".equalsIgnoreCase(rolUsuario); }
    private boolean isTecnico() { return "TECNICO".equalsIgnoreCase(rolUsuario); }

    private void inicializarUI() {
        Color fondoClaro = new Color(249, 250, 251);
        setLayout(new BorderLayout());

        //  HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setPreferredSize(new Dimension(getWidth(), 65));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));

        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 17));
        logoPanel.setBackground(Color.WHITE);
        JLabel lblLogo = new JLabel("<html><b><span style='color:black;'>HelpDesk</span><span style='color:#2563eb;'>Pro</span></b></html>");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logoPanel.add(lblLogo);


        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        headerRight.setBackground(Color.WHITE);

        if (isAdmin()) {
            JLabel lblReporte = new JLabel("Generar reporte:");
            lblReporte.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            lblReporte.setForeground(new Color(75, 85, 99));
            headerRight.add(lblReporte);

            JButton btnPdf = crearBotonHeader("PDF");
            btnPdf.addActionListener(e -> onGenerarReporte("pdf"));
            headerRight.add(btnPdf);

            JButton btnJson = crearBotonHeader("JSON");
            btnJson.addActionListener(e -> onGenerarReporte("json"));
            headerRight.add(btnJson);
        }

        JLabel lblUserInfo = new JLabel(nombreUsuario);
        lblUserInfo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblUserInfo.setForeground(new Color(55, 65, 81));
        headerRight.add(lblUserInfo);

        JPanel avatarPanel = new JPanel();
        avatarPanel.setLayout(new BorderLayout());
        avatarPanel.setPreferredSize(new Dimension(38, 38));
        avatarPanel.setBackground(new Color(255, 237, 213));
        avatarPanel.setBorder(BorderFactory.createLineBorder(new Color(251, 191, 36), 2));
        JLabel lblAvatar = new JLabel("👤");
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatarPanel.add(lblAvatar, BorderLayout.CENTER);
        headerRight.add(avatarPanel);

        header.add(logoPanel, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        //  sidebar
        JPanel sidebar = new JPanel(new MigLayout("wrap 1, fillx, insets 20 12 18 12", "[fill]", "[]8[]8[]8[]push[]"));
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(229, 231, 235)));

        // Barra de búsqueda
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(new Color(249, 250, 251));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(8, 10, 8, 10)
        ));
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchField = new JTextField("Buscar por título o ID...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setForeground(new Color(156, 163, 175));
        searchField.setBorder(null);
        searchField.setBackground(new Color(249, 250, 251));
        searchField.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Buscar por título o ID...")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(55, 65, 81));
                }
            }
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Buscar por título o ID...");
                    searchField.setForeground(new Color(156, 163, 175));
                }
            }
        });
        searchPanel.add(searchIcon, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        sidebar.add(searchPanel, "gapbottom 15");

        // filtro local de la tabla
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filtrar() {
                if (modeloTabla == null || tabla == null) return; // evita NPE si aún no hay modelo

                String texto = searchField.getText().trim().toLowerCase();
                if (texto.isEmpty() || texto.equals("buscar por título o id...")) {
                    if (tabla.getRowSorter() != null)
                        ((TableRowSorter<?>) tabla.getRowSorter()).setRowFilter(null);
                    return;
                }

                // Asegura un sorter
                TableRowSorter<DefaultTableModel> sorter;
                if (tabla.getRowSorter() instanceof TableRowSorter<?>) {
                    sorter = (TableRowSorter<DefaultTableModel>) tabla.getRowSorter();
                } else {
                    sorter = new TableRowSorter<>(modeloTabla);
                    tabla.setRowSorter(sorter);
                }

                // Filtros por vista
                RowFilter<DefaultTableModel, Object> filtro = switch (vistaActual) {
                    case TICKETS -> new RowFilter<>() {
                        @Override
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                            // columnas: 0=ID, 2=Título, 4=Técnico, 5=Cliente
                            int[] cols = {0, 2, 4, 5};
                            for (int c : cols) {
                                Object v = e.getValue(c);
                                if (v != null && v.toString().toLowerCase().contains(texto)) return true;
                            }
                            return false;
                        }
                    };
                    case TECNICOS, USUARIOS -> new RowFilter<>() {
                        @Override
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Object> e) {
                            // columnas: 1=Nombre, 2=Correo
                            int[] cols = {1, 2};
                            for (int c : cols) {
                                Object v = e.getValue(c);
                                if (v != null && v.toString().toLowerCase().contains(texto)) return true;
                            }
                            return false;
                        }
                    };
                };

                sorter.setRowFilter(filtro);
            }

            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });


        btnTickets = crearBotonMenu("🎫", "Todos los Tickets", true);
        btnTickets.addActionListener(e -> {
            actualizarEstadoMenu(Vista.TICKETS);
            setVista(Vista.TICKETS);
        });
        sidebar.add(btnTickets);

        if (isAdmin()) {
            btnTecnicos = crearBotonMenu("👤", "Técnicos", false);
            btnTecnicos.addActionListener(e -> {
                actualizarEstadoMenu(Vista.TECNICOS);
                setVista(Vista.TECNICOS);
            });
            sidebar.add(btnTecnicos);

            btnUsuarios = crearBotonMenu("👥", "Usuarios", false);
            btnUsuarios.addActionListener(e -> {
                actualizarEstadoMenu(Vista.USUARIOS);
                setVista(Vista.USUARIOS);
            });
            sidebar.add(btnUsuarios);
        }

        if (isAdmin()) {
            JButton btnRegistrar = crearBotonMenu("➕", "Registrar usuario", false);
            btnRegistrar.addActionListener(e -> new RegisterForm().setVisible(true));
            sidebar.add(btnRegistrar, "gaptop 10");
        }

        JButton btnCerrarSesion = crearBotonMenu("🚪", "Cerrar sesión", false);
        btnCerrarSesion.setForeground(new Color(185, 28, 28));
        btnCerrarSesion.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        sidebar.add(btnCerrarSesion, "dock south, gaptop 10");

        add(sidebar, BorderLayout.WEST);

        // ===== PANEL PRINCIPAL =====
        panelContenido = new JPanel(new BorderLayout(0, 20));
        panelContenido.setBackground(fondoClaro);
        panelContenido.setBorder(new EmptyBorder(35, 40, 35, 40));
        add(panelContenido, BorderLayout.CENTER);

        // Panel título con botón
        JPanel tituloPanel = new JPanel(new BorderLayout());
        tituloPanel.setBackground(fondoClaro);

        lblTitulo = new JLabel("Tickets de Soporte");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitulo.setForeground(new Color(31, 41, 55));
        tituloPanel.add(lblTitulo, BorderLayout.WEST);

        panelContenido.add(tituloPanel, BorderLayout.NORTH);

        // Panel Tabla
        JPanel tablaPanel = new JPanel(new BorderLayout(0, 15));
        tablaPanel.setBackground(Color.WHITE);
        tablaPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(229, 231, 235)),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Barra de filtros (solo vista tickets)
        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filtros.setBackground(Color.WHITE);

        JLabel lblEstado = new JLabel("Estado:");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblEstado.setForeground(new Color(75, 85, 99));
        filtros.add(lblEstado);

        cmbEstado = new JComboBox<>(new String[]{"Todos", "PENDIENTE", "EN_PROCESO", "RESUELTO"});
        cmbEstado.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbEstado.setBackground(Color.WHITE);
        cmbEstado.setPreferredSize(new Dimension(140, 32));
        filtros.add(cmbEstado);

        JLabel lblTipo = new JLabel("Tipo:");
        lblTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTipo.setForeground(new Color(75, 85, 99));
        filtros.add(lblTipo);

        cmbTipo = new JComboBox<>(new String[]{"Todos", "SOFTWARE", "HARDWARE", "RED", "SIN_CLASIFICAR"});
        cmbTipo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbTipo.setBackground(Color.WHITE);
        cmbTipo.setPreferredSize(new Dimension(150, 32));
        filtros.add(cmbTipo);

        btnFiltrar = new JButton("Filtrar");
        btnFiltrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnFiltrar.setForeground(new Color(37, 99, 235));
        btnFiltrar.setBackground(new Color(239, 246, 255));
        btnFiltrar.setFocusPainted(false);
        btnFiltrar.setBorderPainted(false);
        btnFiltrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFiltrar.setBorder(new EmptyBorder(7, 16, 7, 16));
        btnFiltrar.addActionListener(e -> cargarTicketsDesdeAPI());
        filtros.add(btnFiltrar);

        tablaPanel.add(filtros, BorderLayout.NORTH);

        // Tabla
        tabla = new JTable();
        tabla.setRowHeight(52);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setSelectionBackground(new Color(239, 246, 255));
        tabla.setGridColor(new Color(243, 244, 246));
        tabla.setShowVerticalLines(true);
        tabla.setShowHorizontalLines(true);
        tabla.setAutoCreateRowSorter(true);
        tabla.setIntercellSpacing(new Dimension(10, 0));

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(243, 244, 246)));
        scroll.getViewport().setBackground(Color.WHITE);
        tablaPanel.add(scroll, BorderLayout.CENTER);
        panelContenido.add(tablaPanel, BorderLayout.CENTER);

        // Handlers
        tabla.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (vistaActual == Vista.TICKETS && e.getClickCount() == 2 && tabla.getSelectedRow() != -1) {
                    int viewRow = tabla.getSelectedRow();
                    int modelRow = tabla.convertRowIndexToModel(viewRow);
                    Long ticketId = Long.valueOf(modeloTabla.getValueAt(modelRow, 0).toString());
                    String titulo  = modeloTabla.getValueAt(modelRow, 2).toString();
                    String estado  = modeloTabla.getValueAt(modelRow, 3).toString();
                    new TicketDetailsForm(usuarioId, nombreUsuario, rolUsuario, ticketId, titulo, estado, MainFrame.this).setVisible(true);
                }
            }
        });
    }

    private JButton crearBotonHeader(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(37, 99, 235));
        btn.setBackground(new Color(239, 246, 255));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    private JButton crearBotonMenu(String icono, String texto, boolean seleccionado) {
        JButton btn = new JButton("<html><span style='font-size:14px;'>" + icono + "</span>  " + texto + "</html>");
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(seleccionado ? new Color(239, 246, 255) : Color.WHITE);
        btn.setForeground(seleccionado ? new Color(37, 99, 235) : new Color(75, 85, 99));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 12, 10, 12));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!seleccionado) {
                    btn.setBackground(new Color(249, 250, 251));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!seleccionado) {
                    btn.setBackground(Color.WHITE);
                }
            }
        });

        return btn;
    }

    private void actualizarEstadoMenu(Vista nuevaVista) {
        if (btnTickets != null) {
            btnTickets.setBackground(nuevaVista == Vista.TICKETS ? new Color(239, 246, 255) : Color.WHITE);
            btnTickets.setForeground(nuevaVista == Vista.TICKETS ? new Color(37, 99, 235) : new Color(75, 85, 99));
        }
        if (btnTecnicos != null) {
            btnTecnicos.setBackground(nuevaVista == Vista.TECNICOS ? new Color(239, 246, 255) : Color.WHITE);
            btnTecnicos.setForeground(nuevaVista == Vista.TECNICOS ? new Color(37, 99, 235) : new Color(75, 85, 99));
        }
        if (btnUsuarios != null) {
            btnUsuarios.setBackground(nuevaVista == Vista.USUARIOS ? new Color(239, 246, 255) : Color.WHITE);
            btnUsuarios.setForeground(nuevaVista == Vista.USUARIOS ? new Color(37, 99, 235) : new Color(75, 85, 99));
        }
    }

    //  Vista
    private void setVista(Vista nueva) {
        this.vistaActual = nueva;

        // 🧹 Limpiar búsqueda y resetear placeholder al cambiar de vista
        if (searchField != null) {
            String placeholder = switch (nueva) {
                case TICKETS -> "Buscar por título o ID...";
                case TECNICOS, USUARIOS -> "Buscar por nombre o correo...";
            };
            searchField.setText(placeholder);
            searchField.setForeground(new Color(156, 163, 175));

            if (tabla != null && tabla.getRowSorter() != null) {
                ((TableRowSorter<?>) tabla.getRowSorter()).setRowFilter(null);
            }
        }


        // Título + visibilidad filtros + texto botón reporte
        switch (nueva) {
            case TICKETS -> {
                lblTitulo.setText("Tickets de Soporte");
                setBarraFiltrosVisible(true);

                configurarTablaTickets();
                cargarTicketsDesdeAPI();
            }
            case TECNICOS -> {
                lblTitulo.setText("Técnicos");
                setBarraFiltrosVisible(false);
                configurarTablaTecnicos();
                cargarTecnicosDesdeAPI();
            }
            case USUARIOS -> {
                lblTitulo.setText("Usuarios");
                setBarraFiltrosVisible(false);

                configurarTablaUsuarios();
                cargarUsuariosDesdeAPI();
            }
        }
    }

    private void setBarraFiltrosVisible(boolean visible) {
        // La barra de filtros es el BorderLayout.NORTH del panel de tabla (índice 1 en panelContenido)
        JPanel tablaPanel = (JPanel) panelContenido.getComponent(1);
        Component north = ((BorderLayout) tablaPanel.getLayout()).getLayoutComponent(BorderLayout.NORTH);
        north.setVisible(visible);
        tablaPanel.revalidate();
        tablaPanel.repaint();
    }

    // ===== Config tablas =====
    private void configurarTablaTickets() {
        String[] cols = {"ID", "Tipo", "Título", "Estado", "Técnico", "Cliente", "Acciones"};
        modeloTabla = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c == 6; } };
        tabla.setModel(modeloTabla);

        // Configurar anchos de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);

        // Render/Editor Acciones (Eliminar)
        TableColumn acciones = tabla.getColumnModel().getColumn(6);
        acciones.setCellRenderer(new ButtonRenderer());
        acciones.setCellEditor(new ButtonEditor((row) -> {
            Long id = Long.valueOf(modeloTabla.getValueAt(row, 0).toString());
            String tecnicoNombre = Objects.toString(modeloTabla.getValueAt(row, 4), "");
            boolean puedoEliminar = isAdmin() || tecnicoNombre.equalsIgnoreCase(nombreUsuario);
            if (!puedoEliminar) {
                JOptionPane.showMessageDialog(this, "Solo el administrador o el técnico asignado pueden eliminar este ticket.");
                return;
            }
            int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar el ticket #" + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) eliminarTicket(id);
        }));

        beautifyHeader();
        // sirve para personalizar la forma en que se muestran los datos de una columna específica del JTable
        tabla.setDefaultRenderer(Object.class, new EstadoBadgeRenderer(3));
        // asegura sorter para permitir filtro
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
    }

    private void configurarTablaTecnicos() {
        String[] cols = {"ID", "Nombre", "Correo", "Rol", "Acciones"};
        modeloTabla = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c == 4; } };
        tabla.setModel(modeloTabla);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);

        TableColumn acciones = tabla.getColumnModel().getColumn(4);
        acciones.setCellRenderer(new ButtonRenderer("\uD83D\uDDD1\uFE0F Eliminar"));
        acciones.setCellEditor(new ButtonEditor((row) -> {
            if (!isAdmin()) { JOptionPane.showMessageDialog(this, "Solo el administrador puede eliminar usuarios."); return; }
            Long id = Long.valueOf(modeloTabla.getValueAt(row, 0).toString());
            if (Objects.equals(id, usuarioId)) { JOptionPane.showMessageDialog(this, "No puedes eliminar tu propio usuario."); return; }
            int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar técnico ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) eliminarUsuario(id);
        }));

        beautifyHeader();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
    }

    private void configurarTablaUsuarios() {
        String[] cols = {"ID", "Nombre", "Correo", "Rol", "Acciones"};
        modeloTabla = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return c == 4; } };
        tabla.setModel(modeloTabla);

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(200);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);

        TableColumn acciones = tabla.getColumnModel().getColumn(4);
        acciones.setCellRenderer(new ButtonRenderer("🗑 Eliminar"));
        acciones.setCellEditor(new ButtonEditor((row) -> {
            if (!isAdmin()) { JOptionPane.showMessageDialog(this, "Solo el administrador puede eliminar usuarios."); return; }
            Long id = Long.valueOf(modeloTabla.getValueAt(row, 0).toString());
            int opt = JOptionPane.showConfirmDialog(this, "¿Eliminar usuario ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) eliminarUsuario(id);
        }));

        beautifyHeader();

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tabla.setRowSorter(sorter);
    }

    private void beautifyHeader() {
        JTableHeader headerTabla = tabla.getTableHeader();
        headerTabla.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerTabla.setBackground(new Color(249, 250, 251));
        headerTabla.setForeground(new Color(75, 85, 99));
        headerTabla.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)));
        headerTabla.setPreferredSize(new Dimension(headerTabla.getWidth(), 40));
    }

    //  Cargas
    private void cargarTicketsDesdeAPI() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url;
            if (isAdmin()) {
                url = String.format(API_TICKETS_ADMIN, usuarioId);
            } else {
                // Técnico: solo sus tickets
                url = String.format(API_TICKETS_TECNICO, usuarioId, usuarioId);
            }
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200) {
                    JsonArray tickets = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent())).getAsJsonArray();
                    modeloTabla.setRowCount(0);

                    String estadoFiltro = (String) cmbEstado.getSelectedItem();
                    String tipoFiltro = (String) cmbTipo.getSelectedItem();

                    for (JsonElement elem : tickets) {
                        JsonObject t = elem.getAsJsonObject();
                        String estado = t.get("estado").getAsString();
                        String tipo   = t.get("tipo").getAsString();

                        if (!"Todos".equals(estadoFiltro) && !estadoFiltro.equalsIgnoreCase(estado)) continue;
                        if (!"Todos".equals(tipoFiltro)   && !tipoFiltro.equalsIgnoreCase(tipo))   continue;

                        String tecnicoNombre = t.has("tecnico") && !t.get("tecnico").isJsonNull()
                                ? t.get("tecnico").getAsJsonObject().get("nombre").getAsString() : "Usuario sin nombre";
                        String clienteNombre = t.has("cliente") && !t.get("cliente").isJsonNull()
                                ? t.get("cliente").getAsJsonObject().get("nombre").getAsString() : "Usuario sin nombre";

                        modeloTabla.addRow(new Object[]{
                                t.get("id").getAsLong(),
                                tipo,
                                t.get("titulo").getAsString(),
                                estado,
                                tecnicoNombre,
                                clienteNombre,
                                "🗑 Eliminar"
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar tickets (" + response.getCode() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarTecnicosDesdeAPI() {
        String url = String.format(API_USUARIOS_LIST, usuarioId);
        cargarUsuariosGenerico(url, "TECNICO");
    }

    private void cargarUsuariosDesdeAPI() {
        String url = String.format(API_USUARIOS_LIST, usuarioId);
        cargarUsuariosGenerico(url, "CLIENTE");
    }

    private void cargarUsuariosGenerico(String url, String filtroRol) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200) {
                    JsonArray usuarios = JsonParser.parseReader(new InputStreamReader(response.getEntity().getContent())).getAsJsonArray();
                    modeloTabla.setRowCount(0);
                    for (JsonElement elem : usuarios) {
                        JsonObject u = elem.getAsJsonObject();
                        String rol = u.get("rol").getAsString();
                        if (!rol.equalsIgnoreCase(filtroRol)) continue; // Filtramos por rol aquí

                        modeloTabla.addRow(new Object[]{
                                u.get("id").getAsLong(),
                                u.get("nombre").isJsonNull() ? "Usuario sin nombre" : u.get("nombre").getAsString(),
                                u.get("correo").isJsonNull() ? "" : u.get("correo").getAsString(),
                                rol,
                                "🗑 Eliminar"
                        });
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error al cargar usuarios (" + response.getCode() + ")", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Acciones
    private void eliminarTicket(Long id) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format(API_TICKET_DELETE, id, usuarioId);
            HttpDelete request = new HttpDelete(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200 || response.getCode() == 204) {
                    JOptionPane.showMessageDialog(this, "Ticket eliminado.");
                    cargarTicketsDesdeAPI();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar ticket (" + response.getCode() + ")");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.");
        }
    }

    private void eliminarUsuario(Long id) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = String.format(API_USUARIO_DELETE, id, usuarioId);
            HttpDelete request = new HttpDelete(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200 || response.getCode() == 204) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado.");
                    if (vistaActual == Vista.TECNICOS) cargarTecnicosDesdeAPI();
                    if (vistaActual == Vista.USUARIOS) cargarUsuariosDesdeAPI();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar usuario (" + response.getCode() + ")");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.");
        }
    }

    private void onGenerarReporte(String formato) {
        if (!isAdmin()) return;

        String url = switch (vistaActual) {
            case TICKETS  -> String.format(API_REPORTE_TICKETS, usuarioId);
            case TECNICOS -> String.format(API_REPORTE_TECNICOS, usuarioId);
            case USUARIOS -> String.format(API_REPORTE_USUARIOS, usuarioId);
        };

        // Agregar formato dinámico
        url += "&formato=" + formato;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = client.execute(request)) {
                if (response.getCode() == 200) {
                    String ext = formato.equalsIgnoreCase("pdf") ? "pdf" : "json";
                    File temp = Files.createTempFile("reporte_" + vistaActual.name().toLowerCase(), "." + ext).toFile();
                    try (InputStream in = response.getEntity().getContent(); OutputStream out = new FileOutputStream(temp)) {
                        in.transferTo(out);
                    }
                    JOptionPane.showMessageDialog(this, "Reporte generado: " + temp.getAbsolutePath());
                    Desktop.getDesktop().open(temp);
                } else {
                    JOptionPane.showMessageDialog(this, "Error al generar reporte (" + response.getCode() + ")");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor.");
        }
    }

    // ===== Renderers / Editors =====
    private static class EstadoBadgeRenderer extends DefaultTableCellRenderer {
        private final int estadoColumn;
        public EstadoBadgeRenderer(int estadoColumn) { this.estadoColumn = estadoColumn; }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (!isSelected && col == estadoColumn) {
                String estado = Objects.toString(value, "");
                JLabel badge = new JLabel(estado, SwingConstants.CENTER);
                badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
                badge.setOpaque(true);
                badge.setBorder(new EmptyBorder(4, 10, 4, 10));
                switch (estado) {
                    case "PENDIENTE" -> {
                        badge.setBackground(new Color(254, 243, 199));
                        badge.setForeground(new Color(146, 64, 14));
                    }
                    case "EN_PROCESO" -> {
                        badge.setBackground(new Color(219, 234, 254));
                        badge.setForeground(new Color(30, 64, 175));
                    }
                    case "RESUELTO" -> {
                        badge.setBackground(new Color(220, 252, 231));
                        badge.setForeground(new Color(22, 101, 52));
                    }
                    default -> {
                        badge.setBackground(new Color(243, 244, 246));
                        badge.setForeground(new Color(75, 85, 99));
                    }
                }
                return badge;
            }
            if (!isSelected) {
                c.setForeground(new Color(55, 65, 81));
            }
            return c;
        }
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        private final String text;
        public ButtonRenderer() { this("Eliminar"); }
        public ButtonRenderer(String text) {
            this.text = text;
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 12, 6, 12));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(text);
            setBackground(new Color(254, 242, 242));
            setForeground(new Color(185, 28, 28));
            setFont(new Font("Segoe UI", Font.PLAIN, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorderPainted(false);
            return this;
        }
    }

    private static class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button = new JButton();
        private final RowAction onClick;
        private int currentRow = -1;
        public interface RowAction { void run(int row); }
        public ButtonEditor(RowAction onClick) {
            this.onClick = onClick;
            button.setFocusPainted(false);
            button.setBorder(new EmptyBorder(6, 12, 6, 12));
            button.addActionListener(this);
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentRow = row;
            button.setText(" Eliminar");
            button.setBackground(new Color(254, 242, 242));
            button.setForeground(new Color(185, 28, 28));
            button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            button.setBorderPainted(false);
            return button;
        }
        @Override public Object getCellEditorValue() { return " Eliminar"; }
        @Override public void actionPerformed(ActionEvent e) {
            if (currentRow >= 0) onClick.run(currentRow);
            fireEditingStopped();
        }
    }


    public void refrescarTickets() {
        if (vistaActual == Vista.TICKETS) {
            if (!SwingUtilities.isEventDispatchThread()) SwingUtilities.invokeLater(this::cargarTicketsDesdeAPI);
            else cargarTicketsDesdeAPI();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame("adminGeneral", "ADMIN", 1L).setVisible(true));
    }
}