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

package org.springframework.mail;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Encapsulates properties of a simple mail such as from, to, cc,
 * subject, text. To be sent with a MailSender implementation.
 *
 * <p>Consider JavaMailSender and JavaMail MimeMessages for creating
 * more sophisticated messages, for example with attachments, special
 * character encodings, or personal names that accompany mail addresses.
 *
 * <p>This simple message class implements the MailMessage interface,
 * to let message population code interact with a simple message or a
 * MIME message through a common interface.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 10.09.2003
 * @see MailSender
 * @see org.springframework.mail.javamail.JavaMailSender
 * @see org.springframework.mail.javamail.MimeMessagePreparator
 * @see org.springframework.mail.javamail.MimeMessageHelper
 * @see org.springframework.mail.javamail.MimeMailMessage
 */
public class SimpleMailMessage implements MailMessage, Serializable {

	private String from;

	private String replyTo;

	private String[] to;

	private String[] cc;

	private String[] bcc;

	private Date sentDate;

	private String subject;

	private String text;


	/**
	 * Create a new SimpleMailMessage.
	 */
	public SimpleMailMessage() {
	}

	/**
	 * Copy constructor.
	 */
	public SimpleMailMessage(SimpleMailMessage original) {
		this.from = original.getFrom();
		this.replyTo = original.getReplyTo();
		if (original.getTo() != null) {
			this.to = new String[original.getTo().length];
			System.arraycopy(original.getTo(), 0, this.to, 0, original.getTo().length);
		}
		if (original.getCc() != null) {
			this.cc = new String[original.getCc().length];
			System.arraycopy(original.getCc(), 0, this.cc, 0, original.getCc().length);
		}
		if (original.getBcc() != null) {
			this.bcc = new String[original.getBcc().length];
			System.arraycopy(original.getBcc(), 0, this.bcc, 0, original.getBcc().length);
		}
		this.sentDate = original.getSentDate();
		this.subject = original.getSubject();
		this.text = original.getText();
	}


	public void setFrom(String from) {
		this.from = from;
	}

	public String getFrom() {
		return this.from;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setTo(String to) {
		this.to = new String[] {to};
	}

	public void setTo(String[] to) {
		this.to = to;
	}

	public String[] getTo() {
		return this.to;
	}

	public void setCc(String cc) {
		this.cc = new String[] {cc};
	}

	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String[] getCc() {
		return cc;
	}

	public void setBcc(String bcc) {
		this.bcc = new String[] {bcc};
	}

	public void setBcc(String[] bcc) {
		this.bcc = bcc;
	}

	public String[] getBcc() {
		return bcc;
	}

	public void setSentDate(Date sentDate) {
		this.sentDate = sentDate;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}


	/**
	 * Copy the contents of this message to the given target message.
	 * @param target the MailMessage to copy to
	 */
	public void copyTo(MailMessage target) {
		if (getFrom() != null) {
			target.setFrom(getFrom());
		}
		if (getReplyTo() != null) {
			target.setReplyTo(getReplyTo());
		}
		if (getTo() != null) {
			target.setTo(getTo());
		}
		if (getCc() != null) {
			target.setCc(getCc());
		}
		if (getBcc() != null) {
			target.setBcc(getBcc());
		}
		if (getSentDate() != null) {
			target.setSentDate(getSentDate());
		}
		if (getSubject() != null) {
			target.setSubject(getSubject());
		}
		if (getText() != null) {
			target.setText(getText());
		}
	}


	public String toString() {
		StringBuffer sb = new StringBuffer("SimpleMailMessage: ");
		sb.append("from=").append(this.from).append("; ");
		sb.append("replyTo=").append(this.replyTo).append("; ");
		sb.append("to=").append(StringUtils.arrayToCommaDelimitedString(this.to)).append("; ");
		sb.append("cc=").append(StringUtils.arrayToCommaDelimitedString(this.cc)).append("; ");
		sb.append("bcc=").append(StringUtils.arrayToCommaDelimitedString(this.bcc)).append("; ");
		sb.append("sentDate=").append(this.sentDate).append("; ");
		sb.append("subject=").append(this.subject).append("; ");
		sb.append("text=").append(this.text);
		return sb.toString();
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SimpleMailMessage)) {
			return false;
		}
		SimpleMailMessage otherMessage = (SimpleMailMessage) other;
		return (ObjectUtils.nullSafeEquals(this.from, otherMessage.from) &&
				ObjectUtils.nullSafeEquals(this.replyTo, otherMessage.replyTo) &&
				java.util.Arrays.equals(this.to, otherMessage.to) &&
				java.util.Arrays.equals(this.cc, otherMessage.cc) &&
				java.util.Arrays.equals(this.bcc, otherMessage.bcc) &&
				ObjectUtils.nullSafeEquals(this.sentDate, otherMessage.sentDate) &&
				ObjectUtils.nullSafeEquals(this.subject, otherMessage.subject) &&
				ObjectUtils.nullSafeEquals(this.text, otherMessage.text));
	}

	public int hashCode() {
		int hashCode = (this.from == null ? 0 : this.from.hashCode());
		hashCode = 29 * hashCode + (this.replyTo == null ? 0 : this.replyTo.hashCode());
		for (int i = 0; this.to != null && i < this.to.length; i++) {
			hashCode = 29 * hashCode + (this.to == null ? 0 : this.to[i].hashCode());
		}
		for (int i = 0; this.cc != null && i < this.cc.length; i++) {
			hashCode = 29 * hashCode + (this.cc == null ? 0 : this.cc[i].hashCode());
		}
		for (int i = 0; this.bcc != null && i < this.bcc.length; i++) {
			hashCode = 29 * hashCode + (this.bcc == null ? 0 : this.bcc[i].hashCode());
		}
		hashCode = 29 * hashCode + (this.sentDate == null ? 0 : this.sentDate.hashCode());
		hashCode = 29 * hashCode + (this.subject == null ? 0 : this.subject.hashCode());
		hashCode = 29 * hashCode + (this.text == null ? 0 : this.text.hashCode());
		return hashCode;
	}

}
