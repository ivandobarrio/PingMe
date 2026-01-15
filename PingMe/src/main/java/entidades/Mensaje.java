
package entidades;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "mensajes")
public class Mensaje implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental en MySQL
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "contenido", nullable = false, length = 500)
    private String contenido;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "fecha", nullable = false)
    private Date fecha;

    @Column(name = "id_emisor", nullable = false, length = 36)
    private String idEmisor;

    @Column(name = "id_receptor", nullable = false, length = 36)
    private String idReceptor;

    // ===== Constructores =====
    public Mensaje() {
        // Requerido por JPA
    }

    public Mensaje(String contenido, String idEmisor, String idReceptor) {
        this.contenido = contenido;
        this.fecha = new Date();
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
    }

    // ===== Getters y Setters =====
    public Long getId() { return id; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getIdEmisor() { return idEmisor; }
    public void setIdEmisor(String idEmisor) { this.idEmisor = idEmisor; }

    public String getIdReceptor() { return idReceptor; }
    public void setIdReceptor(String idReceptor) { this.idReceptor = idReceptor; }

    @Override
    public String toString() {
        return "Mensaje [id=" + id + ", contenido=" + contenido + ", fecha=" + fecha +
               ", idEmisor=" + idEmisor + ", idReceptor=" + idReceptor + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mensaje)) return false;
        Mensaje other = (Mensaje) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
