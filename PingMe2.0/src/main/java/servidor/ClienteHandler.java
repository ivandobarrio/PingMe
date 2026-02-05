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
    
    // Método principal del hilo que maneja la comunicación con el cliente
    // Lee mensajes del cliente, los procesa y envía respuestas
    // El hilo se mantiene activo hasta que el cliente se desconecta o envía un comando de logout
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
    
    // Método para procesar un mensaje recibido del cliente
    // El mensaje se divide en partes usando el carácter '|' como separador
    // El primer elemento de las partes se interpreta como el comando, y el resto como parámetros
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
    
    // Método para procesar un comando de login
    private void procesarLogin(String[] partes) {
        if (partes.length < 3) {
            enviar("LOGIN_ERROR|Faltan parámetros");
            return;
        }
        
        String username = partes[1];
        String password = partes[2];
        
        // Validar las credenciales usando el DAO de usuarios
        // Si las credenciales son correctas, se establece el usuario actual y se envía una respuesta de éxito al cliente
        if (usuarioDAO.validarCredenciales(username, password)) {
            Usuario usuario = usuarioDAO.buscarPorUsername(username);
            this.usuarioActual = username;
            ServidorPrincipal.agregarCliente(this);
            enviar("LOGIN_OK|" + usuario.getId());
            
            // Enviar la lista de salas a las que el usuario ya pertenece
            for (Sala sala : usuario.getSalas()) {
                enviar("ROOM_JOINED|" + sala.getCodigoUnico() + "|" + sala.getNombre());
            }
        } else {
            enviar("LOGIN_ERROR|Credenciales incorrectas");
        }
    }
    
    // Método para procesar un comando de registro de nuevo usuario
    // Valida los parámetros recibidos, verifica que el username y email no estén ya registrados, y luego crea un nuevo usuario en la base de datos
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
        
        // Crear un nuevo usuario con los datos recibidos y opcionalmente con edad, sexo, pregunta y respuesta de seguridad
        Usuario nuevoUsuario = new Usuario(username, email, password);
        
        // Intentar parsear la edad si se proporciona, y establecer los demás campos opcionales
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
        
         // Guardar el nuevo usuario en la base de datos usando el DAO de usuarios
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
    
    // Método para procesar un comando de creación de sala
    // Valida el nombre de la sala, genera un código único para la sala, crea una nueva sala en la base de datos y asocia la sala al usuario actual
    private void procesarCrearSala(String[] partes) {
        if (partes.length < 2) {
            enviar("CREATE_ROOM_ERROR|Falta nombre de sala");
            return;
        }
        
        // Generar un código único para la sala usando UUID y tomar solo los primeros 8 caracteres para hacerlo más legible
        String nombreSala = partes[1];
        String codigoUnico = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Sala nuevaSala = new Sala(nombreSala, codigoUnico);
        salaDAO.guardar(nuevaSala);
        
        // Asociar la nueva sala al usuario actual en la base de datos
        Usuario usuario = usuarioDAO.buscarPorUsername(usuarioActual);
        if (usuario != null) {
            usuario.getSalas().add(nuevaSala);
            usuarioDAO.actualizar(usuario);
        }
        
        // Enviar una respuesta al cliente con el código único y el nombre de la sala creada
        enviar("ROOM_CREATED|" + codigoUnico + "|" + nombreSala);
    }
    
    // Método para procesar un comando de unirse a una sala existente
    // Valida el código de la sala, busca la sala en la base de datos, y si existe, asocia la sala al usuario actual y envía una respuesta de éxito al cliente
    private void procesarUnirseSala(String[] partes) {
        if (partes.length < 2) {
            enviar("JOIN_ROOM_ERROR|Falta código de sala");
            return;
        }
        
        // Buscar la sala por su código único usando el DAO de salas
        String codigo = partes[1];
        Sala sala = salaDAO.buscarPorCodigo(codigo);
        
        if (sala != null) {
            
        	// Asociar la sala al usuario actual en la base de datos
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
    
    // Método para procesar un comando de envío de mensaje privado
    // Valida los parámetros recibidos, crea un nuevo mensaje en la base de datos, y luego envía el mensaje al destinatario si está conectado
    private void procesarMensajePrivado(String[] partes) {
        if (partes.length < 3) {
            enviar("MSG_ERROR|Faltan parámetros");
            return;
        }
        
        String destinatario = partes[1];
        String contenido = partes[2];
        
        
        Mensaje mensaje = new Mensaje(usuarioActual, destinatario, null, contenido);
        mensajeDAO.guardar(mensaje);
        
        // Enviar el mensaje al destinatario si está conectado, usando el método estático del servidor principal para enviar mensajes privados
        ServidorPrincipal.enviarMensajePrivado(destinatario, 
            "MSG_PRIVATE_EVENT|" + usuarioActual + "|" + contenido);
        
        // Enviar una respuesta de éxito al cliente que envió el mensaje
        enviar("MSG_SENT|OK");
    }
    
    // Método para procesar un comando de envío de mensaje a una sala
    // Valida los parámetros recibidos, crea un nuevo mensaje en la base de datos, y luego envía el mensaje a todos los miembros de la sala usando el método estático del servidor principal para enviar mensajes a salas
    private void procesarMensajeSala(String[] partes) {
        if (partes.length < 3) {
            enviar("MSG_ERROR|Faltan parámetros");
            return;
        }
        
        String codigoSala = partes[1];
        String contenido = partes[2];
        
        // Crear un nuevo mensaje con el emisor, el código de la sala y el contenido, y guardarlo en la base de datos usando el DAO de mensajes
        Mensaje mensaje = new Mensaje(usuarioActual, null, codigoSala, contenido);
        mensajeDAO.guardar(mensaje);
        
        // Enviar el mensaje a todos los miembros de la sala usando el método estático del servidor principal para enviar mensajes a salas
        ServidorPrincipal.enviarMensajeSala(codigoSala, 
            "MSG_ROOM_EVENT|" + codigoSala + "|" + usuarioActual + "|" + contenido);
        
        enviar("MSG_SENT|OK");
    }
    
    // Método para procesar un comando de generación de informe de usuarios
    private void procesarInformeUsuarios() {
        // Generar el informe de usuarios usando la clase GeneradorInformes, que devuelve un array de bytes con el contenido del PDF
    	try {
            byte[] pdfBytes = GeneradorInformes.generarInformeUsuarios();
            enviar("PDF_SIZE|" + pdfBytes.length);
            
            // Enviar el PDF al cliente usando el socket directamente, ya que es un archivo binario
            OutputStream out = socket.getOutputStream();
            out.write(pdfBytes);
            out.flush();
            
            System.out.println("PDF enviado: " + pdfBytes.length + " bytes");
        } catch (Exception e) {
            enviar("PDF_ERROR|" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método para procesar un comando de generación de historia de mensajes de una sala
    private void procesarHistoriaSala(String[] partes) {
        if (partes.length < 2) {
            enviar("HISTORIA_ERROR|Falta código de sala");
            return;
        }
        
        // Obtener la historia de mensajes de la sala usando el DAO de mensajes, y enviar cada mensaje al cliente
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

    // Método para procesar un comando de listado de usuarios conectados
    // Obtiene la lista de todos los usuarios registrados en la base de datos usando el DAO de usuarios, y envía la lista al cliente, excluyendo al usuario actual
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
    
    // Método para procesar un comando de generación de historia de mensajes privados con otro usuario
    private void procesarHistoriaPrivada(String[] partes) {
        if (partes.length < 2) {
            enviar("HISTORIA_ERROR|Falta usuario destino");
            return;
        }
        
        // Obtener la historia de mensajes privados entre el usuario actual y el otro usuario usando el DAO de mensajes, y enviar cada mensaje al cliente
        String otroUsuario = partes[1];
        List<Mensaje> mensajes = mensajeDAO.obtenerMensajesPrivados(usuarioActual, otroUsuario);
        
        if (mensajes == null || mensajes.isEmpty()) {
            enviar("HISTORIA_PRIVADA|" + otroUsuario + "|0");
            return;
        }
        
        // Enviar la cantidad de mensajes en la historia privada, seguida de cada mensaje con su emisor, contenido y fecha
        enviar("HISTORIA_PRIVADA|" + otroUsuario + "|" + mensajes.size());
        for (Mensaje msg : mensajes) {
            enviar("HISTORIA_MSG|" + msg.getEmisor() + "|" + msg.getContenido() + "|" + msg.getFecha());
        }
    }
    
    // Método para enviar un mensaje al cliente a través del PrintWriter
    public void enviar(String mensaje) {
        if (salida != null) {
            salida.println(mensaje);
            System.out.println("→ Enviado: " + mensaje);
        }
    }
    
    // Método para obtener el nombre de usuario del cliente actual, que puede ser utilizado por el servidor principal para identificar al cliente
    public String getUsuarioActual() {
        return usuarioActual;
    }
    
    // Método para desconectar al cliente, que se llama cuando el hilo termina o cuando el cliente envía un comando de logout
    // El método elimina al cliente de la lista de clientes conectados en el servidor principal, cierra el socket y muestra un mensaje en la consola indicando que el cliente se ha desconectado
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
