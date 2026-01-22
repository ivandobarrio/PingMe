package DAO;

import entidades.Usuario;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class UsuarioDAO {

	//INSERCCION USUARIO
    public void insertarUsuario(Usuario usuario) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.save(usuario);  
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

    //SELECCIONAR USAURIO POR ID
    public Usuario obtenerPorId(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Usuario.class, id);
        }
    }
    
    //OBTENER USUARIO POR EMAIL
    public Usuario obtenerPorEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                "FROM Usuario WHERE email = :email", Usuario.class
            );
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    //SELECT, LISTA TODOS LOS USUARIOS
    public List<Usuario> obtenerTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Usuario", Usuario.class).list();
        }
    }

    //DELETE, ELIMINA USUARIO POR ID
    public void eliminarPorId(Long id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Usuario user = session.get(Usuario.class, id);
            if (user != null) {
                session.delete(user);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }

}
