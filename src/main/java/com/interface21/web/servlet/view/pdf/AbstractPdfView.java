package com.interface21.web.servlet.view.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.interface21.web.servlet.view.AbstractView;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
 


/**
 * Abstract superclass for PDF views.
 * Still seems to require PDF extension for the page to work in IE.
 */
public abstract class AbstractPdfView extends AbstractView {
	
	private int width = 600;
	
	private int	height = 750;
	
	//private Color

	/**
	 * Set the appropriate content type.
	 * Note that IE won't take much notice of this,
	 * but there's not a lot we can do about this.
	 * Generated documents should have a pdf extension.
 	*/
	public AbstractPdfView() {
		setContentType("application/pdf");
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}


	/**
	 * @see AbstractView#renderMergedOutputModel(Map, HttpServletRequest, HttpServletResponse)
	 */
	protected final void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		/*
			See	http://www.lowagie.com/iText/faq.html#msie
			for an explanation of why we can't use the obvious form above.
			IE needs to know the content length.
		
		*/
		
		
		// Bean properties
		//Rectangle pageSize = new Rectangle(this.width, this.height);
		//pageSize.setBackgroundColor(new java.awt.Color(0xFF, 0xFF, 0xEE));

		// HOW TO SET THROUGH BEAN PROPERTY!?
		Document document = new Document(PageSize.A4);		

		try {
			// PdfWriter.getInstance(document, response.getOutputStream());
			//document.open();

			//doPdfDocument(model, document);

			//document.close();
			
			// Doesn't work
			
			
			// IE workaround
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PdfWriter writer = PdfWriter.getInstance(document, baos);
			
			writer.setViewerPreferences(PdfWriter.AllowPrinting | PdfWriter.PageLayoutSinglePage);
			
			document.open();

			buildPdfDocument(model, document, request, response);

			document.close();

			response.setContentLength(baos.size());
			ServletOutputStream out = response.getOutputStream();
			baos.writeTo(out);
			out.flush();

		}
		catch (DocumentException ex) {
			throw new ServletException("Error creating PDF document", ex);
		}
	}


	/**
	 * @param request in case we need locale etc. Shouldn't look at attributes
	 * @param response in case we need to set cookies. Shouldn't write to it.
	 */
	protected abstract void buildPdfDocument(Map model, Document pdfDoc, HttpServletRequest request, HttpServletResponse response) throws DocumentException;

	public static void main(String[] args) {
		System.out.println("Chapter 1 example 2: PageSize");

		// step 1: creation of a document-object
		Rectangle pageSize = new Rectangle(144, 720);
		pageSize.setBackgroundColor(new java.awt.Color(0xFF, 0xFF, 0xDE));
		Document document = new Document(pageSize);

		try {

			// step 2:
			// we create a writer that listens to the document
			// and directs a PDF-stream to a file

			PdfWriter.getInstance(document, new FileOutputStream("d:\\pdfTest.pdf"));

			// step 3: we open the document
			document.open();

			// step 4: we add some paragraphs to the document
			for (int i = 0; i < 5; i++) {
				document.add(new Paragraph("Hello World"));
			}

		}
		catch (DocumentException de) {
			System.err.println(de.getMessage());
		}
		catch (IOException ioe) {
			System.err.println(ioe.getMessage());
		}

		// step 5: we close the document
		document.close();

	}
}