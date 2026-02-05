package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import hibernate.HibernateUtil;

public class ServidorPrincipal {
    
    private static final int PUERTO = 5555;
    private static List<ClienteHandler> clientesConectados = new ArrayList<>(); // Lista para mantener los clientes conectados
    
    // Método principal para iniciar el servidor
    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("       SERVIDOR PINGME - INICIANDO     ");
        System.out.println("═══════════════════════════════════════");
        
        // Inicializar Hibernate
        try {
            HibernateUtil.getSessionFactory();
            System.out.println("Hibernate inicializado correctamente");
        } catch (Exception e) {
            System.err.println("Error al inicializar Hibernate");
            e.printStackTrace();
            return;
        }
        
        // Iniciar el servidor y aceptar conexiones de clientes
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en puerto " + PUERTO);
            System.out.println("═══════════════════════════════════════\n");
            
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                ClienteHandler handler = new ClienteHandler(clienteSocket);
                handler.start();
            }
            
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.shutdown();
        }
    }
    
    // Métodos para manejar la lista de clientes conectados
    // Estos métodos son sincronizados para evitar problemas de concurrencia al acceder a la lista desde múltiples hilos
    public static synchronized void agregarCliente(ClienteHandler cliente) {
        clientesConectados.add(cliente);
        System.out.println("Cliente agregado. Total conectados: " + clientesConectados.size());
    }

    // Método para remover un cliente de la lista de clientes conectados
    public static synchronized void removerCliente(ClienteHandler cliente) {
        clientesConectados.remove(cliente);
        System.out.println("Cliente removido. Total conectados: " + clientesConectados.size());
    }
    
    // Método para enviar un mensaje privado a un cliente específico identificado por su nombre de usuario
    // Este método recorre la lista de clientes conectados y envía el mensaje al cliente que coincida con el nombre de usuario destino
    public static synchronized void enviarMensajePrivado(String usuarioDestino, String mensaje) {
        for (ClienteHandler cliente : clientesConectados) {
            if (usuarioDestino.equals(cliente.getUsuarioActual())) {
                cliente.enviar(mensaje);
                break;
            }
        }
    }
    
    public static synchronized void enviarMensajeSala(String codigoSala, String mensaje) {
        
        for (ClienteHandler cliente : clientesConectados) {
            cliente.enviar(mensaje);
        }
    }
    
    public static synchronized void broadcast(String mensaje) {
        for (ClienteHandler cliente : clientesConectados) {
            cliente.enviar(mensaje);
        }
    }
}
