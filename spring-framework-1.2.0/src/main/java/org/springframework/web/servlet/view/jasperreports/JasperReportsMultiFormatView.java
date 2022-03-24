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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JasperPrint;

import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;

/**
 * Jasper Reports view class that allows for the actual rendering format to be
 * specified at runtime using a parameter contained in the model.
 *
 * <p>This view works on the concept of a format key and a mapping key.
 * The format key is used to pass the mapping key from your
 * <code>Controller</code> to Spring through as part of the model and the
 * mapping key is used to map a logical format to an actual JasperReports
 * view class. For example you might add the following code to your
 * <code>Controller</code>:
 *
 * <pre>
 * Map model = new HashMap();
 * model.put("format", "pdf");</pre>
 *
 * Here <code>format</code> is the format key and <code>pdf</code> is
 * the mapping key. When rendering a report, this class looks for a
 * model parameter under the format key, which by default is
 * <code>format</code>. It then uses the value of this parameter to lookup
 * the actual <code>View</code> class to use. The default mappings for this
 * lookup are:
 *
 * <p><ul>
 * <li><code>csv</code> - <code>JasperReportsCsvView</code></li>
 * <li><code>html</code> - <code>JasperReportsHtmlView</code></li>
 * <li><code>pdf</code> - <code>JasperReportsPdfView</code></li>
 * <li><code>xls</code> - <code>JasperReportsXlsView</code></li>
 * </ul>
 *
 * <p>The format key can be changed using the <code>formatKey</code>
 * property and the mapping key to view class mappings can be changed using the
 * <code>formatMappings</code> property.
 *
 * @author Rob Harrop
 * @since 1.1.5
 * @see #setFormatKey(String)
 * @see #setFormatMappings(Properties)
 */
public class JasperReportsMultiFormatView extends AbstractJasperReportsView {

	/**
	 * Default value used for format key.
	 */
	public static final String DEFAULT_FORMAT_KEY = "format";


	/**
	 * The key of the model parameter that holds the format key.
	 */
	private String formatKey = DEFAULT_FORMAT_KEY;

	/**
	 * Stores the format mappings, with the format discriminator
	 * as key and the corresponding view class as value.
	 */
	private Map formatMappings;

	/**
	 * Stores the mappings of mapping keys to Content-Disposition header values.
	 */
	private Properties contentDispositionMappings;


  /**
	 * Creates a new <code>JasperReportsMultiFormatView</code> instance
	 * with a default set of mappings.
	 */
	public JasperReportsMultiFormatView() {
		this.formatMappings = new HashMap(4);
		this.formatMappings.put("csv", JasperReportsCsvView.class);
		this.formatMappings.put("html", JasperReportsHtmlView.class);
		this.formatMappings.put("pdf", JasperReportsPdfView.class);
		this.formatMappings.put("xls", JasperReportsXlsView.class);
	}

	/**
	 * Set the key of the model parameter that holds the format discriminator.
	 * Default is "format".
	 */
	public void setFormatKey(String formatKey) {
		this.formatKey = formatKey;
	}

	/**
	 * Set the mappings of format discriminators to view class names.
	 * The default mappings are:
	 * <p><ul>
	 * <li><code>csv</code> - <code>JasperReportsCsvView</code></li>
	 * <li><code>html</code> - <code>JasperReportsHtmlView</code></li>
	 * <li><code>pdf</code> - <code>JasperReportsPdfView</code></li>
	 * <li><code>xls</code> - <code>JasperReportsXlsView</code></li>
	 * </ul>
	 */
	public void setFormatMappings(Properties mappingsWithClassNames) {
		if (mappingsWithClassNames == null || mappingsWithClassNames.isEmpty()) {
			throw new IllegalArgumentException("formatMappings must not be empty");
		}

		this.formatMappings = new HashMap(mappingsWithClassNames.size());
		for (Enumeration discriminators = mappingsWithClassNames.propertyNames(); discriminators.hasMoreElements();) {
			String discriminator = (String) discriminators.nextElement();
			String className = mappingsWithClassNames.getProperty(discriminator);
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("Mapped view class [" + className + "] to mapping key [" + discriminator + "]");
				}
				this.formatMappings.put(discriminator, ClassUtils.forName(className));
			}
			catch (ClassNotFoundException ex) {
				throw new ApplicationContextException(
						"Class [" + className + "] mapped to format [" + discriminator + "] cannot be found", ex);
			}
		}
	}

	/**
	 * Set the mappings of <code>Content-Disposition</code> header values to
	 * mapping keys. If specified, Spring will look at these mappings to determine
	 * the value of the <code>Content-Disposition</code> header for a given
	 * format mapping.
	 */
	public void setContentDispositionMappings(Properties mappings) {
		this.contentDispositionMappings = mappings;
	}

	/**
	 * Gets the mappings of <code>Content-Disposition</code> header values to
	 * mapping keys.
	 */
	public Properties getContentDispositionMappings() {
		// NOTE: Added to support configuration through the properties file mechanism.
		return contentDispositionMappings;
	}


	/**
	 * Locates the format key in the model using the configured discriminator key and uses this
	 * key to lookup the appropriate view class from the mappings. The rendering of the
	 * report is then delegated to an instance of that view class.
	 */
	protected void renderReport(JasperPrint populatedReport, Map model, HttpServletResponse response)
			throws Exception {

		String format = (String) model.get(this.formatKey);
		if (format == null) {
			throw new IllegalArgumentException("No format format found in model");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Rendering report using format mapping key [" + format + "]");
		}

		Class viewClass = (Class) this.formatMappings.get(format);
		if (viewClass == null) {
			throw new IllegalArgumentException("Format discriminator [" + format + "] is not a configured mapping");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Rendering report using view class [" + viewClass.getName() + "]");
		}

		AbstractJasperReportsView view = (AbstractJasperReportsView) BeanUtils.instantiateClass(viewClass);

		// Copy appropriate properties across.
		view.setExporterParameters(getExporterParameters());

		// Can skip most initialization since all relevant
		// URL processing has been done - just need to convert
		// parameters on the sub view..
    view.convertExporterParameters();

		response.setContentType(view.getContentType());

		populateContentDispositionIfNecessary(format, response);

		view.renderReport(populatedReport, model, response);
	}

	/**
	 * Adds/overwrites the <code>Content-Disposition</code> header value with the format-specific
	 * value if the mappings have been specified and a valid one exists for the given format.
	 * @param format the format key of the mapping
	 * @param response the <code>HttpServletResponse</code> to set the header in
	 * @see #setContentDispositionMappings
	 */
	private void populateContentDispositionIfNecessary(String format, HttpServletResponse response) {
		if (this.contentDispositionMappings != null) {
			String header = this.contentDispositionMappings.getProperty(format);
			if (header != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Setting Content-Disposition header to: [" + header + "]");
				}
				response.setHeader(HEADER_CONTENT_DISPOSITION, header);
			}
		}
	}

}
