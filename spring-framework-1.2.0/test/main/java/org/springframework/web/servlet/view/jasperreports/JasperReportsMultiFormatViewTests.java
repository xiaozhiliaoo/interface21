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

package org.springframework.web.servlet.view.jasperreports;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;

/**
 * @author Rob Harrop
 */
public class JasperReportsMultiFormatViewTests extends AbstractJasperReportsViewTests {

	protected void extendModel(Map model) {
		model.put(getDiscriminatorKey(), "csv");
	}

	public void testSimpleHtmlRender() throws Exception {
		if (!canCompileReport) {
			return;
		}

		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);

		Map model = getBaseModel();
		model.put(getDiscriminatorKey(), "html");

		view.render(model, request, response);

		assertEquals("Invalid content type", "text/html", response.getContentType());
	}

	public void testOverrideContentDisposition() throws Exception {
		if (!canCompileReport) {
			return;
		}

		AbstractJasperReportsView view = getView(UNCOMPILED_REPORT);

		Map model = getBaseModel();
		model.put(getDiscriminatorKey(), "csv");

		String headerValue = "inline; filename=foo.txt";

		Properties mappings = new Properties();
		mappings.put("csv", headerValue);

		((JasperReportsMultiFormatView) view).setContentDispositionMappings(mappings);

		view.render(model, request, response);

		assertEquals("Invalid Content-Disposition header value", headerValue,
				response.getHeader("Content-Disposition"));
	}

	public void testExporterParametersAreCarriedAcross() throws Exception {
		if (!canCompileReport) {
			return;
		}

		JasperReportsMultiFormatView view = (JasperReportsMultiFormatView) getView(UNCOMPILED_REPORT);

		Properties mappings = new Properties();
		mappings.put("test", ExporterParameterTestView.class.getName());

		Map exporterParameters = new HashMap();

		// test view class performs the assertions - robh
		exporterParameters.put(ExporterParameterTestView.TEST_PARAM, "foo");

		view.setExporterParameters(exporterParameters);
		view.setFormatMappings(mappings);
		view.initApplicationContext();

		Map model = getBaseModel();
		model.put(getDiscriminatorKey(), "test");

		view.render(model, request, response);
	}

	protected String getDiscriminatorKey() {
		return "format";
	}

	protected AbstractJasperReportsView getViewImplementation() {
		return new JasperReportsMultiFormatView();
	}

	protected String getDesiredContentType() {
		return "text/csv";
	}

	private Map getBaseModel() {
		Map model = new HashMap();
		model.put("ReportTitle", "Foo");
		model.put("dataSource", getData());
		return model;
	}


	public static class ExporterParameterTestView extends AbstractJasperReportsView {

		public static final String TEST_PARAM = "net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI";

		protected void renderReport(JasperPrint filledReport, Map parameters, HttpServletResponse response) {
			assertNotNull("Exporter parameters are null", getExporterParameters());
			assertEquals("Incorrect number of exporter parameters", 1, getExporterParameters().size());
		}
	}

}
