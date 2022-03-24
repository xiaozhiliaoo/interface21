/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.jmx.export.assembler;

import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.MBeanInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.interceptor.NopInterceptor;
import org.springframework.jmx.IJmxTestBean;
import org.springframework.jmx.JmxTestBean;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.metadata.JmxAttributeSource;
import org.springframework.jmx.support.ObjectNameManager;

/**
 * @author Rob Harrop
 */
public abstract class AbstractMetadataAssemblerTests extends AbstractJmxAssemblerTests {

	public void testDescription() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		assertEquals("The descriptions are not the same", "My Managed Bean",
				info.getDescription());
	}

	public void testAttributeDescriptionOnSetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = inf.getAttribute(AGE_ATTRIBUTE);
		assertEquals("The description for the age attribute is incorrect",
				"The Age Attribute", attr.getDescription());
	}

	public void testAttributeDescriptionOnGetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = inf.getAttribute(NAME_ATTRIBUTE);
		assertEquals("The description for the name attribute is incorrect",
				"The Name Attribute", attr.getDescription());
	}

	/**
	 * Tests the situation where the attribute is only defined on the getter.
	 */
	public void testReadOnlyAttribute() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = inf.getAttribute(AGE_ATTRIBUTE);
		assertFalse("The age attribute should not be writable", attr.isWritable());
	}

	public void testReadWriteAttribute() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = inf.getAttribute(NAME_ATTRIBUTE);
		assertTrue("The name attribute should be writable", attr.isWritable());
		assertTrue("The name attribute should be readable", attr.isReadable());
	}

	/**
	 * Tests the situation where the property only has a getter.
	 */
	public void testWithOnlyGetter() throws Exception {
		ModelMBeanInfo inf = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = inf.getAttribute("NickName");
		assertNotNull("Attribute should not be null", attr);
	}

	/**
	 * Tests the situation where the property only has a setter.
	 */
	public void testWithOnlySetter() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		ModelMBeanAttributeInfo attr = info.getAttribute("Superman");
		assertNotNull("Attribute should not be null", attr);
	}

	public void testManagedResourceDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		Descriptor desc = info.getMBeanDescriptor();

		assertEquals("Logging should be set to true", "true", desc.getFieldValue("log"));
		assertEquals("Log file should be jmx.log", "jmx.log", desc.getFieldValue("logFile"));
		assertEquals("Currency Time Limit should be 15", "15", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
		assertEquals("Persist Period should be 200", "200", desc.getFieldValue("persistPeriod"));
		assertEquals("Persist Location should be foo", "./foo", desc.getFieldValue("persistLocation"));
		assertEquals("Persist Name should be bar", "bar.jmx", desc.getFieldValue("persistName"));
	}

	public void testAttributeDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		Descriptor desc = info.getAttribute(NAME_ATTRIBUTE).getDescriptor();

		assertEquals("Default value should be foo", "foo", desc.getFieldValue("default"));
		assertEquals("Currency Time Limit should be 20", "20", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Persist Policy should be OnUpdate", "OnUpdate", desc.getFieldValue("persistPolicy"));
		assertEquals("Persist Period should be 300", "300", desc.getFieldValue("persistPeriod"));
	}

	public void testOperationDescriptor() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		Descriptor desc = info.getOperation("myOperation").getDescriptor();

		assertEquals("Currency Time Limit should be 30", "30", desc.getFieldValue("currencyTimeLimit"));
		assertEquals("Role should be \"operation\"", "operation", desc.getFieldValue("role"));
	}

	public void testOperationParameterMetadata() throws Exception {
		ModelMBeanInfo info = getMBeanInfoFromAssembler();
		ModelMBeanOperationInfo oper = info.getOperation("add");
		MBeanParameterInfo[] params = oper.getSignature();

		assertEquals("Invalid number of params", 2, params.length);
		assertEquals("Incorrect name for x param", "x", params[0].getName());
		assertEquals("Incorrect type for x param", int.class.getName(), params[0].getType());
		
		assertEquals("Incorrect name for y param", "y", params[1].getName());
		assertEquals("Incorrect type for y param", int.class.getName(), params[1].getType());
	}

	public void testWithCglibProxy() throws Exception {
		IJmxTestBean tb = createJmxTestBean();
		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(tb);
		pf.addAdvice(new NopInterceptor());
		Object proxy = pf.getProxy();

		MetadataMBeanInfoAssembler assembler = (MetadataMBeanInfoAssembler) getAssembler();

		MBeanExporter exporter = new MBeanExporter();
		exporter.setBeanFactory(getContext());
		exporter.setAssembler(assembler);

		String objectName = "spring:bean=test,proxy=true";

		Map beans = new HashMap();
		beans.put(objectName, proxy);
		exporter.setBeans(beans);
		exporter.afterPropertiesSet();

		MBeanInfo inf = server.getMBeanInfo(ObjectNameManager.getInstance(objectName));
		assertEquals("Incorrect number of operations", getExpectedOperationCount(), inf.getOperations().length);
		assertEquals("Incorrect number of attributes", getExpectedAttributeCount(), inf.getAttributes().length);

		assertTrue("Not included in autodetection", assembler.includeBean(proxy.getClass(), "some bean name"));
	}

	public void testWithJdkProxy() throws Exception {
		IJmxTestBean tb = createJmxTestBean();
		ProxyFactory pf = new ProxyFactory();
		pf.setTarget(tb);
		pf.addInterface(IJmxTestBean.class);
		pf.addAdvice(new NopInterceptor());
		Object proxy = pf.getProxy();

		MetadataMBeanInfoAssembler assembler = (MetadataMBeanInfoAssembler) getAssembler();

		MBeanExporter exporter = new MBeanExporter();
		exporter.setBeanFactory(getContext());
		exporter.setAssembler(assembler);

		String objectName = "spring:bean=test,proxy=true";

		Map beans = new HashMap();
		beans.put(objectName, proxy);
		exporter.setBeans(beans);

		try {
			exporter.afterPropertiesSet();
			fail("Should have thrown an IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	protected abstract String getObjectName();

	protected int getExpectedAttributeCount() {
		return 4;
	}

	protected int getExpectedOperationCount() {
		return 7;
	}

	protected IJmxTestBean createJmxTestBean() {
		return new JmxTestBean();
	}

	protected MBeanInfoAssembler getAssembler() {
		MetadataMBeanInfoAssembler assembler = new MetadataMBeanInfoAssembler();
		assembler.setAttributeSource(getAttributeSource());
		return assembler;
	}

	protected abstract JmxAttributeSource getAttributeSource();

}
