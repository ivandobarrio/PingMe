package conexionFrontend;

import javafx.application.Platform;

import java.io.*;
import java.net.Socket;


public class TcpConexionBasicaClient {
    
    private static final String HOST = "localhost";
    private static final int PUERTO = 5555;
    
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private Thread hiloEscucha;
    private MensajeCallback callback;
    private boolean conectado;
    
    public TcpConexionBasicaClient() {
        this.conectado = false;
    }
    
//     Método para conectar al servidor
    public boolean conectar() {
        try {
            socket = new Socket(HOST, PUERTO);
            entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            salida = new PrintWriter(socket.getOutputStream(), true);
            conectado = true;
            
            System.out.println("Conectado al servidor en " + HOST + ":" + PUERTO);
            
            
            iniciarHiloEscucha(); 
            
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar: " + e.getMessage());
            return false;
        }
    }
    
//     Método para enviar una línea de texto al servidor
    public void enviarLinea(String linea) {
        if (salida != null && conectado) {
            salida.println(linea);
            System.out.println("→ Enviado: " + linea);
        }
    }
    
//     Método para recibir una línea de texto del servidor
    public String recibirLinea() throws IOException {
        if (entrada != null && conectado) {
            return entrada.readLine();
        }
        return null;
    }
    
//     Método para establecer el callback de mensajes recibidos
//     Este callback se llamará cada vez que se reciba un mensaje del servidor
//     El mensaje se pasará como argumento al método onMensajeRecibido del callback 
//     El callback se ejecutará en el hilo de la interfaz gráfica para evitar problemas de concurrencia
    public void setCallback(MensajeCallback callback) {
        this.callback = callback;
    }
    

//     Método para iniciar el hilo de escucha de mensajes del servidor
//     Este hilo se ejecutará en segundo plano y estará constantemente leyendo mensajes del servidor
//     Cada vez que se reciba un mensaje, se imprimirá en la consola y se llamará al callback para actualizar la interfaz gráfica

    private void iniciarHiloEscucha() {
        hiloEscucha = new Thread(new Runnable() {
            @Override
            public void run() {
            try {
                String linea;
                while (conectado && (linea = entrada.readLine()) != null) {
                    System.out.println("← Recibido: " + linea);
                    
                    final String mensaje = linea;
                    
                    
                    if (callback != null) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                callback.onMensajeRecibido(mensaje);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                if (conectado) {
                    System.err.println("Error en hilo de escucha: " + e.getMessage());
                }
            }
        }});
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
    }
    
    // Método para recibir una cantidad específica de bytes del servidor
    // Este método se usa para recibir archivos o datos binarios del servidor
    public byte[] recibirBytes(int cantidad) throws IOException {
        InputStream in = socket.getInputStream();
        byte[] buffer = new byte[cantidad];
        int leidos = 0;
        
        while (leidos < cantidad) {
            int n = in.read(buffer, leidos, cantidad - leidos);
            if (n == -1) break;
            leidos += n;
        }
        
        return buffer;
    }
    
    // Método para desconectar del servidor
    public void desconectar() {
        try {
            conectado = false;
            
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            
            System.out.println("Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    // Método para verificar si la conexión al servidor está activa
    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
}
