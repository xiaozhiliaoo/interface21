/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.web.servlet.view.jasperreports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.jasperreports.PersonBean;
import org.springframework.ui.jasperreports.ProductBean;

/**
 * @author Rob Harrop
 * @since 18.11.2004
 */
public abstract class AbstractJasperReportsTests extends TestCase {

	protected static final String COMPILED_REPORT =
			"org/springframework/ui/jasperreports/DataSourceReport.jasper";

	protected static final String UNCOMPILED_REPORT =
			"org/springframework/ui/jasperreports/DataSourceReport.jrxml";

	protected static final String SUB_REPORT_PARENT =
			"org/springframework/ui/jasperreports/subReportParent.jrxml";

	protected static boolean canCompileReport;

	static {
		try {
			Class.forName("org.eclipse.jdt.internal.compiler.Compiler");
			canCompileReport = true;
		}
		catch (ClassNotFoundException ex) {
			canCompileReport = false;
		}
	}


	protected MockHttpServletRequest request;

	protected MockHttpServletResponse response;


	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
	}


	protected Map getModel() {
		Map model = new HashMap();
		model.put("ReportTitle", "Dear Lord!");
		model.put("dataSource", new JRBeanCollectionDataSource(getData()));
		extendModel(model);
		return model;
	}

	/**
	 * Subclasses can extend the model if they need to.
	 */
	protected void extendModel(Map model) {
	}

	protected List getData() {
		List list = new ArrayList();
		for (int x = 0; x < 10; x++) {
			PersonBean bean = new PersonBean();
			bean.setId(x);
			bean.setName("Rob Harrop");
			bean.setStreet("foo");
			list.add(bean);
		}
		return list;
	}

	protected List getProductData() {
		List list = new ArrayList();
		for (int x = 0; x < 10; x++) {
			ProductBean bean = new ProductBean();
			bean.setId(x);
			bean.setName("Foo Bar");
			bean.setPrice(1.9f);
			bean.setQuantity(1.0f);

			list.add(bean);
		}
		return list;
	}

}
