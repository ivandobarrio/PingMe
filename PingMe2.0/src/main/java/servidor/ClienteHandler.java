package servidor;

import dao.*;
import entidades.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

public class ClienteHandler extends Thread {
    
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private String usuarioActual;
    private boolean conectado;
    
    
    private UsuarioDAO usuarioDAO;
    private SalaDAO salaDAO;
    private MensajeDAO mensajeDAO;
    
    public ClienteHandler(Socket socket) {
        this.socket = socket;
        this.conectado = true;
        this.usuarioDAO = new UsuarioDAO();
        this.salaDAO = new SalaDAO();
        this.mensajeDAO = new MensajeDAO();
    }
    
    @Override
    public void run() {
        try {
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Nuevo app conectado: " + socket.getInetAddress());
            
            String linea;
            while (conectado && (linea = entrada.readLine()) != null) {
                System.out.println("← Recibido: " + linea);
                procesarMensaje(linea);
            }
            
        } catch (IOException e) {
            System.err.println("Error en ClienteHandler: " + e.getMessage());
        } finally {
            desconectar();
        }
    }
    
    private void procesarMensaje(String mensaje) {
        String[] partes = mensaje.split("\\|", -1);
        String comando = partes[0];
        
        try {
            switch (comando) {
                case "LOGIN":
                    procesarLogin(partes);
                    break;
                    
                case "REGISTER":
                    procesarRegistro(partes);
                    break;
                    
                case "CREATE_ROOM":
                    procesarCrearSala(partes);
                    break;
                    
                case "JOIN_ROOM":
                    procesarUnirseSala(partes);
                    break;
                    
                case "MSG_PRIVATE":
                    procesarMensajePrivado(partes);
                    break;
                    
                case "MSG_ROOM":
                    procesarMensajeSala(partes);
                    break;
                    
                case "REPORT_USERS":
                    procesarInformeUsuarios();
                    break;
                    
                case "LIST_USERS":
                    procesarListarUsuarios();
                    break;
                    
                case "HISTORIA_PRIVADA":
                    procesarHistoriaPrivada(partes);
                    break;
                    
                case "HISTORIA_SALA":
                    procesarHistoriaSala(partes);
                    break;
                    
                case "LOGOUT":
                    conectado = false;
                    enviar("LOGOUT_OK");
                    break;
                    
                default:
                    enviar("ERROR|Comando desconocido: " + comando);
            }
        } catch (Exception e) {
            enviar("ERROR|" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void procesarLogin(String[] partes) {
        if (partes.length < 3) {
            enviar("LOGIN_ERROR|Faltan parámetros");
            return;
        }
        
        String username = partes[1];
        String password = partes[2];
        
        if (usuarioDAO.validarCredenciales(username, password)) {
            Usuario usuario = usuarioDAO.buscarPorUsername(username);
            this.usuarioActual = username;
            ServidorPrincipal.agregarCliente(this);
            enviar("LOGIN_OK|" + usuario.getId());
            
            
            for (Sala sala : usuario.getSalas()) {
                enviar("ROOM_JOINED|" + sala.getCodigoUnico() + "|" + sala.getNombre());
            }
        } else {
            enviar("LOGIN_ERROR|Credenciales incorrectas");
        }
    }
    
    private void procesarRegistro(String[] partes) {
        
        if (partes.length < 4) {
            enviar("REGISTER_ERROR|Faltan parámetros obligatorios");
            return;
        }
        
        String username = partes[1];
        String email = partes[2];
        String password = partes[3];
        
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            enviar("REGISTER_ERROR|Campos obligatorios vacíos");
            return;
        }
        
        
        if (usuarioDAO.buscarPorUsername(username) != null) {
            enviar("REGISTER_ERROR|El usuario ya existe");
            return;
        }
        
        if (usuarioDAO.buscarPorEmail(email) != null) {
            enviar("REGISTER_ERROR|El email ya está registrado");
            return;
        }
        
        
        Usuario nuevoUsuario = new Usuario(username, email, password);
        
        
        try {
            if (partes.length > 4 && !partes[4].isEmpty()) {
                nuevoUsuario.setEdad(Integer.parseInt(partes[4]));
            }
            if (partes.length > 5) nuevoUsuario.setSexo(partes[5]);
            if (partes.length > 6) nuevoUsuario.setPreguntaSeguridad(partes[6]);
            if (partes.length > 7) nuevoUsuario.setRespuestaSeguridad(partes[7]);
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear edad: " + e.getMessage());
        }
        
        try {
            System.out.println("Intentando guardar usuario: " + nuevoUsuario.getUsername());
            usuarioDAO.guardar(nuevoUsuario);
            System.out.println("Usuario guardado con ID: " + nuevoUsuario.getId());
            enviar("REGISTER_OK|" + nuevoUsuario.getId());
        } catch (Exception e) {
            System.err.println("Error al guardar usuario en BD: " + e.getMessage());
            enviar("REGISTER_ERROR|Error al guardar en base de datos: " + e.getMessage());
        }
    }
    
    private void procesarCrearSala(String[] partes) {
        if (partes.length < 2) {
            enviar("CREATE_ROOM_ERROR|Falta nombre de sala");
            return;
        }
        
        String nombreSala = partes[1];
        String codigoUnico = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Sala nuevaSala = new Sala(nombreSala, codigoUnico);
        salaDAO.guardar(nuevaSala);
        
        
        Usuario usuario = usuarioDAO.buscarPorUsername(usuarioActual);
        if (usuario != null) {
            usuario.getSalas().add(nuevaSala);
            usuarioDAO.actualizar(usuario);
        }
        
        enviar("ROOM_CREATED|" + codigoUnico + "|" + nombreSala);
    }
    
    private void procesarUnirseSala(String[] partes) {
        if (partes.length < 2) {
            enviar("JOIN_ROOM_ERROR|Falta código de sala");
            return;
        }
        
        String codigo = partes[1];
        Sala sala = salaDAO.buscarPorCodigo(codigo);
        
        if (sala != null) {
            
            Usuario usuario = usuarioDAO.buscarPorUsername(usuarioActual);
            if (usuario != null) {
                usuario.getSalas().add(sala);
                usuarioDAO.actualizar(usuario);
            }
            enviar("ROOM_JOINED|" + codigo + "|" + sala.getNombre());
        } else {
            enviar("JOIN_ROOM_ERROR|Sala no encontrada");
        }
    }
    
    private void procesarMensajePrivado(String[] partes) {
        if (partes.length < 3) {
            enviar("MSG_ERROR|Faltan parámetros");
            return;
        }
        
        String destinatario = partes[1];
        String contenido = partes[2];
        
        
        Mensaje mensaje = new Mensaje(usuarioActual, destinatario, null, contenido);
        mensajeDAO.guardar(mensaje);
        
        
        ServidorPrincipal.enviarMensajePrivado(destinatario, 
            "MSG_PRIVATE_EVENT|" + usuarioActual + "|" + contenido);
        
        enviar("MSG_SENT|OK");
    }
    
    private void procesarMensajeSala(String[] partes) {
        if (partes.length < 3) {
            enviar("MSG_ERROR|Faltan parámetros");
            return;
        }
        
        String codigoSala = partes[1];
        String contenido = partes[2];
        
        
        Mensaje mensaje = new Mensaje(usuarioActual, null, codigoSala, contenido);
        mensajeDAO.guardar(mensaje);
        
        
        ServidorPrincipal.enviarMensajeSala(codigoSala, 
            "MSG_ROOM_EVENT|" + codigoSala + "|" + usuarioActual + "|" + contenido);
        
        enviar("MSG_SENT|OK");
    }
    
    private void procesarInformeUsuarios() {
        try {
            byte[] pdfBytes = GeneradorInformes.generarInformeUsuarios();
            enviar("PDF_SIZE|" + pdfBytes.length);
            
            
            OutputStream out = socket.getOutputStream();
            out.write(pdfBytes);
            out.flush();
            
            System.out.println("PDF enviado: " + pdfBytes.length + " bytes");
        } catch (Exception e) {
            enviar("PDF_ERROR|" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void procesarHistoriaSala(String[] partes) {
        if (partes.length < 2) {
            enviar("HISTORIA_ERROR|Falta código de sala");
            return;
        }
        
        String codigoSala = partes[1];
        List<Mensaje> mensajes = mensajeDAO.obtenerMensajesDeSala(codigoSala);
        
        if (mensajes == null || mensajes.isEmpty()) {
            enviar("HISTORIA_SALA|" + codigoSala + "|0");
            return;
        }
        
        enviar("HISTORIA_SALA|" + codigoSala + "|" + mensajes.size());
        
        for (Mensaje msg : mensajes) {
            enviar("HISTORIA_MSG|" + msg.getEmisor() + "|" + msg.getContenido() + "|" + msg.getFecha());
        }
    }

    
    private void procesarListarUsuarios() {
        List<Usuario> usuarios = usuarioDAO.listarTodos();
        if (usuarios == null) {
            enviar("USERS_LIST|0");
            return;
        }
        
        
        enviar("USERS_LIST|" + (usuarios.size() - 1));
        for (Usuario u : usuarios) {
            if (!u.getUsername().equals(usuarioActual)) {
                enviar("USER_ITEM|" + u.getUsername());
            }
        }
    }
    
    private void procesarHistoriaPrivada(String[] partes) {
        if (partes.length < 2) {
            enviar("HISTORIA_ERROR|Falta usuario destino");
            return;
        }
        
        String otroUsuario = partes[1];
        List<Mensaje> mensajes = mensajeDAO.obtenerMensajesPrivados(usuarioActual, otroUsuario);
        
        if (mensajes == null || mensajes.isEmpty()) {
            enviar("HISTORIA_PRIVADA|" + otroUsuario + "|0");
            return;
        }
        
        enviar("HISTORIA_PRIVADA|" + otroUsuario + "|" + mensajes.size());
        for (Mensaje msg : mensajes) {
            enviar("HISTORIA_MSG|" + msg.getEmisor() + "|" + msg.getContenido() + "|" + msg.getFecha());
        }
    }
    
    public void enviar(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
            System.out.println("→ Enviado: " + mensaje);
        }
    }
    
    public String getUsuarioActual() {
        return usuarioActual;
    }
    
    private void desconectar() {
        try {
            ServidorPrincipal.removerCliente(this);
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Cliente desconectado: " + usuarioActual);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
