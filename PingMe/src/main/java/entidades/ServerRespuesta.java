package entidades;

import java.io.Serializable;

public class ServerRespuesta implements Serializable {
	private int codigo;
	private String mensaje;
	private Object contenido;

	public ServerRespuesta(int codigo, String mensaje, Object contenido) {
		this.codigo = codigo;
		this.mensaje = mensaje;
		this.contenido = contenido;
	}

	@Override
	public String toString() {
		return "ServerRespuesta [codigo=" + codigo + ", mensaje=" + mensaje + ", contenido=" + contenido + "]";
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public Object getContenido() {
		return contenido;
	}

	public void setContenido(Object contenido) {
		this.contenido = contenido;
	}

}
