package entidades;

import java.io.Serializable;

public class ConversacionDTO implements Serializable {

    public String tipo; 
    public String a;    
    public String b;    

    public ConversacionDTO(String tipo, String a, String b) {
        this.tipo = tipo;
        this.a = a;
        this.b = b;
    }
}

