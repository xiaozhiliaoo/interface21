package com.interface21.web.tags;

/**
 * Simple adapter to expose status of a field.
 * Set as a variable by the <bind> tag.
 * Intended for use by JSP STL expressions.
 * 
 * TEMP: TODO: also expose path? for form
 * user.x?
 * This could then handle multi bind resolution automatically
 */
public class BindStatus {
	
	private static final String NO_ERROR_MESSAGE = "";

	private final String errorMessage;

	private final boolean error;

	private final String errorCode;

	private final Object value;
	
	/** From BindTagHelper */
	private final int source;
	
	/** Such as user.age */
	private String expression;

	/**
	 * Constructor for BindStatus.
	 */
	protected BindStatus(Object value, String expression, int source) {
		this(value, NO_ERROR_MESSAGE, NO_ERROR_MESSAGE, expression, source);
	}

	/**
	 * @param source code from BindTagHelper
	 */
	protected BindStatus(Object value, String errorCode, String errorMessage, String expression, int source) {
		this.value = value;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
		this.error = !NO_ERROR_MESSAGE.equals(this.errorMessage);
		this.expression = expression;
		this.source = source;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * Gets the errorMessage. 
	 * @return Returns a String. The emptyt string if no error.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Gets the error.
	 * @return Returns a boolean
	 */
	public boolean getError() {
		return error;
	}

	/**
	 * Gets the errorCode.
	 * @return Returns a String
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	public boolean isBlank() {
		return this.source == BindTagHelper.BLANK;
	}
	
	public boolean isFromBean() {
		return this.source == BindTagHelper.FROM_BEAN;
	}
	
	public boolean isFromError() {
		return this.source == BindTagHelper.FROM_ERROR;
	}
	
	/** bind path e.g. user.age
	 * can use in form resubmission as input name
	 */
	public String getExpression() {
		return this.expression;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer("BindStatus: value=[" + value + "]");
		if (error)
			sb.append("; error code='" + errorCode + "'; error message='" + errorMessage + "'; ");
		sb.append("source=" + BindTagHelper.SRC_NAMES[this.source]);
		return sb.toString();
	}

}