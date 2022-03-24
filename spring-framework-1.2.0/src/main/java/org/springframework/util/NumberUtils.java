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

package org.springframework.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Miscellaneous utility methods for number conversion and parsing.
 * Mainly for internal use within the framework; consider Jakarta's
 * Commons Lang for a more comprehensive suite of string utilities.
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @since 1.1.2
 */
public abstract class NumberUtils {

	/**
	 * Convert the given number into an instance of the given target class.
	 * @param number the number to convert
	 * @param targetClass the target class to convert to
	 * @return the converted number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see Byte
	 * @see Short
	 * @see Integer
	 * @see Long
	 * @see BigInteger
	 * @see Float
	 * @see Double
	 * @see BigDecimal
	 */
	public static Number convertNumberToTargetClass(Number number, Class targetClass)
			throws IllegalArgumentException {

		if (targetClass.isInstance(number)) {
			return number;
		}
		else if(targetClass.equals(Byte.class)) {
			return new Byte(number.byteValue());
		}
		else if (targetClass.equals(Short.class)) {
			return new Short(number.shortValue());
		}
		else if (targetClass.equals(Integer.class)) {
			return new Integer(number.intValue());
		}
		else if (targetClass.equals(Long.class)) {
			return new Long(number.longValue());
		}
		else if (targetClass.equals(Float.class)) {
			return new Float(number.floatValue());
		}
		else if (targetClass.equals(Double.class)) {
			return new Double(number.doubleValue());
		}
		else if (targetClass.equals(BigInteger.class)) {
			return BigInteger.valueOf(number.longValue());
		}
		else if (targetClass.equals(BigDecimal.class)) {
			// using BigDecimal(String) here, to avoid unpredictability of BigDecimal(double)
			// (see BigDecimal javadoc for details)
			return new BigDecimal(number.toString());
		}
		else {
			throw new IllegalArgumentException("Couldn't convert number [" + number +
					"] to target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the corresponding default <code>valueOf</code> methods. Trims the
	 * input <code>String</code> before attempting to parse the number.
	 * @param text the text to convert
	 * @param targetClass the target class to parse into
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see Byte#valueOf
	 * @see Short#valueOf
	 * @see Integer#valueOf
	 * @see Long#valueOf
	 * @see BigInteger#BigInteger(String)
	 * @see Float#valueOf
	 * @see Double#valueOf
	 * @see BigDecimal#BigDecimal(String)
	 */
	public static Number parseNumber(String text, Class targetClass) {
		String trimmed = text.trim();

		if(targetClass.equals(Byte.class)) {
			return Byte.valueOf(trimmed);
		}
		else if (targetClass.equals(Short.class)) {
			return Short.valueOf(trimmed);
		}
		else if (targetClass.equals(Integer.class)) {
			return Integer.valueOf(trimmed);
		}
		else if (targetClass.equals(Long.class)) {
			return Long.valueOf(trimmed);
		}
		else if (targetClass.equals(BigInteger.class)) {
			return new BigInteger(trimmed);
		}
		else if (targetClass.equals(Float.class)) {
			return Float.valueOf(trimmed);
		}
		else if (targetClass.equals(Double.class)) {
			return Double.valueOf(trimmed);
		}
		else if (targetClass.equals(BigDecimal.class)) {
			return new BigDecimal(trimmed);
		}
		else {
			throw new IllegalArgumentException(
					"Cannot convert [" + trimmed + "] to target class [" + targetClass.getName() + "]");
		}
	}

	/**
	 * Parse the given text into a number instance of the given target class,
	 * using the given NumberFormat. Trims the input <code>String</code>
	 * before attempting to parse the number.
	 * @param text the text to convert
	 * @param targetClass the target class to parse into
	 * @param numberFormat the NumberFormat to use for parsing
	 * @return the parsed number
	 * @throws IllegalArgumentException if the target class is not supported
	 * (i.e. not a standard Number subclass as included in the JDK)
	 * @see NumberFormat#parse
	 * @see #convertNumberToTargetClass
	 */
	public static Number parseNumber(String text, Class targetClass, NumberFormat numberFormat) {
		try {
			Number number = numberFormat.parse(text.trim());
			return convertNumberToTargetClass(number, targetClass);
		}
		catch (ParseException ex) {
			throw new IllegalArgumentException(ex.getMessage());
		}
	}

}
