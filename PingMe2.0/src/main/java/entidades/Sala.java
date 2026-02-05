package entidades;

import javax.persistence.*;

@Entity
@Table(name = "salas")
public class Sala {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(unique = true, nullable = false)
    private String codigoUnico;
    
    
    public Sala() {
    }
    
    
    public Sala(String nombre, String codigoUnico) {
        this.nombre = nombre;
        this.codigoUnico = codigoUnico;
    }
    
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCodigoUnico() {
        return codigoUnico;
    }
    
    public void setCodigoUnico(String codigoUnico) {
        this.codigoUnico = codigoUnico;
    }
    
    @Override
    public String toString() {
        return "Sala{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", codigoUnico='" + codigoUnico + '\'' +
                '}';
    }
}
