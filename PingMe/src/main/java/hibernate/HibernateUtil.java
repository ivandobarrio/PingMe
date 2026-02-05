package hibernate;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import entidades.Usuario;

public class HibernateUtil {

	private static SessionFactory sessionFactory;

	private HibernateUtil() {
	}

	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			synchronized (HibernateUtil.class) {
				if (sessionFactory == null) {
					try {
						Configuration configuration = new Configuration();
						configuration.configure("hibernate.cfg.xml");

						configuration.addAnnotatedClass(Usuario.class);

						ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
								.applySettings(configuration.getProperties()).build();

						sessionFactory = configuration.buildSessionFactory(serviceRegistry);
					} catch (Exception ex) {
						System.err.println("Error inicializando SessionFactory: " + ex.getMessage());
						ex.printStackTrace();
						throw new ExceptionInInitializerError(ex);
					}
				}
			}
		}
		return sessionFactory;
	}

	public static void shutdown() {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}
}
