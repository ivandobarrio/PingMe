package entidades;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
//fafa
@Entity
@Table(name = "salas")
public class Sala implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "historial", columnDefinition = "TEXT")
    private String historial;

    @ManyToMany
    @JoinTable(
        name = "sala_integrantes",
        joinColumns = @JoinColumn(name = "sala_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> integrantes = new HashSet<>();

    protected Sala() {
    }

    public Sala(String nombre) {
        this.nombre = nombre;
        this.fechaCreacion = LocalDateTime.now();
        this.historial = "";
    }

    public Sala(String nombre, String historial) {
        this.nombre = nombre;
        this.fechaCreacion = LocalDateTime.now();
        this.historial = historial;
    }

    // ===== Getters y Setters =====
    public Long getId() { return id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getHistorial() { return historial; }
    public void setHistorial(String historial) { this.historial = historial; }

    public Set<Usuario> getIntegrantes() { return integrantes; }
    public void setIntegrantes(Set<Usuario> integrantes) { this.integrantes = integrantes; }

    public void addIntegrante(Usuario usuario) {
        integrantes.add(usuario);
    }

    public void removeIntegrante(Usuario usuario) {
        integrantes.remove(usuario);
    }

    // ===== toString =====
    @Override
    public String toString() {
        return "Sala [id=" + id + ", nombre=" + nombre +
                ", fechaCreacion=" + fechaCreacion +
                ", integrantes=" + integrantes.size() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sala)) return false;
        Sala other = (Sala) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
