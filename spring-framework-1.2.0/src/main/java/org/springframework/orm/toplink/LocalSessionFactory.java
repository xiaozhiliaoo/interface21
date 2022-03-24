/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.orm.toplink;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.sql.DataSource;

import oracle.toplink.exceptions.TopLinkException;
import oracle.toplink.internal.databaseaccess.DatabasePlatform;
import oracle.toplink.jndi.JNDIConnector;
import oracle.toplink.sessions.DatabaseLogin;
import oracle.toplink.sessions.DatabaseSession;
import oracle.toplink.sessions.SessionLog;
import oracle.toplink.threetier.ServerSession;
import oracle.toplink.tools.sessionconfiguration.XMLLoader;
import oracle.toplink.tools.sessionmanagement.SessionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient JavaBean-style factory for a TopLink SessionFactory instance.
 * Loads a TopLink <code>sessions.xml</code> file from the class path, exposing a
 * specific TopLink Session defined there (usually a ServerSession).
 *
 * <p>TopLink Session configuration is done using a <code>sessions.xml</code> file.
 * The most convenient way to create the <code>sessions.xml</code> file is to use
 * the Oracle TopLink SessionsEditor workbench. The <code>sessions.xml</code> file
 * contains all runtime configuration and points to a second XML or Class resource
 * from which to load the actual TopLink project metadata (which defines mappings).
 *
 * <p>LocalSessionFactory loads the <code>sessions.xml</code> file during
 * initialization in order to bootstrap the specified TopLink (Server)Session.
 * The name of the actual config resource and the name of the Session to be loaded,
 * if different from <code>sessions.xml</code> and "Session", respectively, can be
 * configured through bean properties.
 *
 * <p>All resources (<code>sessions.xml</code> and Mapping Workbench metadata) are
 * loaded using <code>ClassLoader.getResourceAsStream</code> calls by TopLink, so
 * users may need to configure a ClassLoader with appropriate visibility. This is
 * particularly important in J2EE environments where the TopLink metadata might be
 * deployed to a different location than the Spring configuration. The ClassLoader
 * used to search for the TopLink metadata and to load the persistent classes
 * defined there will default to the the context ClassLoader for the current Thread.
 *
 * <p>TopLink's debug logging can be redirected to Commons Logging by passing a
 * CommonsLoggingSessionLog to the "sessionLog" bean property. Otherwise, TopLink
 * uses it's own DefaultSessionLog, whose levels are configured in the
 * <code>sessions.xml</code> file.
 *
 * <p>This class has been tested against both TopLink 9.0.4 and TopLink 10.1.3.
 * It will automatically adapt to the TopLink version encountered: for example,
 * using an XMLSessionConfigLoader on 10.1.3, but an XMLLoader on 9.0.4.
 *
 * <p><b>NOTE:</b> When defining a TopLink SessionFactory in a Spring application
 * context, you will usually define a bean of type <b>LocalSessionFactoryBean</b>.
 * LocalSessionFactoryBean is a subclass of this factory, which will automatically
 * expose the created TopLink SessionFactory instance as bean reference.
 *
 * @author Juergen Hoeller
 * @author <a href="mailto:james.x.clark@oracle.com">James Clark</a>
 * @since 1.2
 * @see LocalSessionFactoryBean
 * @see TopLinkTemplate#setSessionFactory
 * @see TopLinkTransactionManager#setSessionFactory
 * @see SingleSessionFactory
 * @see ServerSessionFactory
 * @see oracle.toplink.threetier.ServerSession
 * @see oracle.toplink.tools.sessionconfiguration.XMLLoader
 * @see oracle.toplink.tools.sessionconfiguration.XMLSessionConfigLoader
 */
public class LocalSessionFactory {

	/**
	 * The default location of the <code>sessions.xml</code> TopLink configuration file:
	 * "sessions.xml" in the class path.
	 */
	public static final String DEFAULT_SESSIONS_XML = "sessions.xml";

	/**
	 * The default session name to look for in the sessions.xml: "Session".
	 */
	public static final String DEFAULT_SESSION_NAME = "Session";


	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * The classpath location of the sessions TopLink configuration file.
	 */
	private String configLocation = DEFAULT_SESSIONS_XML;

	/**
	 * The session name to look for in the sessions.xml configuration file.
	 */
	private String sessionName = DEFAULT_SESSION_NAME;

	/**
	 * The ClassLoader to use to load the sessions.xml and project XML files.
	 */
	private ClassLoader sessionClassLoader;

	private DatabaseLogin databaseLogin;

	private DataSource dataSource;

	private DatabasePlatform databasePlatform;

	private SessionLog sessionLog;


	/**
	 * Set the TopLink <code>sessions.xml</code> configuration file that defines
	 * TopLink Sessions, as class path resource location.
	 * <p>The <code>sessions.xml</code> file will usually be placed in the META-INF
	 * directory or root path of a JAR file, or the <code>WEB-INF/classes</code>
	 * directory of a WAR file (specifying "META-INF/toplink-sessions.xml" or
	 * simply "toplink-sessions.xml" as config location, respectively).
	 * <p>The default config location is "sessions.xml" in the root of the class path.
	 * @param configLocation the class path location of the <code>sessions.xml</code> file
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Set the name of the TopLink Session, as defined in TopLink's
	 * <code>sessions.xml</code> configuration file.
	 * The default session name is "Session".
	 */
	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	/**
	 * Set the ClassLoader that should be used to lookup the config resources.
	 * If nothing is set here, then we will try to use the Thread context ClassLoader
	 * and the ClassLoader that loaded this factory class, in that order.
	 * <p>This ClassLoader will be used to load the TopLink configuration files
	 * and the project metadata. Furthermore, the TopLink ConversionManager will
	 * use this ClassLoader to load all TopLink entity classes. If the latter is not
	 * appropriate, users can configure a pre-login SessionEvent to alter the
	 * ConversionManager ClassLoader that TopLink will use at runtime.
	 */
	public void setSessionClassLoader(ClassLoader sessionClassLoader) {
		this.sessionClassLoader = sessionClassLoader;
	}

	/**
	 * Specify the DatabaseLogin instance that carries the TopLink database
	 * configuration to use. This is an alternative to specifying that information
	 * in a &lt;login&gt; tag in the <code>sessions.xml</code> configuration file,
	 * allowing for configuring a DatabaseLogin instance as standard Spring bean
	 * definition (being able to leverage Spring's placeholder mechanism, etc).
	 * <p>The DatabaseLogin instance can either carry traditional JDBC config properties
	 * or hold a nested TopLink Connector instance, pointing to the connection pool to use.
	 * DatabaseLogin also holds the TopLink DatabasePlatform instance that defines the
	 * database product that TopLink is talking to (for example, HSQLPlatform).
	 * @see oracle.toplink.sessions.DatabaseLogin#setDriverClassName(String)
	 * @see oracle.toplink.sessions.DatabaseLogin#setDatabaseURL(String)
	 * @see oracle.toplink.sessions.DatabaseLogin#setConnector(oracle.toplink.sessions.Connector)
	 * @see oracle.toplink.sessions.DatabaseLogin#setUsesExternalConnectionPooling(boolean)
	 * @see oracle.toplink.sessions.DatabaseLogin#usePlatform(oracle.toplink.internal.databaseaccess.DatabasePlatform)
	 */
	public void setDatabaseLogin(DatabaseLogin databaseLogin) {
		this.databaseLogin = databaseLogin;
	}

	/**
	 * Specify a standard JDBC DataSource that TopLink should use as connection pool.
	 * This allows for using a shared DataSource definition instead of TopLink's
	 * own connection pool.
	 * <p>A passed-in DataSource will be wrapped in an appropriate TopLink Connector
	 * and registered with the TopLink DatabaseLogin instance (either the default
	 * instance or one passed in through the "databaseLogin" property). The
	 * "usesExternalConnectionPooling" flag will automatically be set to "true".
	 * @see oracle.toplink.sessions.DatabaseLogin#setConnector(oracle.toplink.sessions.Connector)
	 * @see oracle.toplink.sessions.DatabaseLogin#setUsesExternalConnectionPooling(boolean)
	 * @see #setDatabaseLogin(oracle.toplink.sessions.DatabaseLogin)
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Specify the TopLink DatabasePlatform instance that the Session should use:
	 * for example, HSQLPlatform. This is an alternative to specifying the platform
	 * in a &lt;login&gt; tag in the <code>sessions.xml</code> configuration file.
	 * <p>A passed-in DatabasePlatform will be registered with the TopLink
	 * DatabaseLogin instance (either the default instance or one passed in
	 * through the "databaseLogin" property).
	 * @see oracle.toplink.internal.databaseaccess.HSQLPlatform
	 * @see oracle.toplink.platform.database.HSQLPlatform
	 */
	public void setDatabasePlatform(DatabasePlatform databasePlatform) {
		this.databasePlatform = databasePlatform;
	}

	/**
	 * Specify a TopLink SessionLog instance to use for detailed logging of the
	 * Session's activities: for example, DefaultSessionLog (which logs to the
	 * console), JavaLog (which logs through JDK 1.4'S <code>java.util.logging</code>,
	 * available as of TopLink 10.1.3), or CommonsLoggingSessionLog /
	 * CommonsLoggingSessionLog904 (which logs through Commons Logging,
	 * on TopLink 10.1.3 and 9.0.4, respectively).
	 * <p>Note that detailed Session logging is usually only useful for debug
	 * logging, with adjustable detail level. As of TopLink 10.1.3, TopLink also
	 * uses different log categories, which allows for fine-grained filtering of
	 * log messages. For standard execution, no SessionLog needs to be specified.
	 * @see oracle.toplink.sessions.DefaultSessionLog
	 * @see oracle.toplink.logging.DefaultSessionLog
	 * @see oracle.toplink.logging.JavaLog
	 * @see org.springframework.orm.toplink.support.CommonsLoggingSessionLog
	 * @see org.springframework.orm.toplink.support.CommonsLoggingSessionLog904
	 */
	public void setSessionLog(SessionLog sessionLog) {
		this.sessionLog = sessionLog;
	}


	/**
	 * Create a TopLink SessionFactory according to the configuration settings.
	 * @return the new TopLink SessionFactory
	 * @throws TopLinkException in case of errors
	 */
	public SessionFactory createSessionFactory() throws TopLinkException {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing TopLink SessionFactory from [" + this.configLocation + "]");
		}

		// Determine class loader to use.
		ClassLoader classLoader = this.sessionClassLoader;
		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader == null) {
				classLoader = getClass().getClassLoader();
			}
		}

		// Initialize the TopLink Session, using the configuration file
		// and the session name.
		DatabaseSession session = loadDatabaseSession(this.configLocation, this.sessionName, classLoader);

		// Override default DatabaseLogin instance with specified one, if any.
		if (this.databaseLogin != null) {
			session.setLogin(this.databaseLogin);
		}

		// Override default connection pool with specified DataSource, if any.
		if (this.dataSource != null) {
			session.getLogin().setConnector(new JNDIConnector(this.dataSource));
			session.getLogin().setUsesExternalConnectionPooling(true);
		}

		// Override default DatabasePlatform with specified one, if any.
		if (this.databasePlatform != null) {
			session.getLogin().usePlatform(this.databasePlatform);
		}

		// Override default SessionLog with specified one, if any.
		if (this.sessionLog != null) {
			session.setSessionLog(this.sessionLog);
			session.logMessages();
		}

		// Log in and create corresponding SessionFactory.
		session.login();
		return newSessionFactory(session);
	}

	/**
	 * Load the specified DatabaseSession from the TopLink <code>sessions.xml</code>
	 * configuration file.
	 * @param configLocation the class path location of the <code>sessions.xml</code> file
	 * @param sessionName the name of the TopLink Session in the configuration file
	 * @param sessionClassLoader the class loader to use
	 * @return the DatabaseSession instance
	 * @throws TopLinkException in case of errors
	 */
	protected DatabaseSession loadDatabaseSession(
			String configLocation, String sessionName, ClassLoader sessionClassLoader)
			throws TopLinkException {

		SessionManager manager = SessionManager.getManager();

		// Try to find TopLink 10.1.3 XMLSessionConfigLoader.
		Class loaderClass = null;
		Method getSessionMethod = null;
		try {
			loaderClass = Class.forName("oracle.toplink.tools.sessionconfiguration.XMLSessionConfigLoader");
			getSessionMethod = SessionManager.class.getMethod("getSession",
					new Class[] {loaderClass, String.class, ClassLoader.class, boolean.class, boolean.class});
			if (logger.isDebugEnabled()) {
				logger.debug("Using TopLink 10.1.3 XMLSessionConfigLoader");
			}
		}
		catch (Exception ex) {
			// TopLink 10.1.3 XMLSessionConfigLoader not found ->
			// fall back to TopLink 9.0.4 XMLLoader.
			if (logger.isDebugEnabled()) {
				logger.debug("Using TopLink 9.0.4 XMLLoader");
			}
			XMLLoader loader = new XMLLoader(configLocation);
			return (DatabaseSession) manager.getSession(loader, sessionName, sessionClassLoader, false, false);
		}

		// TopLink 10.1.3 XMLSessionConfigLoader found -> create loader instance
		// through reflection and fetch specified Session from SessionManager.
		try {
			Constructor ctor = loaderClass.getConstructor(new Class[] {String.class});
			Object loader = ctor.newInstance(new Object[] {configLocation});
			return (DatabaseSession) getSessionMethod.invoke(manager,
					new Object[] {loader, sessionName, sessionClassLoader, Boolean.FALSE, Boolean.FALSE});
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) ex.getTargetException();
			}
			throw new IllegalStateException(
					"TopLink SessionManager.getSession method threw exception: " + ex.getTargetException().getMessage());
		}
		catch (Exception ex) {
			throw new IllegalStateException(
					"TopLink SessionManager.getSession access failed: " + ex.getMessage());
		}
	}

	/**
	 * Create a new SessionFactory for the given TopLink DatabaseSession.
	 * <p>The default implementation creates a ServerSessionFactory for a
	 * ServerSession and a SingleSessionFactory for a plain DatabaseSession.
	 * @param session the TopLink DatabaseSession to create a SessionFactory for
	 * @return the SessionFactory
	 * @throws TopLinkException in case of errors
	 * @see ServerSessionFactory
	 * @see SingleSessionFactory
	 * @see oracle.toplink.threetier.ServerSession
	 * @see oracle.toplink.sessions.DatabaseSession
	 */
	protected SessionFactory newSessionFactory(DatabaseSession session) {
		if (session instanceof ServerSession) {
			return new ServerSessionFactory((ServerSession) session);
		}
		else {
			return new SingleSessionFactory(session);
		}
	}

}
