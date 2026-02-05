package dao;

import entidades.Mensaje;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class MensajeDAO {
	
	// Guarda un mensaje en la base de datos
    public void guardar(Mensaje mensaje) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(mensaje);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    // Obtiene los mensajes de una sala específica, ordenados por fecha
    public List<Mensaje> obtenerMensajesDeSala(String codigoSala) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Mensaje> query = session.createQuery(
                "FROM Mensaje WHERE sala = :sala ORDER BY fecha ASC", Mensaje.class);
            query.setParameter("sala", codigoSala);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Obtiene los mensajes privados entre dos usuarios, ordenados por fecha
    public List<Mensaje> obtenerMensajesPrivados(String usuario1, String usuario2) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Mensaje> query = session.createQuery(
                "FROM Mensaje WHERE (emisor = :u1 AND receptor = :u2) OR (emisor = :u2 AND receptor = :u1) ORDER BY fecha ASC", 
                Mensaje.class);
            query.setParameter("u1", usuario1);
            query.setParameter("u2", usuario2);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
