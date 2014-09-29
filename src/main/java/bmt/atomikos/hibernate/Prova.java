package bmt.atomikos.hibernate;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import com.atomikos.jdbc.AtomikosDataSourceBean;

public class Prova {

	private static UserTransaction userTransaction;
	private static AtomikosDataSourceBean dataSourcecrm;
	private static AtomikosDataSourceBean dataSourcemio;

	public static void main(String[] args) throws Exception {
		initDataSources();
		initTransactionManager();
		
		
		AnnotationConfiguration configcrm = new AnnotationConfiguration();
		configcrm.configure("/crm.hibernate.cfg.xml");
		
		AnnotationConfiguration configmio = new AnnotationConfiguration();
		configmio.configure("/mio.hibernate.cfg.xml");
		
		
		
//		ServiceRegistry serviceRegistryCrm = new StandardServiceRegistryBuilder().
//				applySettings(configcrm.getProperties()).build();
//		ServiceRegistry serviceRegistryMio = new StandardServiceRegistryBuilder().
//				applySettings(configmio.getProperties()).build();
		
		SessionFactory sfcrm = configcrm.buildSessionFactory();
		SessionFactory sfmio = configmio.buildSessionFactory();

		
		userTransaction.setTransactionTimeout(60);
		userTransaction.begin();
		

		try {
			
			doWork(sfcrm, sfmio);

			userTransaction.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			userTransaction.rollback();
		}

		sfcrm.close();
		sfmio.close();
		dataSourcecrm.close();
		dataSourcemio.close();
		
		System.out.println("Both databases updated successfully");
	}

	/**
	 * Perform some work. In this scenario, we get a session from the specified
	 * SessionFactory then we create a new user with the specified username then
	 * list all users.
	 */
	private static void doWork(SessionFactory sfcrm, SessionFactory sfmio) {
		Session sessioncrm = sfcrm.getCurrentSession();
		Session sessionmio = sfmio.getCurrentSession();
		persistAcidRest(sessionmio);
		persistCanale(sessioncrm);
		sessioncrm.close();
		sessionmio.close();
	}

	private static void persistAcidRest(Session session) {
		AcidRest acid = new AcidRest();
		acid.setRest(1);
		acid.setTemp(1);
		session.save(acid);
	}

	private static void persistCanale(Session session) {
		Canale canale = new Canale();
		canale.setId(4);
		session.save(canale);
	}

	private static void initTransactionManager() {
		userTransaction = new com.atomikos.icatch.jta.UserTransactionImp();
	}

	private static void initDataSources() throws NamingException {
		System.setProperty("java.naming.factory.initial","org.apache.naming.java.javaURLContextFactory");
//		System.setProperty("com.atomikos.icatch.service","com.atomikos.icatch.standalone.UserTransactionServiceFactory");
//		System.setProperty("com.atomikos.icatch.automatic_resource_registration","true");
//		System.setProperty("com.atomikos.icatch.console_log_level","DUBUG");
		Context ctx = new InitialContext();
		dataSourcecrm = getDataSource_crm();
		ctx.rebind(dataSourcecrm.getUniqueResourceName(), dataSourcecrm);
		dataSourcemio = getDataSource_mio();
		ctx.rebind(dataSourcemio.getUniqueResourceName(), dataSourcemio);
		ctx.close();
	}

	/**
	 * Create the first datasource.
	 */
	private static AtomikosDataSourceBean getDataSource_mio() {
		AtomikosDataSourceBean ds1 = new AtomikosDataSourceBean();
		ds1.setUniqueResourceName("uno");
		ds1.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		Properties p1 = new Properties();
		p1.setProperty("user", "root");
		p1.setProperty("password", "root");
		p1.setProperty("URL", "jdbc:mysql://localhost:3306/mio");
		ds1.setXaProperties(p1);
		ds1.setPoolSize(5);
		return ds1;
	}

	/**
	 * Create the second datasource.
	 */
	private static AtomikosDataSourceBean getDataSource_crm() {
		AtomikosDataSourceBean ds1 = new AtomikosDataSourceBean();
		ds1.setUniqueResourceName("due");
		ds1.setXaDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
		Properties p1 = new Properties();
		p1.setProperty("user", "root");
		p1.setProperty("password", "root");
		p1.setProperty("URL", "jdbc:mysql://localhost:3306/crm");
		ds1.setXaProperties(p1);
		ds1.setPoolSize(5);
		return ds1;
	}

}
