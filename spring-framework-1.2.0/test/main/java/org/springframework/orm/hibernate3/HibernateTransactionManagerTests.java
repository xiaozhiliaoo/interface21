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

package org.springframework.orm.hibernate3;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import junit.framework.TestCase;
import org.easymock.MockControl;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.dialect.HSQLDialect;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.JdkVersion;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * @author Juergen Hoeller
 * @since 05.03.2005
 */
public class HibernateTransactionManagerTests extends TestCase {

	public void testTransactionCommit() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		final List list = new ArrayList();
		list.add("test");
		con.getTransactionIsolation();
		conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conControl.setVoidCallable(1);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.setTimeout(10);
		queryControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);

		dsControl.replay();
		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		queryControl.replay();

		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				return sf;
			}
		};
		lsfb.afterPropertiesSet();
		final SessionFactory sfProxy = (SessionFactory) lsfb.getObject();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
		tm.setSessionFactory(sfProxy);
		tm.setDataSource(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.setTimeout(10);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sfProxy));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sfProxy));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				assertEquals(session, sfProxy.getCurrentSession());
				HibernateTemplate ht = new HibernateTemplate(sfProxy);
				return ht.find("some query string");
			}
		});
		assertTrue("Correct result list", result == list);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sfProxy));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		dsControl.verify();
		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		queryControl.verify();
	}

	public void testTransactionRollback() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(org.hibernate.Session session) {
							throw new RuntimeException("application exception");
						}
					});
				}
			});
			fail("Should have thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackOnly() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				HibernateTemplate ht = new HibernateTemplate(sf);
				ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
				ht.execute(new HibernateCallback() {
					public Object doInHibernate(org.hibernate.Session session) {
						return null;
					}
				});
				status.setRollbackOnly();
				return null;
			}
		});

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithCommit() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				return sf;
			}
		};
		lsfb.afterPropertiesSet();
		final SessionFactory sfProxy = (SessionFactory) lsfb.getObject();

		PlatformTransactionManager tm = new HibernateTransactionManager(sfProxy);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						assertEquals(session, sfProxy.getCurrentSession());
						HibernateTemplate ht = new HibernateTemplate(sfProxy);
						ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
						return ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(org.hibernate.Session session) {
								return l;
							}
						});
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollback() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					return tt.execute(new TransactionCallback() {
						public Object doInTransaction(TransactionStatus status) {
							HibernateTemplate ht = new HibernateTemplate(sf);
							ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
							return ht.executeFind(new HibernateCallback() {
								public Object doInHibernate(org.hibernate.Session session) {
									throw new RuntimeException("application exception");
								}
							});
						}
					});
				}
			});
			fail("Should not thrown RuntimeException");
		}
		catch (RuntimeException ex) {
			// expected
		}
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithRollbackOnly() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				return tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						HibernateTemplate ht = new HibernateTemplate(sf);
						ht.execute(new HibernateCallback() {
							public Object doInHibernate(org.hibernate.Session session) {
								return l;
							}
						});
						status.setRollbackOnly();
						return null;
					}
				});
			}
		});

		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithWithRequiresNew() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl session1Control = MockControl.createControl(Session.class);
		Session session1 = (Session) session1Control.getMock();
		MockControl session2Control = MockControl.createControl(Session.class);
		Session session2 = (Session) session2Control.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session1, 1);
		sf.openSession();
		sfControl.setReturnValue(session2, 1);
		session1.beginTransaction();
		session1Control.setReturnValue(tx, 1);
		session2.beginTransaction();
		session2Control.setReturnValue(tx, 1);
		session2.flush();
		session2Control.setVoidCallable(1);
		session1.close();
		session1Control.setReturnValue(null, 1);
		session2.close();
		session2Control.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(2);
		session1.connection();
		session1Control.setReturnValue(con, 2);
		session2.connection();
		session2Control.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 2);
		sfControl.replay();
		session1Control.replay();
		session2Control.replay();
		conControl.replay();
		txControl.replay();

		PlatformTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				final SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertTrue("Has thread session", holder != null);
				tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						HibernateTemplate ht = new HibernateTemplate(sf);
						ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
						return ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(org.hibernate.Session session) {
								assertTrue("Not enclosing session", session != holder.getSession());
								return null;
							}
						});
					}
				});
				assertTrue("Same thread session as before",
				           holder.getSession() == SessionFactoryUtils.getSession(sf, false));
				return null;
			}
		});
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		sfControl.verify();
		session1Control.verify();
		session2Control.verify();
		conControl.verify();
		txControl.verify();
	}

	public void testParticipatingTransactionWithWithNotSupported() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 2);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.getFlushMode();
		sessionControl.setReturnValue(FlushMode.AUTO, 1);
		session.flush();
		sessionControl.setVoidCallable(2);
		session.close();
		sessionControl.setReturnValue(null, 2);
		tx.commit();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		conControl.replay();
		txControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertTrue("Has thread session", holder != null);
				tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
				tt.execute(new TransactionCallback() {
					public Object doInTransaction(TransactionStatus status) {
						assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
						HibernateTemplate ht = new HibernateTemplate(sf);
						ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
						return ht.executeFind(new HibernateCallback() {
							public Object doInHibernate(org.hibernate.Session session) {
								return null;
							}
						});
					}
				});
				assertTrue("Same thread session as before",
				           holder.getSession() == SessionFactoryUtils.getSession(sf, false));
				return null;
			}
		});
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));

		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}
	public void testTransactionWithPropagationSupports() throws Exception {
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.getSessionFactory();
		sessionControl.setReturnValue(sf, 1);
		session.getFlushMode();
		sessionControl.setReturnValue(FlushMode.NEVER, 1);
		session.flush();
		sessionControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		sfControl.replay();
		sessionControl.replay();

		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean() {
			protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
				return sf;
			}
		};
		lsfb.afterPropertiesSet();
		final SessionFactory sfProxy = (SessionFactory) lsfb.getObject();

		PlatformTransactionManager tm = new HibernateTransactionManager(sfProxy);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sfProxy));

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sfProxy));
				assertTrue("Is not new transaction", !status.isNewTransaction());
				HibernateTemplate ht = new HibernateTemplate(sfProxy);
				ht.setFlushMode(HibernateTemplate.FLUSH_EAGER);
				ht.execute(new HibernateCallback() {
					public Object doInHibernate(org.hibernate.Session session) {
						return null;
					}
				});
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sfProxy));
				assertEquals(session, sfProxy.getCurrentSession());
				return null;
			}
		});

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sfProxy));
		sfControl.verify();
		sessionControl.verify();
	}

	public void testTransactionCommitWithEntityInterceptor() throws Exception {
		MockControl interceptorControl = MockControl.createControl(org.hibernate.Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		tm.setEntityInterceptor(entityInterceptor);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				HibernateTemplate ht = new HibernateTemplate(sf);
				return ht.executeFind(new HibernateCallback() {
					public Object doInHibernate(org.hibernate.Session session) throws HibernateException {
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		interceptorControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithEntityInterceptorBeanName() throws Exception {
		MockControl interceptorControl = MockControl.createControl(org.hibernate.Interceptor.class);
		Interceptor entityInterceptor = (Interceptor) interceptorControl.getMock();
		interceptorControl.replay();
		MockControl interceptor2Control = MockControl.createControl(org.hibernate.Interceptor.class);
		Interceptor entityInterceptor2 = (Interceptor) interceptor2Control.getMock();
		interceptor2Control.replay();

		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession(entityInterceptor);
		sfControl.setReturnValue(session, 1);
		sf.openSession(entityInterceptor2);
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 2);
		session.close();
		sessionControl.setReturnValue(null, 2);
		tx.commit();
		txControl.setVoidCallable(2);
		session.connection();
		sessionControl.setReturnValue(con, 4);
		con.isReadOnly();
		conControl.setReturnValue(false, 2);
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		MockControl beanFactoryControl = MockControl.createControl(BeanFactory.class);
		BeanFactory beanFactory = (BeanFactory) beanFactoryControl.getMock();
		beanFactory.getBean("entityInterceptor", Interceptor.class);
		beanFactoryControl.setReturnValue(entityInterceptor, 1);
		beanFactory.getBean("entityInterceptor", Interceptor.class);
		beanFactoryControl.setReturnValue(entityInterceptor2, 1);
		beanFactoryControl.replay();
		
		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		tm.setEntityInterceptorBeanName("entityInterceptor");
		tm.setBeanFactory(beanFactory);

		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		for (int i = 0; i < 2; i++) {
			tt.execute(new TransactionCallbackWithoutResult() {
				public void doInTransactionWithoutResult(TransactionStatus status) {
					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					ht.execute(new HibernateCallback() {
						public Object doInHibernate(org.hibernate.Session session) throws HibernateException {
							return null;
						}
					});
				}
			});
		}

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		interceptorControl.verify();
		interceptor2Control.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
		beanFactoryControl.verify();
	}

	public void testTransactionCommitWithReadOnly() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		final List list = new ArrayList();
		list.add("test");
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.setReadOnly(true);
		conControl.setVoidCallable(1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(true, 1);
		con.setReadOnly(false);
		conControl.setVoidCallable(1);
		session.close();
		sessionControl.setReturnValue(null, 1);

		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		queryControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setReadOnly(true);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				HibernateTemplate ht = new HibernateTemplate(sf);
				return ht.find("some query string");
			}
		});
		assertTrue("Correct result list", result == list);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		queryControl.verify();
	}

	public void testTransactionCommitWithFlushingFailure() throws Exception {
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		tx.commit();
		txControl.setThrowable(new JDBCException("", new SQLException("argh", "27")), 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.rollback();
		txControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);

		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		conControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager(sf);
		TransactionTemplate tt = new TransactionTemplate(tm);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.executeFind(new HibernateCallback() {
						public Object doInHibernate(org.hibernate.Session session) throws HibernateException {
							return l;
						}
					});
				}
			});
			fail("Should have thrown DataIntegrityViolationException");
		}
		catch (DataIntegrityViolationException ex) {
			// expected
		}

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		conControl.verify();
	}

	public void testTransactionCommitWithPreBound() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();

		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.getFlushMode();
		sessionControl.setReturnValue(FlushMode.NEVER, 1);
		session.setFlushMode(FlushMode.AUTO);
		sessionControl.setVoidCallable(1);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		con.getTransactionIsolation();
		conControl.setReturnValue(Connection.TRANSACTION_READ_COMMITTED);
		con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
		conControl.setVoidCallable(1);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conControl.setVoidCallable(1);
		tx.commit();
		txControl.setVoidCallable(1);
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		dsControl.replay();
		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.setDataSource(ds);
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		final List l = new ArrayList();
		l.add("test");
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertTrue("Has thread transaction", sessionHolder.getTransaction() != null);
				HibernateTemplate ht = new HibernateTemplate(sf);
				ht.setExposeNativeSession(true);
				return ht.executeFind(new HibernateCallback() {
					public Object doInHibernate(org.hibernate.Session sess) throws HibernateException {
						assertEquals(session, sess);
						return l;
					}
				});
			}
		});
		assertTrue("Correct result list", result == l);

		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
		assertTrue("Hasn't thread transaction", sessionHolder.getTransaction() == null);
		TransactionSynchronizationManager.unbindResource(sf);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		dsControl.verify();
		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
	}

	public void testTransactionRollbackWithPreBound() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		final Session session = (Session) sessionControl.getMock();
		MockControl tx1Control = MockControl.createControl(Transaction.class);
		final Transaction tx1 = (Transaction) tx1Control.getMock();
		MockControl tx2Control = MockControl.createControl(Transaction.class);
		final Transaction tx2 = (Transaction) tx2Control.getMock();

		session.beginTransaction();
		sessionControl.setReturnValue(tx1, 1);
		tx1.rollback();
		tx1Control.setVoidCallable(1);
		session.clear();
		sessionControl.setVoidCallable(1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx2, 1);
		tx2.commit();
		tx2Control.setVoidCallable(1);

		session.getFlushMode();
		sessionControl.setReturnValue(FlushMode.NEVER, 2);
		session.setFlushMode(FlushMode.AUTO);
		sessionControl.setVoidCallable(2);
		session.setFlushMode(FlushMode.NEVER);
		sessionControl.setVoidCallable(2);
		session.connection();
		sessionControl.setReturnValue(con, 4);
		con.isReadOnly();
		conControl.setReturnValue(false, 2);

		dsControl.replay();
		conControl.replay();
		sfControl.replay();
		sessionControl.replay();
		tx1Control.replay();
		tx2Control.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.setDataSource(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		TransactionSynchronizationManager.bindResource(sf, new SessionHolder(session));
		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));

		tt.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertEquals(tx1, sessionHolder.getTransaction());
				tt.execute(new TransactionCallbackWithoutResult() {
					public void doInTransactionWithoutResult(TransactionStatus status) {
						status.setRollbackOnly();
						HibernateTemplate ht = new HibernateTemplate(sf);
						ht.setExposeNativeSession(true);
						ht.execute(new HibernateCallback() {
							public Object doInHibernate(org.hibernate.Session sess) throws HibernateException {
								assertEquals(session, sess);
								return null;
							}
						});
					}
				});
			}
		});

		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
		assertTrue("Hasn't thread transaction", sessionHolder.getTransaction() == null);
		assertTrue("Not marked rollback-only", !sessionHolder.isRollbackOnly());

		tt.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sf);
				assertEquals(tx2, sessionHolder.getTransaction());
				HibernateTemplate ht = new HibernateTemplate(sf);
				ht.setExposeNativeSession(true);
				ht.execute(new HibernateCallback() {
					public Object doInHibernate(org.hibernate.Session sess) throws HibernateException {
						assertEquals(session, sess);
						return null;
					}
				});
			}
		});

		assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread transaction", sessionHolder.getTransaction() == null);
		TransactionSynchronizationManager.unbindResource(sf);
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		dsControl.verify();
		conControl.verify();
		sfControl.verify();
		sessionControl.verify();
		tx1Control.verify();
		tx2Control.verify();
	}

	public void testExistingTransactionWithPropagationNestedAndRollback() throws Exception {
		doTestExistingTransactionWithPropagationNestedAndRollback(false);
	}

	public void testExistingTransactionWithManualSavepointAndRollback() throws Exception {
		doTestExistingTransactionWithPropagationNestedAndRollback(true);
	}

	private void doTestExistingTransactionWithPropagationNestedAndRollback(final boolean manualSavepoint)
			throws Exception {

		if (JdkVersion.getMajorJavaVersion() < JdkVersion.JAVA_14) {
			return;
		}

		MockControl dsControl = MockControl.createControl(DataSource.class);
		final DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl mdControl = MockControl.createControl(DatabaseMetaData.class);
		DatabaseMetaData md = (DatabaseMetaData) mdControl.getMock();
		MockControl spControl = MockControl.createControl(Savepoint.class);
		Savepoint sp = (Savepoint) spControl.getMock();
		MockControl sfControl = MockControl.createControl(SessionFactory.class);
		final SessionFactory sf = (SessionFactory) sfControl.getMock();
		MockControl sessionControl = MockControl.createControl(Session.class);
		Session session = (Session) sessionControl.getMock();
		MockControl txControl = MockControl.createControl(Transaction.class);
		Transaction tx = (Transaction) txControl.getMock();
		MockControl queryControl = MockControl.createControl(Query.class);
		Query query = (Query) queryControl.getMock();

		final List list = new ArrayList();
		list.add("test");
		con.isReadOnly();
		conControl.setReturnValue(false, 1);
		sf.openSession();
		sfControl.setReturnValue(session, 1);
		session.beginTransaction();
		sessionControl.setReturnValue(tx, 1);
		session.connection();
		sessionControl.setReturnValue(con, 2);
		md.supportsSavepoints();
		mdControl.setReturnValue(true, 1);
		con.getMetaData();
		conControl.setReturnValue(md, 1);
		con.setSavepoint();
		conControl.setReturnValue(sp, 1);
		con.rollback(sp);
		conControl.setVoidCallable(1);
		session.createQuery("some query string");
		sessionControl.setReturnValue(query, 1);
		query.list();
		queryControl.setReturnValue(list, 1);
		session.close();
		sessionControl.setReturnValue(null, 1);
		tx.commit();
		txControl.setVoidCallable(1);
		dsControl.replay();
		conControl.replay();
		mdControl.replay();
		spControl.replay();
		sfControl.replay();
		sessionControl.replay();
		txControl.replay();
		queryControl.replay();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setJdbcExceptionTranslator(new SQLStateSQLExceptionTranslator());
		tm.setNestedTransactionAllowed(true);
		tm.setSessionFactory(sf);
		tm.setDataSource(ds);
		final TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setPropagationBehavior(TransactionDefinition.PROPAGATION_NESTED);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		Object result = tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				if (manualSavepoint) {
					Object savepoint = status.createSavepoint();
					status.rollbackToSavepoint(savepoint);
				}
				else {
					tt.execute(new TransactionCallbackWithoutResult() {
						protected void doInTransactionWithoutResult(TransactionStatus status) {
							assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
							assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
							status.setRollbackOnly();
						}
					});
				}
				HibernateTemplate ht = new HibernateTemplate(sf);
				return ht.find("some query string");
			}
		});
		assertTrue("Correct result list", result == list);

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
		dsControl.verify();
		conControl.verify();
		mdControl.verify();
		spControl.verify();
		sfControl.verify();
		sessionControl.verify();
		txControl.verify();
		queryControl.verify();
	}

	public void testTransactionCommitWithNonExistingDatabase() throws Exception {
		final DriverManagerDataSource ds = new DriverManagerDataSource();
		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
		lsfb.setDataSource(ds);
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", HSQLDialect.class.getName());
		lsfb.setHibernateProperties(props);
		lsfb.afterPropertiesSet();
		final SessionFactory sf = (SessionFactory) lsfb.getObject();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.afterPropertiesSet();
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.setTimeout(10);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		try {
			tt.execute(new TransactionCallback() {
				public Object doInTransaction(TransactionStatus status) {
					assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
					assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
					HibernateTemplate ht = new HibernateTemplate(sf);
					return ht.find("from java.lang.Object");
				}
			});
			fail("Should have thrown CannotCreateTransactionException");
		}
		catch (CannotCreateTransactionException ex) {
			// expected
		}

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	}

	public void testTransactionCommitWithNonExistingDatabaseAndLazyConnection() throws Exception {
		DriverManagerDataSource dsTarget = new DriverManagerDataSource();
		final LazyConnectionDataSourceProxy ds = new LazyConnectionDataSourceProxy();
		ds.setTargetDataSource(dsTarget);
		ds.setDefaultAutoCommit(true);
		ds.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		LocalSessionFactoryBean lsfb = new LocalSessionFactoryBean();
		lsfb.setDataSource(ds);
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", HSQLDialect.class.getName());
		lsfb.setHibernateProperties(props);
		lsfb.afterPropertiesSet();
		final SessionFactory sf = (SessionFactory) lsfb.getObject();

		HibernateTransactionManager tm = new HibernateTransactionManager();
		tm.setSessionFactory(sf);
		tm.afterPropertiesSet();
		TransactionTemplate tt = new TransactionTemplate(tm);
		tt.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
		tt.setTimeout(10);
		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());

		tt.execute(new TransactionCallback() {
			public Object doInTransaction(TransactionStatus status) {
				assertTrue("Has thread session", TransactionSynchronizationManager.hasResource(sf));
				assertTrue("Has thread connection", TransactionSynchronizationManager.hasResource(ds));
				HibernateTemplate ht = new HibernateTemplate(sf);
				return ht.find("from java.lang.Object");
			}
		});

		assertTrue("Hasn't thread session", !TransactionSynchronizationManager.hasResource(sf));
		assertTrue("Hasn't thread connection", !TransactionSynchronizationManager.hasResource(ds));
		assertTrue("JTA synchronizations not active", !TransactionSynchronizationManager.isSynchronizationActive());
	}

	protected void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}
