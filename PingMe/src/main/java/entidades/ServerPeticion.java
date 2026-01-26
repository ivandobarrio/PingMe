package entidades;

import java.io.Serializable;

public class ServerPeticion implements Serializable {
	private TipoComando comando;
	private Object contenido;

	public ServerPeticion(TipoComando comando, Object contenido) {
		this.comando = comando;
		this.contenido = contenido;
	}

	@Override
	public String toString() {
		return "ServerPeticion [comando=" + comando + ", contenido=" + contenido + "]";
	}

	public TipoComando getComando() {
		return comando;
	}

	public void setComando(TipoComando comando) {
		this.comando = comando;
	}

	public Object getContenido() {
		return contenido;
	}

	public void setContenido(Object contenido) {
		this.contenido = contenido;
	}

}
