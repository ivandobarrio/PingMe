package services;

import java.util.List;

import DAO.UsuarioDAO;
import entidades.Usuario;

public class UsuarioService {

	private final UsuarioDAO dao = new UsuarioDAO();

	public Usuario crearUsuario(Usuario u) {

		if (u == null)
			throw new IllegalArgumentException("Usuario nulo");
		if (u.getNombre() == null || u.getNombre().isBlank())
			throw new IllegalArgumentException("Nombre requerido");
		if (u.getEmail() == null || u.getEmail().isBlank())
			throw new IllegalArgumentException("Email requerido");
		if (u.getContraseña() == null || u.getContraseña().isBlank())
			throw new IllegalArgumentException("Contraseña requerida");
		if (dao.obtenerPorEmail(u.getEmail()) != null) {
			throw new IllegalStateException("El email ya está registrado: " + u.getEmail());
		}

		if (u.getSexo() == null)
			u.setSexo("");
		if (u.getEdad() == 0)
			u.setEdad(21);
		if (u.getPregunta() == null)
			u.setPregunta("pregunta");
		if (u.getRespuesta() == null)
			u.setRespuesta("respuesta");

		dao.insertarUsuario(u);
		return u;
	}

	public Usuario validarLogin(String usuarioOEmail, String contrasena) {
		if (usuarioOEmail == null || contrasena == null)
			return null;

		Usuario u = null;
		if (usuarioOEmail.contains("@")) {
			u = dao.obtenerPorEmail(usuarioOEmail);
		} else {
			u = dao.obtenerPorNombre(usuarioOEmail);
		}
		if (u == null)
			return null;
		
		if (contrasena.equals(u.getContraseña())) {
			return u;
		}
		return null;
	}

	public List<Usuario> listarTodos() {
		return dao.obtenerTodos();
	}

	public Usuario obtenerPorEmail(String email) {
		return dao.obtenerPorEmail(email);
	}

	public void eliminarPorId(Long id) {
		dao.eliminarPorId(id);
	}

}
