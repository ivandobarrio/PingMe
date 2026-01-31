package entidades;

import java.io.Serializable;

public class CredencialesDTO implements Serializable {
    public String usuarioOEmail;
    public String contrasena;
    public CredencialesDTO(String u, String c){ this.usuarioOEmail=u; this.contrasena=c; }
}

