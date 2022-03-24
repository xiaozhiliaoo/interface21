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

package org.springframework.web.servlet.view.document;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.web.servlet.view.AbstractView;
 
/**
 * Abstract superclass for PDF views, using Bruno Lowagie's
 * <a href="http://www.lowagie.com/iText">iText</a> package.
 * Application-specific view classes will extend this class.
 * The view will be held in the subclass itself, not in a template.
 *
 * <p>Note: Internet Explorer requires a ".pdf" extension, as
 * it doesn't always respect the declared content type.
 *
 * @author Rod Johnson
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 */
public abstract class AbstractPdfView extends AbstractView {
	
	private static final int OUTPUT_BYTE_ARRAY_INITIAL_SIZE = 4096;

	/**
	 * This constructor sets the appropriate content type "application/pdf".
	 * Note that IE won't take much notice of this, but there's not a lot we
	 * can do about this. Generated documents should have a ".pdf" extension.
	 */
	public AbstractPdfView() {
		setContentType("application/pdf");
	}
	
	protected final void renderMergedOutputModel(
			Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		// The following simple method doesn't work in IE, which
		// needs to know the content length.

		// PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
		// document.open();
		// buildPdfDocument(model, document, writer, request, response);
		// document.close();

		// See http://www.lowagie.com/iText/faq.html#msie
		// for an explanation of why we can't use the obvious form above.

		// IE workaround: write into byte array first.
		ByteArrayOutputStream baos = new ByteArrayOutputStream(OUTPUT_BYTE_ARRAY_INITIAL_SIZE);
		Document document = getDocument();
		PdfWriter writer = PdfWriter.getInstance(document, baos);

		// Apply preferences and build metadata.
		writer.setViewerPreferences(getViewerPreferences());
		buildPdfMetadata(model, document, request);

		// Build PDF document.
		document.open();
		buildPdfDocument(model, document, writer, request, response);
		document.close();

		// Write content type and also length (determined via byte array).
		response.setContentType(getContentType());
		response.setContentLength(baos.size());

		// Flush byte array to servlet output stream.
		ServletOutputStream out = response.getOutputStream();
		baos.writeTo(out);
		out.flush();
	}

	/**
	 * @deprecated in favor of newDocument
	 * @see #newDocument
	 */
	protected Document getDocument() {
		return newDocument();
	}

	/**
	 * Create a new document to hold the PDF contents.
	 * <p>By default returns an A4 document, but the subclass can specify any
	 * Document, possibly parameterized via bean properties defined on the View.
	 * @return the newly created iText Document
	 * @see Document#Document(com.lowagie.text.Rectangle)
	 */
	protected Document newDocument() {
		return new Document(PageSize.A4);
	}

	/**
	 * Return the viewer preferences for the PDF file.
	 * <p>By default returns <code>AllowPrinting</code> and
	 * <code>PageLayoutSinglePage</code>, but can be subclassed.
	 * The subclass can either have fixed preferences or retrieve
	 * them from bean properties defined on the View.
	 * @return an int containing the bits information against PdfWriter definitions
	 * @see PdfWriter#AllowPrinting
	 * @see PdfWriter#PageLayoutSinglePage
	 */
	protected int getViewerPreferences() {
		return PdfWriter.AllowPrinting | PdfWriter.PageLayoutSinglePage;
	}

	/**
	 * Populate the iText Document's meta fields (author, title, etc.).
	 * <br>Default is an empty implementation. Subclasses may override this method
	 * to add meta fields such as title, subject, author, creator, keywords, etc.
	 * This method is called after assigning a PdfWriter to the Document and
	 * before calling <code>document.open()</code>.
	 * @param model provides the model, in case meta information must be populated from it
	 * @param document the iText document being populated
	 * @param request in case we need locale etc. Shouldn't look at attributes.
	 * @see Document#addTitle
	 * @see Document#addSubject
	 * @see Document#addKeywords
	 * @see Document#addAuthor
	 * @see Document#addCreator
	 * @see Document#addProducer
	 * @see Document#addCreationDate
	 * @see Document#addHeader
	*/
	protected void buildPdfMetadata(Map model, Document document, HttpServletRequest request) {
	}

	/**
	 * Subclasses must implement this method to build an iText PDF document,
	 * given the model.
	 * @param model the model Map
	 * @param document the iText Document to use
	 * @param writer the PdfWriter to use
	 * @param request in case we need locale etc. Shouldn't look at attributes.
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 * @throws Exception any exception that occured during document building
	 */
	protected abstract void buildPdfDocument(
			Map model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception;

}
