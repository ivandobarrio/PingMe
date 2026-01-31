package entidades;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuarios_email", columnNames = "email")
    }
)
public class Usuario implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental en MySQL
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    // Evitamos 'ñ' en el nombre físico de la columna
    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "sexo", length = 20)
    private String sexo;

    @Column(name = "edad", nullable = false)
    private int edad;
    
    @Column(name = "pregunta", nullable = false, length = 255)
    private String pregunta;
    
    @Column(name = "respuesta", nullable = false, length = 255)
    private String respuesta;

    // ===== Constructores =====
    protected Usuario() {
        // Requerido por JPA
    }

    public Usuario(Long id, String nombre, String contraseña, String email, String sexo, int edad, String pregunta, String respuesta) {
        this.id = id;
        this.nombre = nombre;
        this.contrasena = contraseña;
        this.email = email;
        this.sexo = sexo;
        this.edad = edad;
        this.pregunta = pregunta;
        this.respuesta = respuesta;
    }

    public Usuario(String nombre) {
        this.nombre = nombre;
        this.contrasena = "";
        this.sexo = "";
        this.edad = 21;
    }

    // ===== Getters y Setters =====
    public Long getId() { return id; }
    // No establezcas manualmente el id con IDENTITY; lo asigna la BD.

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getContraseña() { return contrasena; }
    public void setContraseña(String contraseña) { this.contrasena = contraseña; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    
    public String getPregunta() { return pregunta; }
	public void setPregunta(String pregunta) { this.pregunta = pregunta; }

	public String getRespuesta() { return respuesta; }
	public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    @Override
	public String toString() {
		return "Usuario [id=" + id + ", nombre=" + nombre + ", contraseña=" + contrasena + ", email=" + email
				+ ", sexo=" + sexo + ", edad=" + edad + ", pregunta=" + pregunta + ", respuesta=" + respuesta + "]";
	}

	// equals/hashCode por id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario other = (Usuario) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
