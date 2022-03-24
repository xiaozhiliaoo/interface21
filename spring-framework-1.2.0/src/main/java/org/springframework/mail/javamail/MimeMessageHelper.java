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

package org.springframework.mail.javamail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * Helper class for easy population of a <code>javax.mail.internet.MimeMessage</code>.
 *
 * <p>Mirrors the simple setters of SimpleMailMessage, directly applying the values
 * to the underlying MimeMessage. Allows to define a character encoding for the
 * entire message, automatically applied by all methods of this helper.
 *
 * <p>Also offers support for typical mail attachments, and for personal names
 * that accompany mail addresses. Note that advanced settings can still be applied
 * directly to the underlying MimeMessage object!
 *
 * <p>Typically used in MimeMessagePreparator implementations or JavaMailSender
 * client code: simply instantiating it as a MimeMessage wrapper, invoking
 * setters on the wrapper, using the underlying MimeMessage for mail sending.
 * Also used internally by JavaMailSenderImpl.
 *
 * <p>Sample code for an HTML mail with an inline image and a PDF attachment:
 *
 * <pre>
 * mailSender.send(new MimeMessagePreparator() {
 *   public void prepare(MimeMessage mimeMessage) throws MessagingException {
 *     MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
 *     message.setFrom("me@mail.com");
 *     message.setTo("you@mail.com");
 *     message.setSubject("my subject");
 *     message.setText("my text &lt;img src='cid:myLogo'&gt;", true);
 *     message.addInline("myLogo", new ClassPathResource("img/mylogo.gif"));
 *     message.addAttachment("myDocument.pdf", new ClassPathResource("doc/myDocument.pdf"));
 *   }
 * });</pre>
 *
 * Consider using MimeMailMessage (which implements the common MailMessage
 * interface, just like SimpleMailMessage) on top of a MimeMessageHelper,
 * to let message population code interact with a simple message or a MIME
 * message through a common interface.
 *
 * @author Juergen Hoeller
 * @since 19.01.2004
 * @see #getMimeMessage
 * @see MimeMessagePreparator
 * @see JavaMailSender
 * @see JavaMailSenderImpl
 * @see MimeMessage
 * @see org.springframework.mail.SimpleMailMessage
 * @see MimeMailMessage
 */
public class MimeMessageHelper {

	private static final String MULTIPART_SUBTYPE_RELATED = "related";

	private static final String MULTIPART_SUBTYPE_ALTERNATIVE = "alternative";

	private static final String CONTENT_TYPE_HTML = "text/html";

	private static final String CONTENT_TYPE_ALTERNATIVE = "text/alternative";

	private static final String CONTENT_TYPE_CHARSET_SUFFIX = ";charset=";

	private static final String HEADER_CONTENT_ID = "Content-ID";


	private final MimeMessage mimeMessage;

	private MimeMultipart mimeMultipart;

	private final String encoding;

	private FileTypeMap fileTypeMap;

	private boolean validateAddresses = false;


	/**
	 * Create a new MimeMessageHelper for the given MimeMessage,
	 * assuming a simple text message (no multipart content,
	 * i.e. no alternative texts and no inline elements or attachments).
	 * <p>The character encoding for the message will be taken from
	 * the passed-in MimeMessage object, if carried there. Else,
	 * JavaMail's default encoding will be used.
	 * @param mimeMessage MimeMessage to work on
	 * @see #MimeMessageHelper(MimeMessage, boolean)
	 * @see #getDefaultEncoding(MimeMessage)
	 * @see JavaMailSenderImpl#setDefaultEncoding
	 */
	public MimeMessageHelper(MimeMessage mimeMessage) {
		this(mimeMessage, null);
	}

	/**
	 * Create a new MimeMessageHelper for the given MimeMessage,
	 * assuming a simple text message (no multipart content,
	 * i.e. no alternative texts and no inline elements or attachments).
	 * @param mimeMessage MimeMessage to work on
	 * @param encoding the character encoding to use for the message
	 * @see #MimeMessageHelper(MimeMessage, boolean)
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, String encoding) {
		this.mimeMessage = mimeMessage;
		this.encoding = (encoding != null ? encoding : getDefaultEncoding(mimeMessage));
		this.fileTypeMap = getDefaultFileTypeMap(mimeMessage);
	}

	/**
	 * Create a new MimeMessageHelper for the given MimeMessage,
	 * in multipart mode (supporting alternative texts, inline
	 * elements and attachments) if requested.
	 * <p>The character encoding for the message will be taken from
	 * the passed-in MimeMessage object, if carried there. Else,
	 * JavaMail's default encoding will be used.
	 * @param mimeMessage MimeMessage to work on
	 * @param multipart whether to create a multipart message that
	 * supports alternative texts, inline elements and attachments
	 * @see #getDefaultEncoding(MimeMessage)
	 * @see JavaMailSenderImpl#setDefaultEncoding
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
		this(mimeMessage, multipart, null);
	}

	/**
	 * Create a new MimeMessageHelper for the given MimeMessage,
	 * in multipart mode (supporting alternative texts, inline
	 * elements and attachments) if requested.
	 * @param mimeMessage MimeMessage to work on
	 * @param multipart whether to create a multipart message that
	 * supports alternative texts, inline elements and attachments
	 * @param encoding the character encoding to use for the message
	 */
	public MimeMessageHelper(MimeMessage mimeMessage, boolean multipart, String encoding)
	    throws MessagingException {

		this.mimeMessage = mimeMessage;
		if (multipart) {
			this.mimeMultipart = new MimeMultipart(MULTIPART_SUBTYPE_RELATED);
			this.mimeMessage.setContent(this.mimeMultipart);
		}
		this.encoding = (encoding != null ? encoding : getDefaultEncoding(mimeMessage));
		this.fileTypeMap = getDefaultFileTypeMap(mimeMessage);
	}


	/**
	 * Return the underlying MimeMessage object.
	 */
	public final MimeMessage getMimeMessage() {
		return mimeMessage;
	}

	/**
	 * Return whether this helper is in multipart mode,
	 * i.e. whether it holds a multipart message.
	 * @see #MimeMessageHelper(MimeMessage, boolean)
	 */
	public final boolean isMultipart() {
		return (this.mimeMultipart != null);
	}

	/**
	 * Return the underlying MIME multipart object, if any.
	 * Cam be used to manually add body parts etc.
	 * @throws IllegalStateException if this helper is not in multipart mode
	 * @see #isMultipart
	 * @see MimeMultipart#addBodyPart
	 */
	public final MimeMultipart getMimeMultipart() throws IllegalStateException {
		if (this.mimeMultipart == null) {
			throw new IllegalStateException("Not in multipart mode - " +
			    "create an appropriate MimeMessageHelper via a constructor that takes a 'multipart' flag " +
			    "if you need to set alternative texts or add inline elements or attachments.");
		}
		return this.mimeMultipart;
	}

	/**
	 * Determine the default encoding for the given MimeMessage.
	 * @param mimeMessage the passed-in MimeMessage
	 * @return the default encoding associated with the MimeMessage,
	 * or null if none found
	 */
	protected String getDefaultEncoding(MimeMessage mimeMessage) {
		if (mimeMessage instanceof SmartMimeMessage) {
			return ((SmartMimeMessage) mimeMessage).getDefaultEncoding();
		}
		return null;
	}

	/**
	 * Return the specific character encoding used for this message, if any.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Determine the default Java Activation FileTypeMap for the given MimeMessage.
	 * @param mimeMessage the passed-in MimeMessage
	 * @return the default FileTypeMap associated with the MimeMessage,
	 * or a default ConfigurableMimeFileTypeMap if none found for the message
	 * @see ConfigurableMimeFileTypeMap
	 */
	protected FileTypeMap getDefaultFileTypeMap(MimeMessage mimeMessage) {
		if (mimeMessage instanceof SmartMimeMessage) {
			FileTypeMap fileTypeMap = ((SmartMimeMessage) mimeMessage).getDefaultFileTypeMap();
			if (fileTypeMap != null) {
				return fileTypeMap;
			}
		}
		ConfigurableMimeFileTypeMap fileTypeMap = new ConfigurableMimeFileTypeMap();
		fileTypeMap.afterPropertiesSet();
		return fileTypeMap;
	}

	/**
	 * Set the Java Activation Framework <code>FileTypeMap</code> to use
	 * for determining the content type of inline content and attachments
	 * that get added to the message.
	 * <p>Default is the <code>FileTypeMap</code> that the underlying
	 * MimeMessage carries, if any, or the Activation Framework's default
	 * <code>FileTypeMap</code> instance else.
	 * @see #addInline
	 * @see #addAttachment
	 * @see #getDefaultFileTypeMap(MimeMessage)
	 * @see JavaMailSenderImpl#setDefaultFileTypeMap
	 * @see FileTypeMap#getDefaultFileTypeMap
	 * @see ConfigurableMimeFileTypeMap
	 */
	public void setFileTypeMap(FileTypeMap fileTypeMap) {
		this.fileTypeMap = (fileTypeMap != null ? fileTypeMap : getDefaultFileTypeMap(getMimeMessage()));
	}

	/**
	 * Return the <code>FileTypeMap</code> used by this MimeMessageHelper.
	 */
	public FileTypeMap getFileTypeMap() {
		return fileTypeMap;
	}


	/**
	 * Set whether to validate all addresses which get passed to this helper.
	 * Default is false.
	 * <p>Note that this is by default just available for JavaMail >= 1.3.
	 * You can override the default <code>validateAddress method</code> for
	 * validation on older JavaMail versions (or for custom validation).
	 * @see #validateAddress
	 */
	public void setValidateAddresses(boolean validateAddresses) {
		this.validateAddresses = validateAddresses;
	}

	/**
	 * Return whether this helper will validate all addresses passed to it.
	 */
	public boolean isValidateAddresses() {
		return validateAddresses;
	}

	/**
	 * Validate the given mail address.
	 * Called by all of MimeMessageHelper's address setters and adders.
	 * <p>Default implementation invokes <code>InternetAddress.validate()</code>,
	 * provided that address validation is activated for the helper instance.
	 * <p>Note that this method will just work on JavaMail >= 1.3. You can override
	 * it for validation on older JavaMail versions or for custom validation.
	 * @param address the address to validate
	 * @throws AddressException if validation failed
	 * @see #isValidateAddresses()
	 * @see InternetAddress#validate()
	 */
	protected void validateAddress(InternetAddress address) throws AddressException {
		if (isValidateAddresses()) {
			address.validate();
		}
	}

	/**
	 * Validate all given mail addresses.
	 * Default implementation simply delegates to validateAddress for each address.
	 * @param addresses the addresses to validate
	 * @throws AddressException if validation failed
	 * @see #validateAddress(InternetAddress)
	 */
	protected void validateAddresses(InternetAddress[] addresses) throws AddressException {
		for (int i = 0; i < addresses.length; i++) {
			validateAddress(addresses[i]);
		}
	}


	public void setFrom(InternetAddress from) throws MessagingException {
		Assert.notNull(from, "From address must not be null");
		validateAddress(from);
		this.mimeMessage.setFrom(from);
	}

	public void setFrom(String from) throws MessagingException {
		Assert.notNull(from, "From address must not be null");
		setFrom(new InternetAddress(from));
	}

	public void setFrom(String from, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(from, "From address must not be null");
		setFrom(getEncoding() != null ?
		    new InternetAddress(from, personal, getEncoding()) : new InternetAddress(from, personal));
	}

	public void setReplyTo(InternetAddress replyTo) throws MessagingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		validateAddress(replyTo);
		this.mimeMessage.setReplyTo(new InternetAddress[] {replyTo});
	}

	public void setReplyTo(String replyTo) throws MessagingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		setReplyTo(new InternetAddress(replyTo));
	}

	public void setReplyTo(String replyTo, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(replyTo, "Reply-to address must not be null");
		InternetAddress replyToAddress = (getEncoding() != null) ?
				new InternetAddress(replyTo, personal, getEncoding()) : new InternetAddress(replyTo, personal);
		setReplyTo(replyToAddress);
	}


	public void setTo(InternetAddress to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		validateAddress(to);
		this.mimeMessage.setRecipient(Message.RecipientType.TO, to);
	}

	public void setTo(InternetAddress[] to) throws MessagingException {
		Assert.notNull(to, "To address array must not be null");
		validateAddresses(to);
		this.mimeMessage.setRecipients(Message.RecipientType.TO, to);
	}

	public void setTo(String to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		setTo(new InternetAddress(to));
	}

	public void setTo(String[] to) throws MessagingException {
		Assert.notNull(to, "To address array must not be null");
		InternetAddress[] addresses = new InternetAddress[to.length];
		for (int i = 0; i < to.length; i++) {
			addresses[i] = new InternetAddress(to[i]);
		}
		setTo(addresses);
	}

	public void addTo(InternetAddress to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		validateAddress(to);
		this.mimeMessage.addRecipient(Message.RecipientType.TO, to);
	}

	public void addTo(String to) throws MessagingException {
		Assert.notNull(to, "To address must not be null");
		addTo(new InternetAddress(to));
	}

	public void addTo(String to, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(to, "To address must not be null");
		addTo(getEncoding() != null ?
		    new InternetAddress(to, personal, getEncoding()) :
		    new InternetAddress(to, personal));
	}


	public void setCc(InternetAddress cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		validateAddress(cc);
		this.mimeMessage.setRecipient(Message.RecipientType.CC, cc);
	}

	public void setCc(InternetAddress[] cc) throws MessagingException {
		Assert.notNull(cc, "Cc address array must not be null");
		validateAddresses(cc);
		this.mimeMessage.setRecipients(Message.RecipientType.CC, cc);
	}

	public void setCc(String cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		setCc(new InternetAddress(cc));
	}

	public void setCc(String[] cc) throws MessagingException {
		Assert.notNull(cc, "Cc address array must not be null");
		InternetAddress[] addresses = new InternetAddress[cc.length];
		for (int i = 0; i < cc.length; i++) {
			addresses[i] = new InternetAddress(cc[i]);
		}
		setCc(addresses);
	}

	public void addCc(InternetAddress cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		validateAddress(cc);
		this.mimeMessage.addRecipient(Message.RecipientType.CC, cc);
	}

	public void addCc(String cc) throws MessagingException {
		Assert.notNull(cc, "Cc address must not be null");
		addCc(new InternetAddress(cc));
	}

	public void addCc(String cc, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(cc, "Cc address must not be null");
		addCc(getEncoding() != null ?
		    new InternetAddress(cc, personal, getEncoding()) :
		    new InternetAddress(cc, personal));
	}


	public void setBcc(InternetAddress bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		validateAddress(bcc);
		this.mimeMessage.setRecipient(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(InternetAddress[] bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address array must not be null");
		validateAddresses(bcc);
		this.mimeMessage.setRecipients(Message.RecipientType.BCC, bcc);
	}

	public void setBcc(String bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		setBcc(new InternetAddress(bcc));
	}

	public void setBcc(String[] bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address array must not be null");
		InternetAddress[] addresses = new InternetAddress[bcc.length];
		for (int i = 0; i < bcc.length; i++) {
			addresses[i] = new InternetAddress(bcc[i]);
		}
		setBcc(addresses);
	}

	public void addBcc(InternetAddress bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		validateAddress(bcc);
		this.mimeMessage.addRecipient(Message.RecipientType.BCC, bcc);
	}

	public void addBcc(String bcc) throws MessagingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		addBcc(new InternetAddress(bcc));
	}

	public void addBcc(String bcc, String personal) throws MessagingException, UnsupportedEncodingException {
		Assert.notNull(bcc, "Bcc address must not be null");
		addBcc(getEncoding() != null ?
		    new InternetAddress(bcc, personal, getEncoding()) :
		    new InternetAddress(bcc, personal));
	}


	public void setSentDate(Date sentDate) throws MessagingException {
		Assert.notNull(sentDate, "Sent date must not be null");
		this.mimeMessage.setSentDate(sentDate);
	}

	public void setSubject(String subject) throws MessagingException {
		Assert.notNull(subject, "Subject must not be null");
		if (getEncoding() != null) {
			this.mimeMessage.setSubject(subject, getEncoding());
		}
		else {
			this.mimeMessage.setSubject(subject);
		}
	}


	/**
	 * Set the given text directly as content in non-multipart mode
	 * or as default body part in multipart mode.
	 * Always applies the default content type "text/plain".
	 * <p><b>NOTE:</b> Invoke addInline <i>after</i> setText; else, mail
	 * readers might not be able to resolve inline references correctly.
	 * @param text the text for the message
	 * @throws MessagingException in case of errors
	 * @see #addInline
	 */
	public void setText(String text) throws MessagingException {
		setText(text, false);
	}

	/**
	 * Set the given text directly as content in non-multipart mode
	 * or as default body part in multipart mode.
	 * The "html" flag determines the content type to apply.
	 * <p><b>NOTE:</b> Invoke addInline <i>after</i> setText; else, mail
	 * readers might not be able to resolve inline references correctly.
	 * @param text the text for the message
	 * @param html whether to apply content type "text/html" for an
	 * HTML mail, using default content type ("text/plain") else
	 * @throws MessagingException in case of errors
	 * @see #addInline
	 */
	public void setText(String text, boolean html) throws MessagingException {
		Assert.notNull(text, "Text must not be null");
		MimePart partToUse = null;
		if (this.mimeMultipart != null) {
			partToUse = getMainPart();
		}
		else {
			partToUse = this.mimeMessage;
		}
		if (html) {
			setHtmlTextToMimePart(partToUse, text);
		}
		else {
			setPlainTextToMimePart(partToUse, text);
		}
	}

	/**
	 * Set the given plain text and HTML text as alternatives, offering
	 * both options to the email client. Requires multipart mode.
	 * <p><b>NOTE:</b> Invoke addInline <i>after</i> setText; else, mail
	 * readers might not be able to resolve inline references correctly.
	 * @param plainText the plain text for the message
	 * @param htmlText the HTML text for the message	 * @throws MessagingException in case of errors
	 * @see #addInline
	 */
	public void setText(String plainText, String htmlText) throws MessagingException {
		Assert.notNull(plainText, "Plain text must not be null");
		Assert.notNull(htmlText, "HTML text must not be null");

		Multipart messageBody = new MimeMultipart(MULTIPART_SUBTYPE_ALTERNATIVE);
		MimeBodyPart mimeBodyPart;

		// create the plain text part of the message
		mimeBodyPart = new MimeBodyPart();
		setPlainTextToMimePart(mimeBodyPart, plainText);
		messageBody.addBodyPart(mimeBodyPart);

		// create the HTML text part of the message
		mimeBodyPart = new MimeBodyPart();
		setHtmlTextToMimePart(mimeBodyPart, htmlText);
		messageBody.addBodyPart(mimeBodyPart);

		getMainPart().setContent(messageBody, CONTENT_TYPE_ALTERNATIVE);
	}

	private MimeBodyPart getMainPart() throws MessagingException {
		MimeMultipart mimeMultipart = getMimeMultipart();
		MimeBodyPart bodyPart = null;
		for (int i = 0; i < mimeMultipart.getCount(); i++) {
			BodyPart bp = mimeMultipart.getBodyPart(i);
			if (bp.getFileName() == null) {
				bodyPart = (MimeBodyPart) bp;
			}
		}
		if (bodyPart == null) {
			MimeBodyPart mimeBodyPart = new MimeBodyPart();
			mimeMultipart.addBodyPart(mimeBodyPart);
			bodyPart = mimeBodyPart;
		}
		return bodyPart;
	}

	private void setPlainTextToMimePart(MimePart mimePart, String text)
	    throws MessagingException {
		if (getEncoding() != null) {
			mimePart.setText(text, getEncoding());
		}
		else {
			mimePart.setText(text);
		}
	}

	private void setHtmlTextToMimePart(MimePart mimePart, String text)
	    throws MessagingException {
		if (getEncoding() != null) {
			mimePart.setContent(text, CONTENT_TYPE_HTML + CONTENT_TYPE_CHARSET_SUFFIX + getEncoding());
		}
		else {
			mimePart.setContent(text, CONTENT_TYPE_HTML);
		}
	}


	/**
	 * Add an inline element to the MimeMessage, taking the content from a
	 * <code>javax.activation.DataSource</code>.
	 * <p>Note that the InputStream returned by the DataSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * <code>getInputStream()</code> multiple times.
	 * <p><b>NOTE:</b> Invoke <code>addInline</code> <i>after</i> <code>setText</code>;
	 * else, mail readers might not be able to resolve inline references correctly.
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param dataSource the <code>javax.activation.DataSource</code> to take
	 * the content from, determining the InputStream and the content type
	 * @throws MessagingException in case of errors
	 * @see #setText
	 * @see #addInline(String, File)
	 * @see #addInline(String, Resource)
	 */
	public void addInline(String contentId, DataSource dataSource) throws MessagingException {
		Assert.notNull(contentId, "Content ID must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		// We're using setHeader here to stay compatible with JavaMail 1.2,
		// rather than JavaMail 1.3's setContentID.
		mimeBodyPart.setHeader(HEADER_CONTENT_ID, "<" + contentId + ">");
		mimeBodyPart.setDisposition(MimeBodyPart.INLINE);
		getMimeMultipart().addBodyPart(mimeBodyPart);
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from a
	 * <code>java.io.File</code>.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * <p><b>NOTE:</b> Invoke <code>addInline</code> <i>after</i> <code>setText</code>;
	 * else, mail readers might not be able to resolve inline references correctly.
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param file the File resource to take the content from
	 * @throws MessagingException in case of errors
	 * @see #setText
	 * @see #addInline(String, Resource)
	 * @see #addInline(String, DataSource)
	 */
	public void addInline(String contentId, File file) throws MessagingException {
		Assert.notNull(file, "File must not be null");
		FileDataSource dataSource = new FileDataSource(file);
		dataSource.setFileTypeMap(getFileTypeMap());
		addInline(contentId, dataSource);
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.io.InputStreamResource</code>.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * <p>Note that the InputStream returned by the Resource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * <code>getInputStream()</code> multiple times.
	 * <p><b>NOTE:</b> Invoke <code>addInline</code> <i>after</i> <code>setText</code>;
	 * else, mail readers might not be able to resolve inline references correctly.
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param resource the resource to take the content from
	 * @throws MessagingException in case of errors
	 * @see #setText
	 * @see #addInline(String, File)
	 * @see #addInline(String, DataSource)
	 */
	public void addInline(String contentId, Resource resource) throws MessagingException {
		Assert.notNull(resource, "Resource must not be null");
		String contentType = getFileTypeMap().getContentType(resource.getFilename());
		addInline(contentId, resource, contentType);
	}

	/**
	 * Add an inline element to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.InputStreamResource</code>, and
	 * specifying the content type explicitly.
	 * <p>You can determine the content type for any given filename via a Java
	 * Activation Framework's FileTypeMap, for example the one held by this helper.
	 * <p>Note that the InputStream returned by the InputStreamSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * <code>getInputStream()</code> multiple times.
	 * <p><b>NOTE:</b> Invoke <code>addInline</code> <i>after</i> <code>setText</code>;
	 * else, mail readers might not be able to resolve inline references correctly.
	 * @param contentId the content ID to use. Will end up as "Content-ID" header
	 * in the body part, surrounded by angle brackets: e.g. "myId" -> "&lt;myId&gt;".
	 * Can be referenced in HTML source via src="cid:myId" expressions.
	 * @param inputStreamSource the resource to take the content from
	 * @param contentType the content type to use for the element
	 * @throws MessagingException in case of errors
	 * @see #setText
	 * @see #getFileTypeMap
	 * @see #addInline(String, Resource)
	 * @see #addInline(String, DataSource)
	 */
	public void addInline(String contentId, InputStreamSource inputStreamSource, String contentType)
	    throws MessagingException {
		Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
		if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
			throw new IllegalArgumentException(
					"Passed-in Resource contains an open stream: invalid argument. " +
					"JavaMail requires an InputStreamSource that creates a fresh stream for every call.");
		}
		DataSource dataSource = createDataSource(inputStreamSource, contentType, "inline");
		addInline(contentId, dataSource);
	}

	/**
	 * Add an attachment to the MimeMessage, taking the content from a
	 * <code>javax.activation.DataSource</code>.
	 * <p>Note that the InputStream returned by the DataSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * <code>getInputStream()</code> multiple times.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail (the content type will be determined by this)
	 * @param dataSource the <code>javax.activation.DataSource</code> to take
	 * the content from, determining the InputStream and the content type
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, InputStreamSource)
	 * @see #addAttachment(String, File)
	 */
	public void addAttachment(String attachmentFilename, DataSource dataSource) throws MessagingException {
		Assert.notNull(attachmentFilename, "Attachment filename must not be null");
		Assert.notNull(dataSource, "DataSource must not be null");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setFileName(attachmentFilename);
		mimeBodyPart.setDataHandler(new DataHandler(dataSource));
		getMimeMultipart().addBodyPart(mimeBodyPart);
	}

	/**
	 * Add an attachment to the MimeMessage, taking the content from a
	 * <code>java.io.File</code>.
	 * <p>The content type will be determined by the name of the given
	 * content file. Do not use this for temporary files with arbitrary
	 * filenames (possibly ending in ".tmp" or the like)!
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail
	 * @param file the File resource to take the content from
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, InputStreamSource)
	 * @see #addAttachment(String, DataSource)
	 */
	public void addAttachment(String attachmentFilename, File file) throws MessagingException {
		Assert.notNull(file, "File must not be null");
		FileDataSource dataSource = new FileDataSource(file);
		dataSource.setFileTypeMap(getFileTypeMap());
		addAttachment(attachmentFilename, dataSource);
	}

	/**
	 * Add an attachment to the MimeMessage, taking the content from an
	 * <code>org.springframework.core.io.InputStreamResource</code>.
	 * <p>The content type will be determined by the given filename for
	 * the attachment. Thus, any content source will be fine, including
	 * temporary files with arbitrary filenames.
	 * <p>Note that the InputStream returned by the InputStreamSource implementation
	 * needs to be a <i>fresh one on each call</i>, as JavaMail will invoke
	 * <code>getInputStream()</code> multiple times.
	 * @param attachmentFilename the name of the attachment as it will
	 * appear in the mail
	 * @param inputStreamSource the resource to take the content from
	 * (all of Spring's Resource implementations can be passed in here)
	 * @throws MessagingException in case of errors
	 * @see #addAttachment(String, File)
	 * @see #addAttachment(String, DataSource)
	 * @see Resource
	 */
	public void addAttachment(String attachmentFilename, InputStreamSource inputStreamSource)
	    throws MessagingException {

		Assert.notNull(inputStreamSource, "InputStreamSource must not be null");
		if (inputStreamSource instanceof Resource && ((Resource) inputStreamSource).isOpen()) {
			throw new IllegalArgumentException(
					"Passed-in Resource contains an open stream: invalid argument. " +
					"JavaMail requires an InputStreamSource that creates a fresh stream for every call.");
		}
		String contentType = getFileTypeMap().getContentType(attachmentFilename);
		DataSource dataSource = createDataSource(inputStreamSource, contentType, attachmentFilename);
		addAttachment(attachmentFilename, dataSource);
	}

	/**
	 * Create an Activation Framework DataSource for the given InputStreamSource.
	 * @param inputStreamSource the InputStreamSource (typically a Spring Resource)
	 * @param contentType the content type
	 * @param name the name of the DataSource
	 * @return the Activation Framework DataSource
	 */
	protected DataSource createDataSource(
	    final InputStreamSource inputStreamSource, final String contentType, final String name) {

		return new DataSource() {
			public InputStream getInputStream() throws IOException {
				return inputStreamSource.getInputStream();
			}
			public OutputStream getOutputStream() {
				throw new UnsupportedOperationException("Read-only javax.activation.DataSource");
			}
			public String getContentType() {
				return contentType;
			}
			public String getName() {
				return name;
			}
		};
	}

}
