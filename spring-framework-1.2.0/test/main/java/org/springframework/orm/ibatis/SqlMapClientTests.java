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

package org.springframework.orm.ibatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.ibatis.common.util.PaginatedArrayList;
import com.ibatis.common.util.PaginatedList;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapExecutor;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.ibatis.sqlmap.client.event.RowHandler;
import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 * @since 09.10.2004
 */
public class SqlMapClientTests extends TestCase {

	public void testSqlMapClientFactoryBeanWithoutConfig() throws Exception {
		SqlMapClientFactoryBean factory = new SqlMapClientFactoryBean();
		// explicitly set to null, don't know why ;-)
		factory.setConfigLocation(null);
		try {
			factory.afterPropertiesSet();
			fail("Should have thrown IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testSqlMapClientTemplate() throws SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		final Connection con = (Connection) conControl.getMock();
		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.close();
		conControl.setVoidCallable(1);
		dsControl.replay();
		conControl.replay();

		MockControl smsControl = MockControl.createControl(SqlMapSession.class);
		final SqlMapSession sms = (SqlMapSession) smsControl.getMock();
		MockControl smcControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient smc = (SqlMapClient) smcControl.getMock();
		smc.openSession();
		smcControl.setReturnValue(sms);
		sms.setUserConnection(con);
		smsControl.setVoidCallable();
		sms.close();
		smsControl.setVoidCallable();
		smsControl.replay();
		smcControl.replay();

		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setDataSource(ds);
		template.setSqlMapClient(smc);
		template.afterPropertiesSet();
		Object result = template.execute(new SqlMapClientCallback() {
			public Object doInSqlMapClient(SqlMapExecutor executor) {
				assertTrue(executor == sms);
				return "done";
			}
		});
		assertEquals("done", result);
		dsControl.verify();
		conControl.verify();
		smsControl.verify();
		smcControl.verify();
	}

	public void testQueryForObjectOnSqlMapClient() throws SQLException {
		DataSource dataSource = new TransactionAwareDataSourceProxy(new DriverManagerDataSource());
		MockControl clientControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient client = (SqlMapClient) clientControl.getMock();
		client.getDataSource();
		clientControl.setReturnValue(dataSource, 2);
		client.queryForObject("myStatement", "myParameter");
		clientControl.setReturnValue("myResult", 1);
		clientControl.replay();
		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setSqlMapClient(client);
		assertEquals("myResult", template.queryForObject("myStatement", "myParameter"));
		clientControl.verify();
	}

	public void testQueryForObjectOnSqlMapSession() throws SQLException {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		MockControl conControl = MockControl.createControl(Connection.class);
		Connection con = (Connection) conControl.getMock();
		MockControl clientControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient client = (SqlMapClient) clientControl.getMock();
		MockControl sessionControl = MockControl.createControl(SqlMapSession.class);
		SqlMapSession session = (SqlMapSession) sessionControl.getMock();

		ds.getConnection();
		dsControl.setReturnValue(con, 1);
		con.close();
		conControl.setVoidCallable(1);
		client.getDataSource();
		clientControl.setReturnValue(ds, 3);
		client.openSession();
		clientControl.setReturnValue(session, 1);
		session.setUserConnection(con);
		sessionControl.setVoidCallable(1);
		session.queryForObject("myStatement", "myParameter");
		sessionControl.setReturnValue("myResult", 1);
		session.close();
		sessionControl.setVoidCallable(1);

		dsControl.replay();
		conControl.replay();
		clientControl.replay();
		sessionControl.replay();

		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setSqlMapClient(client);
		assertEquals("myResult", template.queryForObject("myStatement", "myParameter"));

		dsControl.verify();
		clientControl.verify();
	}

	public void testQueryForObject() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForObject("myStatement", "myParameter");
		template.executorControl.setReturnValue("myResult", 1);
		template.executorControl.replay();
		assertEquals("myResult", template.queryForObject("myStatement", "myParameter"));
		template.executorControl.verify();
	}

	public void testQueryForObjectWithResultObject() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForObject("myStatement", "myParameter", "myResult");
		template.executorControl.setReturnValue("myResult", 1);
		template.executorControl.replay();
		assertEquals("myResult", template.queryForObject("myStatement", "myParameter", "myResult"));
		template.executorControl.verify();
	}

	public void testQueryForList() throws SQLException {
		List result = new ArrayList();
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForList("myStatement", "myParameter");
		template.executorControl.setReturnValue(result, 1);
		template.executorControl.replay();
		assertEquals(result, template.queryForList("myStatement", "myParameter"));
		template.executorControl.verify();
	}

	public void testQueryForListWithResultSize() throws SQLException {
		List result = new ArrayList();
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForList("myStatement", "myParameter", 10, 20);
		template.executorControl.setReturnValue(result, 1);
		template.executorControl.replay();
		assertEquals(result, template.queryForList("myStatement", "myParameter", 10, 20));
		template.executorControl.verify();
	}

	public void testQueryWithRowHandler() throws SQLException {
		RowHandler rowHandler = new RowHandler() {
			public void handleRow(Object row) {
			}
		};
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryWithRowHandler("myStatement", "myParameter", rowHandler);
		template.executorControl.setVoidCallable(1);
		template.executorControl.replay();
		template.queryWithRowHandler("myStatement", "myParameter", rowHandler);
		template.executorControl.verify();
	}

	public void testQueryForPaginatedList() throws SQLException {
		PaginatedList result = new PaginatedArrayList(10);
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForPaginatedList("myStatement", "myParameter", 10);
		template.executorControl.setReturnValue(result, 1);
		template.executorControl.replay();
		assertEquals(result, template.queryForPaginatedList("myStatement", "myParameter", 10));
		template.executorControl.verify();
	}

	public void testQueryForMap() throws SQLException {
		Map result = new HashMap();
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForMap("myStatement", "myParameter", "myKey");
		template.executorControl.setReturnValue(result, 1);
		template.executorControl.replay();
		assertEquals(result, template.queryForMap("myStatement", "myParameter", "myKey"));
		template.executorControl.verify();
	}

	public void testQueryForMapWithValueProperty() throws SQLException {
		Map result = new HashMap();
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.queryForMap("myStatement", "myParameter", "myKey", "myValue");
		template.executorControl.setReturnValue(result, 1);
		template.executorControl.replay();
		assertEquals(result, template.queryForMap("myStatement", "myParameter", "myKey", "myValue"));
		template.executorControl.verify();
	}

	public void testInsert() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.insert("myStatement", "myParameter");
		template.executorControl.setReturnValue("myResult", 1);
		template.executorControl.replay();
		assertEquals("myResult", template.insert("myStatement", "myParameter"));
		template.executorControl.verify();
	}

	public void testUpdate() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.update("myStatement", "myParameter");
		template.executorControl.setReturnValue(10, 1);
		template.executorControl.replay();
		assertEquals(10, template.update("myStatement", "myParameter"));
		template.executorControl.verify();
	}

	public void testDelete() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.delete("myStatement", "myParameter");
		template.executorControl.setReturnValue(10, 1);
		template.executorControl.replay();
		assertEquals(10, template.delete("myStatement", "myParameter"));
		template.executorControl.verify();
	}

	public void testUpdateWithRequiredRowsAffected() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.update("myStatement", "myParameter");
		template.executorControl.setReturnValue(10, 1);
		template.executorControl.replay();
		template.update("myStatement", "myParameter", 10);
		template.executorControl.verify();
	}

	public void testUpdateWithRequiredRowsAffectedAndInvalidRowCount() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.update("myStatement", "myParameter");
		template.executorControl.setReturnValue(20, 1);
		template.executorControl.replay();
		try {
			template.update("myStatement", "myParameter", 10);
			fail("Should have thrown JdbcUpdateAffectedIncorrectNumberOfRowsException");
		}
		catch (JdbcUpdateAffectedIncorrectNumberOfRowsException ex) {
			// expected
			assertEquals(10, ex.getExpectedRowsAffected());
			assertEquals(20, ex.getActualRowsAffected());
		}
		template.executorControl.verify();
	}

	public void testDeleteWithRequiredRowsAffected() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.delete("myStatement", "myParameter");
		template.executorControl.setReturnValue(10, 1);
		template.executorControl.replay();
		template.delete("myStatement", "myParameter", 10);
		template.executorControl.verify();
	}

	public void testDeleteWithRequiredRowsAffectedAndInvalidRowCount() throws SQLException {
		TestSqlMapClientTemplate template = new TestSqlMapClientTemplate();
		template.executor.delete("myStatement", "myParameter");
		template.executorControl.setReturnValue(20, 1);
		template.executorControl.replay();
		try {
			template.delete("myStatement", "myParameter", 10);
			fail("Should have thrown JdbcUpdateAffectedIncorrectNumberOfRowsException");
		}
		catch (JdbcUpdateAffectedIncorrectNumberOfRowsException ex) {
			// expected
			assertEquals(10, ex.getExpectedRowsAffected());
			assertEquals(20, ex.getActualRowsAffected());
		}
		template.executorControl.verify();
	}

	public void testSqlMapClientDaoSupport() throws Exception {
		MockControl dsControl = MockControl.createControl(DataSource.class);
		DataSource ds = (DataSource) dsControl.getMock();
		SqlMapClientDaoSupport testDao = new SqlMapClientDaoSupport() {
		};
		testDao.setDataSource(ds);
		assertEquals(ds, testDao.getDataSource());

		MockControl smcControl = MockControl.createControl(SqlMapClient.class);
		SqlMapClient smc = (SqlMapClient) smcControl.getMock();
		smcControl.replay();

		testDao.setSqlMapClient(smc);
		assertEquals(smc, testDao.getSqlMapClient());

		SqlMapClientTemplate template = new SqlMapClientTemplate();
		template.setDataSource(ds);
		template.setSqlMapClient(smc);
		testDao.setSqlMapClientTemplate(template);
		assertEquals(template, testDao.getSqlMapClientTemplate());

		testDao.afterPropertiesSet();
	}


	private static class TestSqlMapClientTemplate extends SqlMapClientTemplate {

		public MockControl executorControl = MockControl.createControl(SqlMapExecutor.class);
		public SqlMapExecutor executor = (SqlMapExecutor) executorControl.getMock();

		public Object execute(SqlMapClientCallback action) throws DataAccessException {
			try {
				return action.doInSqlMapClient(executor);
			}
			catch (SQLException ex) {
				throw getExceptionTranslator().translate("SqlMapClient operation", null, ex);
			}
		}
	}

}
