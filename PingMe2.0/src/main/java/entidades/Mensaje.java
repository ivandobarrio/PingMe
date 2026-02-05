package entidades;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String emisor;
    
    @Column
    private String receptor;
    
    @Column
    private String sala;
    
    @Column(nullable = false, length = 2000)
    private String contenido;
    
    @Column(nullable = false)
    private LocalDateTime fecha;
    
    
    public Mensaje() {
        this.fecha = LocalDateTime.now();
    }
    
    
    public Mensaje(String emisor, String receptor, String sala, String contenido) {
        this.emisor = emisor;
        this.receptor = receptor;
        this.sala = sala;
        this.contenido = contenido;
        this.fecha = LocalDateTime.now();
    }
    
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmisor() {
        return emisor;
    }
    
    public void setEmisor(String emisor) {
        this.emisor = emisor;
    }
    
    public String getReceptor() {
        return receptor;
    }
    
    public void setReceptor(String receptor) {
        this.receptor = receptor;
    }
    
    public String getSala() {
        return sala;
    }
    
    public void setSala(String sala) {
        this.sala = sala;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    @Override
    public String toString() {
        return "Mensaje{" +
                "id=" + id +
                ", emisor='" + emisor + '\'' +
                ", receptor='" + receptor + '\'' +
                ", sala='" + sala + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
