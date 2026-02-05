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
    
    public void enviarLinea(String linea) {
        if (salida != null && conectado) {
            salida.println(linea);
            System.out.println("→ Enviado: " + linea);
        }
    }
    
    public String recibirLinea() throws IOException {
        if (entrada != null && conectado) {
            return entrada.readLine();
        }
        return null;
    }
    
    public void setCallback(MensajeCallback callback) {
        this.callback = callback;
    }
    
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
    
    public boolean isConectado() {
        return conectado && socket != null && !socket.isClosed();
    }
}
