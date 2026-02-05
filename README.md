# PingMe

Aplicación de mensajería instantánea entre usuarios y sala, basada en una arquitectura Cliente-Servidor que realiza una comunicación en tiempo real mediante sockets TCP, gestión de persistencia con Hibernate y generación de informes con JasperReports.

## Características principales

### Mensajeria a tiempo real
Chats en salas públicas con códigos de acceso y mensajes privados entre usuarios registrados.

### Persistencia total
Registro en una base de datos MySQL local los datos de los usuarios, mensajes y salas.

### Gestión de tickets
Soporte técnico para usuarios y administradores integrado en el sistema.

### Informes profesionales
Generación de documentos PDF con datos de usuarios.

### Interfaz moderna
Diseñada con JavaFX y estilos mediante CSS

## Tecnologías utilizadas

- *Lenguaje:* Java 21
- *Interfaz Gráfica:* JavaFX 21
- *Persistencia:* Hibernate 5.6.15
- *Base de Datos:* MySQL 8+
- *Gestión de proyecto:* Maven
- *Informes:* JasperReports 6.20.0

## Requisitos del sistema

- *JDK:* 21 o superior
- *MySQL:* 8.0 o superior
- *Maven:* 3.6 o superior
- *Sistema Operativo:* Windows, Linux o macOS

## Estructura del proyecto


PingMe2.0/
├── src/
│   └── main/
│       ├── java/
│       │   ├── app/
│       │   │   └── App.java                    
│       │   ├── servidor/
│       │   │   ├── ServidorPrincipal.java      
│       │   │   ├── ClienteHandler.java         
│       │   │   └── GeneradorInformes.java     
│       │   ├── frontend/
│       │   │   ├── LoginController.java       
│       │   │   ├── RegistroController.java    
│       │   │   ├── PantallaInicio.java       
│       │   │   └── RecuperarContraseñaController.java
│       │   ├── conexionFrontend/
│       │   │   ├── TcpConexionBasicaClient.java 
│       │   │   └── MensajeCallback.java         
│       │   ├── entidades/
│       │   │   ├── Usuario.java                
│       │   │   ├── Sala.java                   
│       │   │   └── Mensaje.java               
│       │   ├── dao/
│       │   │   ├── UsuarioDAO.java             
│       │   │   ├── SalaDAO.java              
│       │   │   └── MensajeDAO.java            
│       │   └── hibernate/
│       │       └── HibernateUtil.java          
│       └── resources/
│           ├── hibernate.cfg.xml               
│           ├── Login.fxml                      
│           ├── Registro.fxml                  
│           ├── PantallaInicio.fxml           
│           ├── RecuperarContraseña.fxml       
│           └── styles.css                      
├── pom.xml                                   
└── README.md


## Instalación y configuración

### 1. Clonar el repositorio

bash
git clone <url-del-repositorio>
cd PingMe2.0


### 2. Configurar la base de datos

#### Crear la base de datos MySQL

sql
CREATE DATABASE pingme CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


*Nota:* Hibernate creará automáticamente las tablas necesarias gracias a la configuración hibernate.hbm2ddl.auto=update.

#### Configurar credenciales

Editar el archivo src/main/resources/hibernate.cfg.xml:

xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/pingme?createDatabaseIfNotExist=true&amp;useSSL=false&amp;serverTimezone=UTC</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">tu_contraseña</property>


### 3. Compilar el proyecto

bash
mvn clean install


### 4. Configurar el host del servidor

En la clase TcpConexionBasicaClient.java, modificar la constante HOST:

- *Para servidor local:* private static final String HOST = "localhost";
- *Para servidor remoto:* private static final String HOST = "dirección_ip_del_servidor";

El puerto predeterminado es 5555.

## Ejecución

### 1. Iniciar el servidor

bash
# Opción 1: Desde IDE
Ejecutar la clase: servidor.ServidorPrincipal

# Opción 2: Desde Maven
mvn exec:java -Dexec.mainClass="servidor.ServidorPrincipal"


El servidor se iniciará en el puerto 5555 y quedará esperando conexiones.

### 2. Iniciar el cliente

bash
# Desde Maven
mvn javafx:run


*Nota:* Si ejecutas desde un IDE, asegúrate de configurar los argumentos de JavaFX en las opciones de ejecución.

## Protocolo de comunicación

La comunicación entre cliente y servidor utiliza un protocolo textual donde los comandos y parámetros están separados por el carácter |.

### Comandos del cliente al servidor

| Comando | Formato | Descripción |
|---------|---------|-------------|
| LOGIN | LOGIN\|username\|password | Iniciar sesión |
| REGISTER | REGISTER\|username\|email\|password\|pregunta\|respuesta | Registrar nuevo usuario |
| CREATE_ROOM | CREATE_ROOM\|nombreSala | Crear una nueva sala |
| JOIN_ROOM | JOIN_ROOM\|codigoSala | Unirse a una sala existente |
| MSG_PRIVATE | MSG_PRIVATE\|destinatario\|mensaje | Enviar mensaje privado |
| MSG_ROOM | MSG_ROOM\|codigoSala\|mensaje | Enviar mensaje a una sala |
| LIST_USERS | LIST_USERS | Obtener lista de usuarios registrados |
| HISTORIA_PRIVADA | HISTORIA_PRIVADA\|otroUsuario | Obtener historial de chat privado |
| HISTORIA_SALA | HISTORIA_SALA\|codigoSala | Obtener historial de una sala |
| REPORT_USERS | REPORT_USERS | Generar informe PDF de usuarios |
| LOGOUT | LOGOUT | Cerrar sesión |

### Respuestas del servidor al cliente

| Respuesta | Formato | Descripción |
|-----------|---------|-------------|
| LOGIN_OK | LOGIN_OK\|userId | Login exitoso |
| LOGIN_ERROR | LOGIN_ERROR\|mensaje | Error en login |
| REGISTER_OK | REGISTER_OK | Registro exitoso |
| REGISTER_ERROR | REGISTER_ERROR\|mensaje | Error en registro |
| ROOM_CREATED | ROOM_CREATED\|codigo\|nombre | Sala creada |
| ROOM_JOINED | ROOM_JOINED\|codigo\|nombre | Usuario unido a sala |
| MSG_RECEIVED | MSG_RECEIVED\|remitente\|mensaje\|timestamp | Mensaje privado recibido |
| ERROR | ERROR\|mensaje | Error genérico |

## Detalles técnicos

### Persistencia avanzada con relación @ManyToMany

Hemos implantado una relación de muchos a muchos entre las entidades Usuario y Sala. Esta relación se almacena en una tabla intermedia de MySQL, la cual se gestiona automáticamente por Hibernate. Para que el servidor pueda acceder a esta información y las recupere todas las salas a las que el usuario pertenece, se ha utilizado FetchType.EAGER, permitiendo una conexión fluida a las conversaciones previas sin necesidad de introducir códigos de acceso.

### Seguridad de recuperación de contraseña

Durante el registro, se almacenan una pregunta y respuesta de seguridad, la cual se guarda en la base de datos junto con todos los datos del usuario. En caso de que el usuario se haya olvidado de la contraseña y no pueda acceder a su inicio de sesión, el servidor hace una consulta a la base de datos para verificar la identidad del usuario problemático antes de permitir el envío de sus credenciales.

### Arquitectura del servidor

La arquitectura del servidor hace uso del multihilo. El ServidorPrincipal actúa como un nodo de escucha (ServerSocket) que mediante un bucle de aceptación delega cada nueva conexión a una instancia independiente del ClienteHandler. Cada hilo gestiona su propio flujo de entrada y salida (InputStream/OutputStream), lo que hace que el servidor pueda atender múltiples usuarios de forma simultánea.

### Patrón de diseño

Uso de callbacks por la interfaz MensajeCallback para desacoplar la lógica de red de la interfaz usuario.

## Guía de uso

### Cómo registrarse

1. Al iniciar la aplicación, verás la ventana de login
2. Haz clic en el botón *"Registrarse"*
3. Completa el formulario con:
   - Nombre de usuario
   - Correo electrónico
   - Contraseña
   - Pregunta de seguridad
   - Respuesta de seguridad
4. Haz clic en *"Registrar"*

### Crear una sala

1. Una vez dentro de la aplicación, haz clic en *"Crear Sala"*
2. Introduce el nombre de la sala
3. Se generará un código único que podrás compartir con otros usuarios

### Unirse a una sala

1. Haz clic en *"Unirse a Sala"*
2. Introduce el código de la sala proporcionado por el creador
3. La sala aparecerá en tu lista de conversaciones

### Enviar un mensaje privado

1. En la sección de *"Chats"*, aparecerán todos los usuarios registrados
2. Haz clic en el usuario con el que quieres chatear
3. Escribe tu mensaje y pulsa Enter o haz clic en *"Enviar"*

### Recuperar contraseña

1. En la pantalla de login, haz clic en *"¿Olvidaste tu contraseña?"*
2. Introduce tu nombre de usuario
3. Responde a la pregunta de seguridad que configuraste durante el registro
4. Si la respuesta es correcta, se te enviara un correo con tu contraseña

## Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo LICENSE para más detalles.

## Autores

Jose Javier Carballo
Janire Martínez
Ivan Dobarrio
Alejandro Onaindia
Conrado Ayuso

