package services;

import java.util.List;

import DAO.SalaDAO;
import entidades.Sala;
import entidades.Usuario;

public class SalaService {
	private final SalaDAO salaDAO = new SalaDAO();

	public Sala crearSala(String nombre) {
		Sala s = new Sala(nombre);
		salaDAO.insertarSala(s);
		return s;
	}

	public List<Sala> listarTodas() {
		return salaDAO.obtenerTodas();
	}

	public Sala obtenerPorNombre(String nombre) {
		return salaDAO.obtenerPorNombre(nombre);
	}

	public List<Sala> obtenerSalasDeUsuario(Long userId) {
		return salaDAO.obtenerSalasDeUsuario(userId);
	}

	public void agregarUsuario(Long salaId, Usuario u) {
		salaDAO.agregarUsuarioASala(salaId, u);
	}

	public void eliminarUsuario(Long salaId, Usuario u) {
		salaDAO.eliminarUsuarioDeSala(salaId, u);
	}
}
