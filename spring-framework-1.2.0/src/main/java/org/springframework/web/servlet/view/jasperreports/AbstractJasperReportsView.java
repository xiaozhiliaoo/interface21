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

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRDataSourceProvider;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRCompiler;
import net.sf.jasperreports.engine.design.JRDefaultCompiler;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.springframework.context.ApplicationContextException;
import org.springframework.core.io.Resource;
import org.springframework.ui.jasperreports.JasperReportsUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;

/**
 * Base class for all JasperReports views. Applies on-the-fly compilation
 * of report designs as required and coordinates the rendering process.
 * The resource path of the main report needs to be specified as <code>url</code>.
 *
 * <p>This class is responsible for getting report data from the model that has
 * been provided to the view. The default implementation checks for a model object
 * under the specified <code>reportDataKey</code> first, then falls back to looking
 * for a value of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
 * object array (in that order).
 *
 * <p>If no <code>JRDataSource</code> can be found in the model, then reports will
 * be filled using the configured <code>javax.sql.DataSource</code> if any. If neither
 * a <code>JRDataSource</code> or <code>javax.sql.DataSource</code> is available then
 * an <code>IllegalArgumentException</code> is raised.
 *
 * <p>Provides support for sub-reports through the <code>subReportUrls</code> and
 * <code>subReportDataKeys</code> properties.
 *
 * <p>When using sub-reports, the master report should be configured using the
 * <code>url</code> property and the sub-reports files should be configured using
 * the <code>subReportUrls</code> property. Each entry in the <code>subReportUrls</code>
 * Map corresponds to an individual sub-report. The key of an entry must match up
 * to a sub-report parameter in your report file of type
 * <code>net.sf.jasperreports.engine.JasperReport</code>,
 * and the value of an entry must be the URL for the sub-report file.
 *
 * <p>For sub-reports that require an instance of <code>JRDataSource</code>, that is,
 * they don't have a hard-coded query for data retrieval, you can include the
 * appropriate data in your model as would with the data source for the parent report.
 * However, you must provide a List of parameter names that need to be converted to
 * <code>JRDataSource</code> instances for the sub-report via the
 * <code>subReportDataKeys</code> property. When using <code>JRDataSource</code>
 * instances for sub-reports, you <i>must</i> specify a value for the
 * <code>reportDataKey</code> property, indicating the data to use for the main report.
 *
 * <p>Allows for exporter parameters to be configured declatively using the
 * <code>exporterParameters</code> property. This is a <code>Map</code> typed
 * property where the key of an entry corresponds to the fully-qualified name
 * of the static field for the <code>JRExporterParameter</code> and the value
 * of an entry is the value you want to assign to the exporter parameter.
 *
 * <p>Response headers can be controlled via the <code>headers</code> property. Spring
 * will attempt to set the correct value for the <code>Content-Diposition</code> header
 * so that reports render correctly in Internet Explorer. However, you can override this
 * setting through the <code>headers</code> property.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see #setUrl
 * @see #getReportData
 * @see #setJdbcDataSource(DataSource)
 * @see #setSubReportDataKeys(String[])
 * @see #setSubReportUrls(Properties)
 * @see #setExporterParameters(Map)
 * @see #setHeaders(Properties)
 */
public abstract class AbstractJasperReportsView extends AbstractUrlBasedView {

	/**
	 * Constant that defines "Content-Disposition" header.
	 */
	protected static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

	/**
	 * Stores the default Content-Disposition header. Used to make IE play nice.
	 */
	private static final String CONTENT_DISPOSITION_INLINE = "inline";


	/**
	 * A String key used to lookup the <code>JRDataSource</code> in the model.
	 */
	private String reportDataKey;

	/**
	 * Stores the paths to any sub-report files used by this top-level report,
	 * along with the keys they are mapped to in the top-level report file.
	 */
	private Properties subReportUrls;

	/**
	 * Stores the names of any data source objects that need to be converted to
	 * <code>JRDataSource</code> instances and included in the report parameters
	 * to be passed on to a sub-report.
	 */
	private String[] subReportDataKeys;

	/**
	 * The <code>JasperReport</code> that is used to render the view.
	 */
	private JasperReport report;

	/**
	 * Holds mappings between sub-report keys and <code>JasperReport</code> objects.
	 */
	private Map subReports;

	/**
	 * Stores the headers to written with each response
	 */
	private Properties headers;

	/**
	 * Stores the exporter parameters passed in by the user as passed in by the user. May be keyed as
	 * <code>String</code>s with the fully qualified name of the exporter parameter field.
	 */
	private Map exporterParameters;

	/**
	 * Stores the converted exporter parameters - keyed by <code>JRExporterParameter</code>.
	 */
	private Map convertedExporterParameters;

	/**
	 * Stores the <code>DataSource</code>, if any, used as the report data source.
	 */
	private DataSource jdbcDataSource;

	/**
	 * Holds the JRCompiler implementation to use for compiling reports on-the-fly.
	 */
	private JRCompiler reportCompiler = new JRDefaultCompiler();


	/**
	 * Set the name of the model attribute that represents the report data.
	 * If not specified, the model map will be searched for a matching value type.
	 * <p>A <code>JRDataSource</code> will be taken as-is. For other types, conversion
	 * will apply: By default, a <code>java.util.Collection</code> will be converted
	 * to <code>JRBeanCollectionDataSource</code>, and an object array to
	 * <code>JRBeanArrayDataSource</code>.
	 * <p><b>Note:</b> If you pass in a Collection or object array in the model map
	 * for use as plain report parameter, rather than as report data to extract fields
	 * from, you need to specify the key for the actual report data to use, to avoid
	 * mis-detection of report data by type.
	 * @see #convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	public void setReportDataKey(String reportDataKey) {
		this.reportDataKey = reportDataKey;
	}

	/**
	 * Specify resource paths which must be loaded as instances of
	 * <code>JasperReport</code> and passed to the JasperReports engine for
	 * rendering as sub-reports, under the same keys as in this mapping.
	 * @param subReports mapping between model keys and resource paths
	 * (Spring resource locations)
	 * @see #setUrl
	 * @see org.springframework.context.ApplicationContext#getResource
	 */
	public void setSubReportUrls(Properties subReports) {
		this.subReportUrls = subReports;
	}

	/**
	 * Set the list of names corresponding to the model parameters that will contain
	 * data source objects for use in sub-reports. Spring will convert these objects
	 * to instances of <code>JRDataSource</code> where applicable and will then
	 * include the resulting <code>JRDataSource</code> in the parameters passed into
	 * the JasperReports engine.
	 * <p>The name specified in the list should correspond to an attribute in the
	 * model Map, and to a sub-report data source parameter in your report file.
	 * If you pass in <code>JRDataSource</code> objects as model attributes,
	 * specifing this list of keys is not required.
	 * <p>If you specify a list of sub-report data keys, it is required to also
	 * specify a <code>reportDataKey</code> for the main report, to avoid confusion
	 * between the data source objects for the various reports involved.
	 * @param subReportDataKeys list of names for sub-report data source objects
	 * @see #setReportDataKey
	 * @see #convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	public void setSubReportDataKeys(String[] subReportDataKeys) {
		this.subReportDataKeys = subReportDataKeys;
	}

	/**
	 * Specify the set of headers that are included in each of response.
	 * @param headers the headers to write to each response.
	 */
	public void setHeaders(Properties headers) {
		this.headers = headers;
	}

	/**
	 * Set the exporter parameters that should be used when rendering a view.
	 * @param parameters <code>Map</code> with the fully qualified field name
	 * of the <code>JRExporterParameter</code> instance as key
	 * (e.g. "net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI")
	 * and the value you wish to assign to the parameter as value
	 */
	public void setExporterParameters(Map parameters) {
		// NOTE: Removed conversion from here since configuration of parameters
		// can also happen through access to the underlying Map using
		// getExporterParameters(). Conversion now happens in initApplicationContext,
		// and subclasses use getConvertedExporterParameters() to access the converted
		// parameter Map - robh.
		this.exporterParameters = parameters;
	}

	/**
	 * Return the exporter parameters that this view uses, if any.
	 */
	public Map getExporterParameters() {
		return this.exporterParameters;
	}

	/**
	 * Allows subclasses to retrieve the converted exporter parameters.
	 */
	protected Map getConvertedExporterParameters() {
		return this.convertedExporterParameters;
	}

	/**
	 * Specify the <code>javax.sql.DataSource</code> to use for reports with
	 * embedded SQL statements.
	 */
	public void setJdbcDataSource(DataSource jdbcDataSource) {
		this.jdbcDataSource = jdbcDataSource;
	}

	/**
	 * Return the <code>javax.sql.DataSource</code> that this view uses, if any.
	 */
	protected DataSource getJdbcDataSource() {
		return jdbcDataSource;
	}

	/**
	 * Specify the JRCompiler implementation to use for compiling a ".jrxml"
	 * report file on-the-fly into a report class.
	 * <p>By default, a JRDefaultCompiler will be used, delegating to the
	 * Eclipse JDT compiler or the Sun JDK compiler underneath.
	 * @see net.sf.jasperreports.engine.design.JRDefaultCompiler
	 */
	public void setReportCompiler(JRCompiler reportCompiler) {
		this.reportCompiler = reportCompiler;
	}

	/**
	 * Return the JRCompiler instance to use for compiling ".jrxml" report files.
	 */
	protected JRCompiler getReportCompiler() {
		return reportCompiler;
	}


	/**
	 * Checks to see that a valid report file URL is supplied in the
	 * configuration. Compiles the report file is necessary.
	 */
	protected void initApplicationContext() throws ApplicationContextException {
		super.initApplicationContext();

		Resource mainReport = getApplicationContext().getResource(getUrl());
		this.report = loadReport(mainReport);

		// Load sub reports if required, and check data source parameters.
		if (this.subReportUrls != null) {
			if (this.subReportDataKeys != null && this.subReportDataKeys.length > 0 &&
							this.reportDataKey == null) {
				throw new ApplicationContextException(
						"'reportDataKey' for main report is required when specifying a value for 'subReportDataKeys'");
			}
			this.subReports = new HashMap(this.subReportUrls.size());
			for (Enumeration urls = this.subReportUrls.propertyNames(); urls.hasMoreElements();) {
				String key = (String) urls.nextElement();
				String path = this.subReportUrls.getProperty(key);
				Resource resource = getApplicationContext().getResource(path);
				this.subReports.put(key, loadReport(resource));
			}
		}

		// Convert user-supplied exporterParameters.
		convertExporterParameters();

		if (this.headers == null) {
			this.headers = new Properties();
		}
		if (!this.headers.containsKey(HEADER_CONTENT_DISPOSITION)) {
			this.headers.setProperty(HEADER_CONTENT_DISPOSITION, CONTENT_DISPOSITION_INLINE);
		}
	}

	/**
	 * Converts the exporter parameters passed in by the user which may be keyed
	 * by <code>String</code>s corresponding to the fully qualified name of the
	 * <code>JRExporterParameter</code> into parameters which are keyed by
	 * <code>JRExporterParameter</code>.
	 * @see #getExporterParameter(Object)
	 */
	protected final void convertExporterParameters() {
		if (this.exporterParameters != null) {
			this.convertedExporterParameters = new HashMap(this.exporterParameters.size());
			for (Iterator it = this.exporterParameters.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				this.convertedExporterParameters.put(getExporterParameter(entry.getKey()), entry.getValue());
			}
		}
	}

	/**
	 * Return a <code>JRExporterParameter</code> for the given parameter object,
	 * converting it from a String if necessary.
	 * @param parameter the parameter object, either a String or a JRExporterParameter
	 * @return a JRExporterParameter for the given parameter object
	 * @see #convertToExporterParameter(String)
	 */
	protected JRExporterParameter getExporterParameter(Object parameter) {
		if (parameter instanceof JRExporterParameter) {
			return (JRExporterParameter) parameter;
		}
		if (parameter instanceof String) {
			return convertToExporterParameter((String) parameter);
		}
		throw new IllegalArgumentException(
				"Parameter [" + parameter + "] is invalid type. Should be either String or JRExporterParameter.");
	}

	/**
	 * Convert the given fully qualified field name to a corresponding
	 * JRExporterParameter instance.
	 * @param fqFieldName the fully qualified field name, consisting
	 * of the class name followed by a dot followed by the field name
	 * (e.g. "net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI")
	 * @return the corresponding JRExporterParameter instance
	 */
	protected JRExporterParameter convertToExporterParameter(String fqFieldName) {
		int index = fqFieldName.lastIndexOf('.');
		if (index == -1 || index == fqFieldName.length()) {
			throw new IllegalArgumentException(
					"Parameter name [" + fqFieldName + "] is not a valid static field. " +
					"The parameter name must map to a static field such as " +
					"[net.sf.jasperreports.engine.export.JRHtmlExporterParameter.IMAGES_URI]");
		}
		String className = fqFieldName.substring(0, index);
		String fieldName = fqFieldName.substring(index + 1);

		try {
			Class cls = ClassUtils.forName(className);
			Field field = cls.getField(fieldName);

			if (JRExporterParameter.class.isAssignableFrom(field.getType())) {
				try {
					return (JRExporterParameter) field.get(null);
				}
				catch (IllegalAccessException ex) {
					throw new IllegalArgumentException(
							"Unable to access field [" + fieldName + "] of class [" + className + "]. " +
							"Check that it is static and accessible.");
				}
			}
			else {
				throw new IllegalArgumentException("Field [" + fieldName + "] on class [" + className +
						"] is not assignable from JRExporterParameter - check the type of this field.");
			}
		}
		catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException(
					"Class [" + className + "] in key [" + fqFieldName + "] could not be found.");
		}
		catch (NoSuchFieldException ex) {
			throw new IllegalArgumentException("Field [" + fieldName + "] in key [" + fqFieldName +
					"] could not be found on class [" + className + "].");
		}
	}

	/**
	 * Loads a <code>JasperReport</code> from the specified <code>Resource</code>. If
	 * the <code>Resource</code> points to an uncompiled report design file then the
	 * report file is compiled dynamically and loaded into memory.
	 * @param resource the <code>Resource</code> containing the report definition or design
	 * @return a <code>JasperReport</code> instance
	 */
	private JasperReport loadReport(Resource resource) throws ApplicationContextException {
		try {
			String fileName = resource.getFilename();
			if (fileName.endsWith(".jasper")) {
				// load pre-compiled report
				if (logger.isInfoEnabled()) {
					logger.info("Loading pre-compiled Jasper Report from " + resource);
				}
				return (JasperReport) JRLoader.loadObject(resource.getInputStream());
			}
			else if (fileName.endsWith(".jrxml")) {
				// compile report on-the-fly
				if (logger.isInfoEnabled()) {
					logger.info("Compiling Jasper Report loaded from " + resource);
				}
				JasperDesign design = JRXmlLoader.load(resource.getInputStream());
				return getReportCompiler().compileReport(design);
			}
			else {
				throw new IllegalArgumentException(
						"Report URL [" + getUrl() + "] must end in either .jasper or .jrxml");
			}
		}
		catch (IOException ex) {
			throw new ApplicationContextException(
					"Could not load JasperReports report for URL [" + getUrl() + "]", ex);
		}
		catch (JRException ex) {
			throw new ApplicationContextException(
					"Could not parse JasperReports report for URL [" + getUrl() + "]", ex);
		}
	}


	/**
	 * Finds the report data to use for rendering the report and then invokes the
	 * <code>renderReport</code> method that should be implemented by the subclass.
	 *
	 * @param model the model map, as passed in for view rendering. Must contain
	 * a report data value that can be converted to a <code>JRDataSource</code>,
	 * acccording to the <code>getReportData</code> method.
	 * @see #getReportData
	 */
	protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType(getContentType());

		if (this.subReports != null) {
			// Expose sub-reports as model attributes.
			model.putAll(this.subReports);

			// Transform any collections etc into JRDataSources for sub reports.
			if (this.subReportDataKeys != null) {
				for (int i = 0; i < this.subReportDataKeys.length; i++) {
					String key = this.subReportDataKeys[i];
					model.put(key, convertReportData(model.get(key)));
				}
			}
		}

		populateHeaders(response);
		JasperPrint filledReport = fillReport(model);
		renderReport(filledReport, model, response);
	}

	/**
	 * Creates a populated <code>JasperPrint</code> instance from the configured
	 * <code>JasperReport</code> instance. By default, will use any <code>JRDataSource</code>
	 * instance (or wrappable <code>Object</code>) that can be located using
	 * <code>getReportData(Map)</code>. If no <code>JRDataSource</code> can be found, will use a
	 * <code>Connection</code> obtained from the configured <code>javax.sql.DataSource</code>.
	 * @param model the model for this request
	 * @throws IllegalArgumentException if no <code>JRDataSource</code> can be found
	 * and no <code>javax.sql.DataSource</code> is supplied
	 * @throws SQLException if there is an error when populating the report using
	 * the <code>javax.sql.DataSource</code>
	 * @throws JRException if there is an error when populating the report using
	 * a <code>JRDataSource</code>
	 * @return the populated <code>JasperPrint</code> instance
	 * @see #getReportData
	 * @see #setJdbcDataSource
	 */
	protected JasperPrint fillReport(Map model) throws IllegalArgumentException, SQLException, JRException {
		// Determine JRDataSource for main report.
		JRDataSource jrDataSource = getReportData(model);

		if (jrDataSource == null && this.jdbcDataSource == null) {
			throw new IllegalArgumentException(
					"No report data source found in model and no [javax.sql.DataSource] specified in configuration");
		}

		if (jrDataSource != null) {
			// Use the JasperReports JRDataSource.
			if (logger.isDebugEnabled()) {
				logger.debug("Filling report with JRDataSource [" + jrDataSource + "].");
			}
			return JasperFillManager.fillReport(this.report, model, jrDataSource);
		}
		else {
			// Use the JDBC DataSource.
			if (logger.isDebugEnabled()) {
				logger.debug("Filling report with JDBC DataSource [" + this.jdbcDataSource + "].");
			}
			Connection con = this.jdbcDataSource.getConnection();
			try {
				return JasperFillManager.fillReport(this.report, model, con);
			}
			finally {
				try {
					con.close();
				}
				catch (SQLException ex) {
					logger.warn("Could not close JDBC Connection", ex);
				}
			}
		}
	}

	/**
	 * Populates the headers in the <code>HttpServletResponse.</code> with the
	 * headers supplied by the user.
	 */
	private void populateHeaders(HttpServletResponse response) {
		// Apply the headers to the response.
		for (Enumeration en = this.headers.propertyNames(); en.hasMoreElements();) {
			String key = (String) en.nextElement();
			response.addHeader(key, this.headers.getProperty(key));
		}
	}

	/**
	 * Find an instance of <code>JRDataSource</code> in the given model map or create an
	 * appropriate JRDataSource for passed-in report data.
	 * <p>The default implementation checks for a model object under the
	 * specified "reportDataKey" first, then falls back to looking for a value
	 * of type <code>JRDataSource</code>, <code>java.util.Collection</code>,
	 * object array (in that order).
	 * @param model the model map, as passed in for view rendering
	 * @return the <code>JRDataSource</code> or <code>null</code> if the data source is not found
	 * @see #setReportDataKey
	 * @see #convertReportData
	 * @see #getReportDataTypes
	 */
	protected JRDataSource getReportData(Map model) {
		// Try model attribute with specified name.
		if (this.reportDataKey != null) {
			Object value = model.get(this.reportDataKey);
			return convertReportData(value);
		}

		// Try to find matching attribute, of given prioritized types.
		Object value = CollectionUtils.findValueOfType(model.values(), getReportDataTypes());

		if (value != null) {
			return convertReportData(value);
		}

		return null;
	}

	/**
	 * Convert the given report data value to a <code>JRDataSource</code>.
	 * <p>The default implementation delegates to <code>JasperReportUtils</code> unless
	 * the report data value is an instance of <code>JRDataSourceProvider</code>.
	 * A <code>JRDataSource</code>, <code>JRDataSourceProvider</code>,
	 * <code>java.util.Collection</code> or object array is detected.
	 * <code>JRDataSource</code>s are returned as is, whilst <code>JRDataSourceProvider</code>s
	 * are used to create an instance of <code>JRDataSource</code> which is then returned.
	 * The latter two are converted to <code>JRBeanCollectionDataSource</code> or
	 * <code>JRBeanArrayDataSource</code>, respectively.
	 * @param value the report data value to convert
	 * @return the JRDataSource
	 * @throws IllegalArgumentException if the value could not be converted
	 * @see JasperReportsUtils#convertReportData
	 * @see net.sf.jasperreports.engine.JRDataSource
	 * @see net.sf.jasperreports.engine.JRDataSourceProvider
	 * @see net.sf.jasperreports.engine.data.JRBeanCollectionDataSource
	 * @see net.sf.jasperreports.engine.data.JRBeanArrayDataSource
	 */
	protected JRDataSource convertReportData(Object value) throws IllegalArgumentException {
		if (value instanceof JRDataSourceProvider) {
			try {
				return ((JRDataSourceProvider) value).create(report);
			}
			catch (JRException ex) {
				throw new IllegalArgumentException("Supplied JRDataSourceProvider is invalid: " + ex);
			}
		}
		else {
			return JasperReportsUtils.convertReportData(value);
		}
	}

	/**
	 * Return the value types that can be converted to a <code>JRDataSource</code>,
	 * in prioritized order. Should only return types that the
	 * <code>convertReportData</code> method is actually able to convert.
	 * <p>Default value types are: <code>JRDataSource</code>,
	 * <code>JRDataSourceProvider</code> <code>java.util.Collection</code>
	 * and <code>Object</code> array.
	 * @return the value types in prioritized order
	 * @see #convertReportData
	 */
	protected Class[] getReportDataTypes() {
		return new Class[] {JRDataSource.class, JRDataSourceProvider.class, Collection.class, Object[].class};
	}

	/**
	 * Allows sub-classes to get access to the <code>JasperReport</code> instance
	 * loaded by Spring.
	 * @return an instance of <code>JasperReport</code>
	 */
	protected JasperReport getReport() {
		return this.report;
	}


	/**
	 * Subclasses should implement this method to perform the actual rendering process.
	 * @param populatedReport the populated <code>JasperPrint</code> to render
	 * @param parameters the map containing report parameters
	 * @param response the HTTP response the report should be rendered to
	 * @throws Exception if rendering failed
	 */
	protected abstract void renderReport(JasperPrint populatedReport, Map parameters, HttpServletResponse response)
			throws Exception;

}
