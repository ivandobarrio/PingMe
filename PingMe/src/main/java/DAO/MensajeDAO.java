package DAO;

import entidades.Mensaje;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.List;

public class MensajeDAO {

	// INSERTAR MENSAJE
	public void insertarMensaje(Mensaje mensaje) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			if (mensaje.getFecha() == null) {
				mensaje.setFecha(new Date());
			}

			session.save(mensaje);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// SELECCIONAR MENSAJE POR ID
	public Mensaje obtenerPorId(Long id) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Mensaje.class, id);
		}
	}

	// OBTENER TODOS LOS MENSAJES
	public List<Mensaje> obtenerTodos() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("FROM Mensaje ORDER BY fecha ASC", Mensaje.class).list();
		}
	}

	// SELECCIONAR MENSAJES POR EMISOR
	public List<Mensaje> obtenerPorEmisor(String idEmisor) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Mensaje> q = session.createQuery("FROM Mensaje WHERE idEmisor = :emisor ORDER BY fecha ASC",
					Mensaje.class);
			q.setParameter("emisor", idEmisor);
			return q.list();
		}
	}

	// SELECCIONAR MENSAJES POR RECEPTOR
	public List<Mensaje> obtenerPorReceptor(String idReceptor) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Mensaje> q = session.createQuery("FROM Mensaje WHERE idReceptor = :receptor ORDER BY fecha ASC",
					Mensaje.class);
			q.setParameter("receptor", idReceptor);
			return q.list();
		}
	}

	// HISTORIAL ENTRE DOS USUARIOS (ASCENDENTE FECHA)
	public List<Mensaje> obtenerConversacionDM(String idUsuarioA, String idUsuarioB) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Mensaje> q = session.createQuery("FROM Mensaje " + "WHERE (idEmisor = :a AND idReceptor = :b) "
					+ "   OR (idEmisor = :b AND idReceptor = :a) " + "ORDER BY fecha ASC", Mensaje.class);
			q.setParameter("a", idUsuarioA);
			q.setParameter("b", idUsuarioB);
			return q.list();
		}
	}

	// ULTIMOS X MENSAJES ENTRE DOS USUARIOS (DESCENDENTE FECHA)
	public List<Mensaje> obtenerUltimosDM(String idUsuarioA, String idUsuarioB, int limite) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Mensaje> q = session.createQuery("FROM Mensaje " + "WHERE (idEmisor = :a AND idReceptor = :b) "
					+ "   OR (idEmisor = :b AND idReceptor = :a) " + "ORDER BY fecha DESC", Mensaje.class);
			q.setParameter("a", idUsuarioA);
			q.setParameter("b", idUsuarioB);
			q.setMaxResults(limite);
			List<Mensaje> recientesDesc = q.list();
			return recientesDesc;
		}
	}

	// SELECT PAGINADO GENERAL
	public List<Mensaje> obtenerPaginado(int pagina, int tamPagina) {
		if (pagina < 0)
			pagina = 0;
		if (tamPagina <= 0)
			tamPagina = 20;

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Mensaje> q = session.createQuery("FROM Mensaje ORDER BY fecha DESC", Mensaje.class);
			q.setFirstResult(pagina * tamPagina);
			q.setMaxResults(tamPagina);
			return q.list();
		}
	}

	// DELETE MENSAJE POR ID
	public void eliminarPorId(Long id) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			Mensaje m = session.get(Mensaje.class, id);
			if (m != null)
				session.delete(m);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// DELETE UNA CONVERSACION ENTRE DOS USUARIOS
	public int eliminarConversacionDM(String idUsuarioA, String idUsuarioB) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			Query<?> q = session.createQuery("DELETE FROM Mensaje " + "WHERE (idEmisor = :a AND idReceptor = :b) "
					+ "   OR (idEmisor = :b AND idReceptor = :a)");
			q.setParameter("a", idUsuarioA);
			q.setParameter("b", idUsuarioB);
			int eliminados = q.executeUpdate();
			tx.commit();
			return eliminados;
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			return 0;
		}
	}

	// DELETE MENSAJE USUARIO (YA SEA EMISOR O RECEPTOR)
	public int eliminarTodosDeUsuario(String idUsuario) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			Query<?> q = session.createQuery("DELETE FROM Mensaje WHERE idEmisor = :id OR idReceptor = :id");
			q.setParameter("id", idUsuario);
			int eliminados = q.executeUpdate();
			tx.commit();
			return eliminados;
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			return 0;
		}
	}

	public List<Mensaje> obtenerHistorialSala(String nombreSala) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			var q = session.createQuery("FROM Mensaje WHERE idReceptor = :sala ORDER BY fecha ASC", Mensaje.class);
			q.setParameter("sala", nombreSala);
			return q.list();
		}
	}

}
