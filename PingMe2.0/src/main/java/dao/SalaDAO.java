package dao;

import entidades.Sala;
import hibernate.HibernateUtil;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class SalaDAO {
    
	// Guarda una sala en la base de datos
    public void guardar(Sala sala) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.save(sala);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
    
    // Actualiza una sala existente en la base de datos
    public Sala buscarPorCodigo(String codigo) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Sala> query = session.createQuery("FROM Sala WHERE codigoUnico = :codigo", Sala.class);
            query.setParameter("codigo", codigo);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    // Elimina una sala de la base de datos
    public List<Sala> listarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Sala", Sala.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
