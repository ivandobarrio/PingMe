package DAO;

import entidades.Sala;
import entidades.Usuario;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class SalaDAO {

	// INSERTAR UNA SALA
	public void insertarSala(Sala sala) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			session.save(sala);
			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// SELECCIONAR UNA SALA POR ID
	public Sala obtenerPorId(Long id) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.get(Sala.class, id);
		}
	}

	// LISTAR TODAS LAS SALAS (FECHA DESCENDENTE)
	public List<Sala> obtenerTodas() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			return session.createQuery("FROM Sala ORDER BY fechaCreacion DESC", Sala.class).list();
		}
	}

	// SELECCIONAR SALA POR NOMBRE
	public Sala obtenerPorNombre(String nombre) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Sala> q = session.createQuery("FROM Sala WHERE nombre = :nombre", Sala.class);
			q.setParameter("nombre", nombre);
			return q.uniqueResult();
		}
	}

	// SELECCIONAR SALA DONDE PARTICIPA UN USUARIO
	public List<Sala> obtenerSalasDeUsuario(Long userId) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Query<Sala> q = session.createQuery("SELECT s FROM Sala s JOIN s.integrantes u WHERE u.id = :uid",
					Sala.class);
			q.setParameter("uid", userId);
			return q.list();
		}
	}

	// AÑADIR UN USUARIO A UNA SALA
	public void agregarUsuarioASala(Long salaId, Long usuarioId) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			Sala sala = session.get(Sala.class, salaId);
			Usuario usu = session.get(Usuario.class, usuarioId);

			if (sala != null && usu != null) {
				sala.getIntegrantes().add(usu);
				session.update(sala);
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// ELIMINAR UN USUARIO DE UNA SALA
	public void eliminarUsuarioDeSala(Long salaId, Usuario usuario) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			Sala sala = session.get(Sala.class, salaId);
			if (sala != null) {
				sala.getIntegrantes().remove(usuario);
				session.update(sala);
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// DELETE UNA SALA
	public void eliminarSala(Long id) {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();

			Sala sala = session.get(Sala.class, id);
			if (sala != null) {
				session.delete(sala);
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
		}
	}

	// DELETE SALAS SIN USUARIOS
	public int eliminarSalasVacias() {
		Transaction tx = null;
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			tx = session.beginTransaction();
			Query<?> q = session.createQuery("DELETE FROM Sala s WHERE size(s.integrantes) = 0");
			int borradas = q.executeUpdate();
			tx.commit();
			return borradas;
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			e.printStackTrace();
			return 0;
		}
	}
}
