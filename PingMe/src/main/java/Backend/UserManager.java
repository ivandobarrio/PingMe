package Backend;

import entidades.ServerPeticion;
import entidades.ServerRespuesta;
import entidades.Usuario;

public class UserManager {

	// TODAS LAS FUNCIONES RECIBEN UN ServerPeticion Y Devuelven un ServerRespuesta.
	public static ServerRespuesta crearUsuario (ServerPeticion peticion){
		Usuario usuario = new Usuario((String) peticion.getContenido());
		ServerRespuesta respuesta = null;
		boolean existe = false;
		
		if (existe) {
			return new ServerRespuesta(2, "Usuario ya existe", null);
		}
		if (peticion.getContenido() != null) {
			respuesta = new ServerRespuesta(0, "Crear Usuario", usuario);
		} else {
			respuesta = new ServerRespuesta(1, "Error:generico", null);
		}
		return respuesta;
	}
	public static ServerRespuesta eliminarUsuario(ServerPeticion peticion) {
		return null;
	}
}
