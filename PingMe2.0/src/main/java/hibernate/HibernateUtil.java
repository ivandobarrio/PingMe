package hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    
    private static SessionFactory sessionFactory;
    
    static {
        try {
            // Carga la configuración de hibernate.cfg.xml y construye el SessionFactory
            sessionFactory = new Configuration().configure().buildSessionFactory();
            System.out.println("SessionFactory creada correctamente");
        } catch (Throwable ex) {
            System.err.println("Error al crear SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    // Método para obtener el SessionFactory
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    // Método para cerrar el SessionFactory al finalizar la aplicación
    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
