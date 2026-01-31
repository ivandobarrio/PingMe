package services;

import java.util.List;

import DAO.MensajeDAO;
import entidades.Mensaje;

public class MensajeService {

	private final MensajeDAO dao = new MensajeDAO();

	public void enviar(Mensaje m) {
		dao.insertarMensaje(m);
	}

	public List<Mensaje> historialDM(String a, String b) {
		return dao.obtenerConversacionDM(a, b);
	}

	public List<Mensaje> historialSala(String nombreSala) {
		return dao.obtenerPorReceptor(nombreSala); // si guardas receptor=nombreSala
	}

}
