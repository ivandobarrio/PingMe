package dao;

import entidades.Usuario;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UsuarioDAO {
    
	// Guarda un nuevo usuario en la base de datos
    public void guardar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("DAO: Iniciando transacción para " + usuario.getUsername());
            transaction = session.beginTransaction();
            session.save(usuario);
            transaction.commit();
            System.out.println("DAO: Transacción completada para " + usuario.getUsername());
        } catch (Exception e) {
            System.err.println("DAO: Error en guardar: " + e.getMessage());
            if (transaction != null) {
                transaction.rollback();
                System.err.println("DAO: Transacción revertida");
            }
            throw e;
        }
    }

    // Actualiza un usuario existente en la base de datos
    public void actualizar(Usuario usuario) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(usuario);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }
    
    // Elimina un usuario de la base de datos
    public Usuario buscarPorUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE username = :username", Usuario.class);
            query.setParameter("username", username);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Busca un usuario por su email en la base de datos
    public Usuario buscarPorEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery("FROM Usuario WHERE email = :email", Usuario.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Lista todos los usuarios registrados en la base de datos
    public List<Usuario> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Usuario", Usuario.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Valida las credenciales de un usuario comparando el username y password con los almacenados en la base de datos
    public boolean validarCredenciales(String username, String password) {
        Usuario usuario = buscarPorUsername(username);
        return usuario != null && usuario.getPassword().equals(password);
    }
    
    // Valida la respuesta de seguridad de un usuario comparando la respuesta proporcionada con la almacenada en la base de datos
    public boolean validarRespuestaSeguridad(Usuario u, String respuesta) {
        if (u == null || u.getRespuestaSeguridad() == null)
            return false;
        if (respuesta == null)
            return false;
        return u.getRespuestaSeguridad().trim().equalsIgnoreCase(respuesta.trim());
    }
}
