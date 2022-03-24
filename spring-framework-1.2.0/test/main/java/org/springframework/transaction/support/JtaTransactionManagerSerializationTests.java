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

package org.springframework.transaction.support;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import junit.framework.TestCase;
import org.easymock.MockControl;

import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.transaction.jta.JtaTransactionManager;
import org.springframework.util.SerializationTestUtils;

/**
 * @author Rod Johnson
 */
public class JtaTransactionManagerSerializationTests extends TestCase {

	public void testSerializable() throws Exception {
		MockControl utMock = MockControl.createControl(UserTransaction.class);
		UserTransaction ut = (UserTransaction) utMock.getMock();
		MockControl ut2Mock = MockControl.createControl(UserTransaction.class);
		UserTransaction ut2 = (UserTransaction) ut2Mock.getMock();
		MockControl tmMock = MockControl.createControl(TransactionManager.class);
		TransactionManager tm = (TransactionManager) tmMock.getMock();

		JtaTransactionManager jtam = new JtaTransactionManager();
		jtam.setUserTransaction(ut);
		jtam.setTransactionManager(tm);
		jtam.afterPropertiesSet();

		SimpleNamingContextBuilder jndiEnv = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		jndiEnv.bind(JtaTransactionManager.DEFAULT_USER_TRANSACTION_NAME, ut2);
		JtaTransactionManager jtam2 = (JtaTransactionManager) SerializationTestUtils.serializeAndDeserialize(jtam);
		
		// should do client-side lookup
		assertNotNull("Logger must survive serialization", jtam2.logger);
		assertTrue("UserTransaction looked up on client", jtam2.getUserTransaction() == ut2);
		assertNull("TransactionManager didn't survive", jtam2.getTransactionManager());
	}

}
