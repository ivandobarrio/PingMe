package entidades;

import java.io.Serializable;
import java.util.Date;

public class Mensaje implements Serializable{
	private String id;
	private String contenido;
	private Date fecha;
	private String id_emisor;
	private String id_receptor;
	
	public Mensaje() {
		this.id = "2";
		this.contenido = "ko";
		this.fecha = new Date();
		this.id_emisor = "1";
		this.id_receptor = "4";
	}

	@Override
	public String toString() {
		return "Mensaje [id=" + id + ", contenido=" + contenido + ", fecha=" + fecha + ", id_emisor=" + id_emisor
				+ ", id_receptor=" + id_receptor + "]";
	}
	
	

}
