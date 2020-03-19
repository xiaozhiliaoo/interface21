package com.wrox.expertj2ee.ticket.customer;

import com.interface21.validation.Errors;
import com.interface21.validation.FieldError;
import com.interface21.validation.Validator;

/**
 * Should be a bean
 */
public class DefaultUserValidator implements Validator {

	/** Minimum length for email */
	public static final int DEFAULT_MIN_EMAIL = 6;

	/** Maximum length for email */
	public static final int DEFAULT_MAX_EMAIL = 64;

	private int minEmail = DEFAULT_MIN_EMAIL;
	private int maxEmail = DEFAULT_MAX_EMAIL;

	/** Bean parameters */
	public void setMinEmail(int minEmail) {
		this.minEmail = minEmail;
	}

	public void setMaxEmail(int maxEmail) {
		this.maxEmail = maxEmail;
	}
	
	public boolean supports(Class clazz) {
		return clazz.equals(RegisteredUser.class);
	}

	/** This is the method that will be used */
	public void validate(Object o, Errors errs) {
		RegisteredUser u = (RegisteredUser) o;
		validateEmail(u.getEmail(), errs);
		checkPostCode(u.getBillingAddress().getPostcode(), errs);
	}

	private void validateEmail(String email, Errors errs) {
		if (email == null || "".equals(email)) {
			errs.rejectValue("email", "emailRequired", "E-mail Address is required");
			return;
		}

		int fioa = email.indexOf("@");
		int lioa = email.lastIndexOf("@");
		int liod = email.lastIndexOf(".");
		int len = email.length();
		if (len < minEmail
			|| len > maxEmail
			|| (fioa != lioa)
			|| (len - liod < 3)
			|| (lioa > liod)
			|| (fioa < 1)
			|| // not x@ - make sure there's at least 1 char before @ symbol
		 (email.charAt(fioa + 1) == '.')
			|| // not xx@.xxx - dot following straight after first @
		 (email.charAt(fioa - 1) == '.')
			|| // not x.@ - dot preceeding first @
		 (email.charAt(0) == '.') // first character isn't '.'
		) {
			errs.rejectValue("email", "emailInvalid", "E-mail Address is invalid");
			return;
		}

		//checkEmailIsUnique(email, ice);
	} // validateEmail

	private void checkPostCode(String postcode, Errors errs) {
		if (postcode == null || postcode.equals("")) {
			errs.rejectValue("billingAddress.postcode", "postcode.missing", "Postcode is required for billing address");
			return;
		}

		if (!postcode.startsWith("SE10"))
			errs.rejectValue("billingAddress.postcode", "postcode.notse", "Postcode must be in Greenwich!");

	}

}



//public class DefaultCustomerDetailsValidator implements CustomerDetailsValidator, Serializable {
//	
//	// WHERE DO ERROR CODE CONSTANTS LIVE!? NOT IN A PARTICULAR VALIDATOR!????
//	
//	/** Minimum length for password */
//	public static final int MIN_PASSWORD = 6;
//	
//	/** Maximum length for password */
//	public static final int MAX_PASSWORD = 100;
//	
//	
//	/** Minimum length for first name */
//	public static final int MIN_FNAME = 1;
//	
//	/** Maximum length for first name */
//	public static final int MAX_FNAME = 64;
//	
//	
//	/** Minimum length for last name */
//	public static final int MIN_LNAME = 1;
//	
//	/** Maximum length for last name */
//	public static final int MAX_LNAME = 64;
//	
//	
//	/** Minimum length for email */
//	public static final int MIN_EMAIL = 6;
//	
//	/** Maximum length for email */
//	public static final int MAX_EMAIL = 64;
//	
//	/** We'll use this to check an email is unique **/	
//	private CustomerManagerRemote customerManagerEJB;
//	
//	
//	/** 
//	 * Created on server with reference to the server
//	 */
//	public DefaultCustomerDetailsValidator(CustomerManagerRemote customerManagerEJB) {
//		this.customerManagerEJB = customerManagerEJB;
//	}
//	
//	
//	/**
//	 * Implementation of CustomerValidator
//	 */
//	public void validate(Customer cust) throws InvalidCommandException, RemoteException {
//		InvalidCommandException ice = new InvalidCommandException(cust);
//		
//		validateForename(cust.getForename(), ice);
//		validateSurname(cust.getSurname(), ice);
//		validatePassword(cust.getPassword(), ice);
//		
//		validateEmail(cust.getEmail(), ice);
//		
//		if (ice.hasErrors())
//			throw ice;
//		
//		// Only do this if all else succeeded, as it's slow
//		checkEmailIsUnique(cust.getEmail(), ice);
//		if (ice.hasErrors())
//			throw ice;
//	}	// validate
//	
//	
//	private void validateEmail(String email, InvalidCommandException ice) {
//		//System.out.println("validateEmail");
//
//		if (email == null || "".equals(email)) {
//			ice.add(new FieldError("email", email, "emailRequired", "E-mail Address is required"));
//			return;
//		}
//		
//		int fioa = email.indexOf("@");
//		int lioa = email.lastIndexOf("@");
//		int liod = email.lastIndexOf(".");
//		int len  = email.length();
//		if(len < MIN_EMAIL               ||
//		len > MAX_EMAIL               ||
//		(fioa != lioa)                ||
//		(len - liod < 3)              ||
//		(lioa > liod)                 ||
//		(fioa < 1)                    ||		// not x@ - make sure there's at least 1 char before @ symbol
//		(email.charAt(fioa+1) == '.') ||		// not xx@.xxx - dot following straight after first @
//		(email.charAt(fioa-1) == '.') ||		// not x.@ - dot preceeding first @
//		(email.charAt(0) == '.')				// first character isn't '.'
//		) {
//			ice.add(new FieldError("email", email, "emailInvalid", "E-mail Address is invalid"));
//			return;
//		}
//		
//		//checkEmailIsUnique(email, ice);
//	}	// validateEmail
//	
//	
//	/**
//	 * This method connects to the EJB server.
//	 * It should only be invoked if all other checks have passed,
//	 * <b>including the email validity check</b>.
//	 */
//	private void checkEmailIsUnique(String email, InvalidCommandException ice) throws RemoteException {
//	 System.out.println("Email is valid. Checking it's unique...");
//		//System.out.println("Will contact server");
//				
//		if (this.customerManagerEJB.usernameIsTaken(email))
//			ice.add(new FieldError("email", email, "emailTaken", "E-mail Address must be unique"));
//	}
//	
//
//	private void validatePassword(String password, InvalidCommandException ice) {
//		if (password == null || "".equals(password)) {
//			ice.add(new FieldError("password", password, "passwordRequired", "Password is required"));
//		}
//		else if (password.length() < MIN_PASSWORD || password.length() > MAX_PASSWORD) {
//			ice.add(new FieldError("password", password, "passwordLengthError", "Password must be between " + MIN_PASSWORD + " and " + MAX_PASSWORD + " characters in length"));
//		}
//	}
//	
//	private void validateForename(String forename, InvalidCommandException ice) {
//		if (forename == null || "".equals(forename)) {
//			ice.add(new FieldError("forename", forename, "forenameRequired", "Forename is required"));
//		} 
//		else if (forename.length() < MIN_FNAME || forename.length() > MAX_FNAME) {
//			ice.add(new FieldError("forename", forename, "passwordRequired", "Forename is invalid"));
//		}
//	}
//	
//	private void validateSurname(String surname, InvalidCommandException ice) {
//		if (surname == null || "".equals(surname)) {
//			ice.add(new FieldError("surname", surname, "surnameRequired", "Surname is required"));
//		} 
//		else if (surname.length() < MIN_LNAME || surname.length() > MAX_LNAME) {
//			ice.add(new FieldError("surname", surname, "surnameRequired", "Surname is invalid"));
//		}
//	}
//	
//	/*
//	private void validateBirthYear(int year) throws PropertyVetoException {
//		if (year <= 0)
//			throw new ErrorCodedPropertyVetoException("Birth year is required", e, "birthYearRequired");
//		if (year < 1940) {
//			System.out.println("Yeah! got another old bugger and vetoed it");
//			throw new ErrorCodedPropertyVetoException("You specified " + year + "; that's too bloody old", e, "tooOld");
//		}
//	}
//	 */
//	
//	
//	/*
//	public static void main(String[] args) {
//		Customer c = new Customer();
//		try {
//			CustomerValidator v = new CustomerValidator();
//			
//			BeanWrapper bw = new BeanWrapperImpl(c);
//			bw.addVetoableChangeListener(v);
//			MutablePropertyValues pvs = new MutablePropertyValues();
//			pvs.addPropertyValue(new PropertyValue("forename", "Bill"));
//			pvs.addPropertyValue(new PropertyValue("surname", "Clinton"));
//			pvs.addPropertyValue(new PropertyValue("password", "monia"));
//			pvs.addPropertyValue(new PropertyValue("email", "bigfoot.com"));
//			pvs.addPropertyValue(new PropertyValue("birthYear", "1966"));
//			//bw.addPropertyValue(new PropertyValue("birthYear", "1920"));
//			bw.setPropertyValues(pvs);
//		}
//		catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		finally {
//			System.out.println(c);
//		}
//	}
//	 */		
//	
//}