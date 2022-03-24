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

package org.springframework.beans;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.springframework.beans.support.DerivedFromProtectedBaseBean;
import org.springframework.util.StringUtils;

/**
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public class BeanWrapperTests extends TestCase {

	public void testSetWrappedInstanceOfSameClass() throws Exception {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		assertTrue(bw.isReadableProperty("age"));
		tb.setAge(11);

		TestBean tb2 = new TestBean();
		bw.setWrappedInstance(tb2);

		bw.setPropertyValue("age", new Integer(14));
		assertTrue("2nd changed", tb2.getAge() == 14);
		assertTrue("1 didn't change", tb.getAge() == 11);
	}

	public void testIsReadablePropertyNotReadable() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		assertFalse(bw.isReadableProperty("age"));
	}

	/**
	 * Shouldn't throw an exception: should just return false
	 */
	public void testIsReadablePropertyNoSuchProperty() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		assertFalse(bw.isReadableProperty("xxxxx"));
	}

	public void testIsReadablePropertyNull() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		try {
			bw.isReadableProperty(null);
			fail("Can't inquire into readability of null property");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testIsWritablePropertyNull() {
		NoRead nr = new NoRead();
		BeanWrapper bw = new BeanWrapperImpl(nr);
		try {
			bw.isWritableProperty(null);
			fail("Can't inquire into writability of null property");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testReadableAndWritableForIndexedProperties() {
		BeanWrapper bw = new BeanWrapperImpl(IndexedTestBean.class);

		assertTrue(bw.isReadableProperty("array"));
		assertTrue(bw.isReadableProperty("list"));
		assertTrue(bw.isReadableProperty("set"));
		assertTrue(bw.isReadableProperty("map"));
		assertFalse(bw.isReadableProperty("xxx"));

		assertTrue(bw.isWritableProperty("array"));
		assertTrue(bw.isWritableProperty("list"));
		assertTrue(bw.isWritableProperty("set"));
		assertTrue(bw.isWritableProperty("map"));
		assertFalse(bw.isWritableProperty("xxx"));

		assertTrue(bw.isReadableProperty("array[0]"));
		assertTrue(bw.isReadableProperty("array[0].name"));
		assertTrue(bw.isReadableProperty("list[0]"));
		assertTrue(bw.isReadableProperty("list[0].name"));
		assertTrue(bw.isReadableProperty("set[0]"));
		assertTrue(bw.isReadableProperty("set[0].name"));
		assertTrue(bw.isReadableProperty("map[key1]"));
		assertTrue(bw.isReadableProperty("map[key1].name"));
		assertTrue(bw.isReadableProperty("map[key4][0]"));
		assertTrue(bw.isReadableProperty("map[key4][0].name"));
		assertTrue(bw.isReadableProperty("map[key4][1]"));
		assertTrue(bw.isReadableProperty("map[key4][1].name"));
		assertFalse(bw.isReadableProperty("array[key1]"));

		assertTrue(bw.isWritableProperty("array[0]"));
		assertTrue(bw.isWritableProperty("array[0].name"));
		assertTrue(bw.isWritableProperty("list[0]"));
		assertTrue(bw.isWritableProperty("list[0].name"));
		assertTrue(bw.isWritableProperty("set[0]"));
		assertTrue(bw.isWritableProperty("set[0].name"));
		assertTrue(bw.isWritableProperty("map[key1]"));
		assertTrue(bw.isWritableProperty("map[key1].name"));
		assertTrue(bw.isWritableProperty("map[key4][0]"));
		assertTrue(bw.isWritableProperty("map[key4][0].name"));
		assertTrue(bw.isWritableProperty("map[key4][1]"));
		assertTrue(bw.isWritableProperty("map[key4][1].name"));
		assertFalse(bw.isWritableProperty("array[key1]"));
	}

	public void testSetWrappedInstanceOfDifferentClass() {
		ThrowsException tex = new ThrowsException();
		BeanWrapper bw = new BeanWrapperImpl(tex);

		TestBean tb2 = new TestBean();
		bw.setWrappedInstance(tb2);

		bw.setPropertyValue("age", new Integer(14));
		assertTrue("2nd changed", tb2.getAge() == 14);
	}

	public void testGetterThrowsException() {
		GetterBean gb = new GetterBean();
		BeanWrapper bw = new BeanWrapperImpl(gb);
		bw.setPropertyValue("name", "tom");
		assertTrue("Set name to tom", gb.getName().equals("tom"));
	}

	public void testEmptyPropertyValuesSet() {
		TestBean t = new TestBean();
		int age = 50;
		String name = "Tony";
		t.setAge(age);
		t.setName(name);
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			assertTrue("age is OK", t.getAge() == age);
			assertTrue("name is OK", name.equals(t.getName()));
			bw.setPropertyValues(new MutablePropertyValues());
			// Check its unchanged
			assertTrue("age is OK", t.getAge() == age);
			assertTrue("name is OK", name.equals(t.getName()));
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testAllValid() {
		TestBean t = new TestBean();
		String newName = "tony";
		int newAge = 65;
		String newTouchy = "valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			//System.out.println(bw);
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("age", new Integer(newAge)));
			pvs.addPropertyValue(new PropertyValue("name", newName));
			pvs.addPropertyValue(new PropertyValue("touchy", newTouchy));
			bw.setPropertyValues(pvs);
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Validly set property must stick", t.getTouchy().equals(newTouchy));
			assertTrue("Validly set property must stick", t.getAge() == newAge);
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testBeanWrapperUpdates() {
		TestBean t = new TestBean();
		int newAge = 33;
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			t.setAge(newAge);
			Object bwAge = bw.getPropertyValue("age");
			assertTrue("Age is an integer", bwAge instanceof Integer);
			int bwi = ((Integer) bwAge).intValue();
			assertTrue("Bean wrapper must pick up changes", bwi == newAge);
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void testValidNullUpdate() {
		TestBean t = new TestBean();
		t.setName("Frank");	// we need to change it back
		t.setSpouse(t);
		BeanWrapper bw = new BeanWrapperImpl(t);
		assertTrue("name is not null to start off", t.getName() != null);
		bw.setPropertyValue("name", null);
		assertTrue("name is now null", t.getName() == null);
		// now test with non-string
		assertTrue("spouse is not null to start off", t.getSpouse() != null);
		bw.setPropertyValue("spouse", null);
		assertTrue("spouse is now null", t.getSpouse() == null);
	}

	public void testIgnoringIndexedProperty() {
		MutablePropertyValues values = new MutablePropertyValues();
		values.addPropertyValue("toBeIgnored[0]", new Integer(42));
		BeanWrapper wrapper = new BeanWrapperImpl(new Object());
		wrapper.setPropertyValues(values, true);
	}

	public void testBooleanObject() {
		BooleanTestBean tb = new BooleanTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);

		try {
			bw.setPropertyValue("bool2", "true");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.TRUE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", tb.getBool2().booleanValue());

		try {
			bw.setPropertyValue("bool2", "false");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}
		assertTrue("Correct bool2 value", Boolean.FALSE.equals(bw.getPropertyValue("bool2")));
		assertTrue("Correct bool2 value", !tb.getBool2().booleanValue());

	}

	public void testNumberObjects() {
		NumberTestBean tb = new NumberTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);

		try {
			bw.setPropertyValue("short2", "2");
			bw.setPropertyValue("int2", "8");
			bw.setPropertyValue("long2", "6");
			bw.setPropertyValue("bigInteger", "3");
			bw.setPropertyValue("float2", "8.1");
			bw.setPropertyValue("double2", "6.1");
			bw.setPropertyValue("bigDecimal", "4.0");
		}
		catch (BeansException ex) {
			fail("Should not throw BeansException: " + ex.getMessage());
		}

		assertTrue("Correct short2 value", new Short("2").equals(bw.getPropertyValue("short2")));
		assertTrue("Correct short2 value", new Short("2").equals(tb.getShort2()));
		assertTrue("Correct int2 value", new Integer("8").equals(bw.getPropertyValue("int2")));
		assertTrue("Correct int2 value", new Integer("8").equals(tb.getInt2()));
		assertTrue("Correct long2 value", new Long("6").equals(bw.getPropertyValue("long2")));
		assertTrue("Correct long2 value", new Long("6").equals(tb.getLong2()));
		assertTrue("Correct bigInteger value", new BigInteger("3").equals(bw.getPropertyValue("bigInteger")));
		assertTrue("Correct bigInteger value", new BigInteger("3").equals(tb.getBigInteger()));
		assertTrue("Correct float2 value", new Float("8.1").equals(bw.getPropertyValue("float2")));
		assertTrue("Correct float2 value", new Float("8.1").equals(tb.getFloat2()));
		assertTrue("Correct double2 value", new Double("6.1").equals(bw.getPropertyValue("double2")));
		assertTrue("Correct double2 value", new Double("6.1").equals(tb.getDouble2()));
		assertTrue("Correct bigDecimal value", new BigDecimal("4.0").equals(bw.getPropertyValue("bigDecimal")));
		assertTrue("Correct bigDecimal value", new BigDecimal("4.0").equals(tb.getBigDecimal()));
	}

	public void testPropertiesProperty() throws Exception {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);
		bw.setPropertyValue("name", "ptest");

		// Note format...
		String ps = "peace=war\nfreedom=slavery";
		bw.setPropertyValue("properties", ps);

		assertTrue("name was set", pt.name.equals("ptest"));
		assertTrue("props non null", pt.props != null);
		String freedomVal = pt.props.getProperty("freedom");
		String peaceVal = pt.props.getProperty("peace");
		assertTrue("peace==war", peaceVal.equals("war"));
		assertTrue("Freedom==slavery", freedomVal.equals("slavery"));
	}

	public void testStringArrayProperty() throws Exception {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);

		bw.setPropertyValue("stringArray", new String[]{"foo", "fi", "fi", "fum"});
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		List list = new ArrayList();
		list.add("foo");
		list.add("fi");
		list.add("fi");
		list.add("fum");
		bw.setPropertyValue("stringArray", list);
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		Set set = new HashSet();
		set.add("foo");
		set.add("fi");
		set.add("fum");
		bw.setPropertyValue("stringArray", set);
		assertTrue("stringArray length = 3", pt.stringArray.length == 3);
		List result = Arrays.asList(pt.stringArray);
		assertTrue("correct values", result.contains("foo") && result.contains("fi") && result.contains("fum"));

		bw.setPropertyValue("stringArray", "one");
		assertTrue("stringArray length = 1", pt.stringArray.length == 1);
		assertTrue("stringArray elt is ok", pt.stringArray[0].equals("one"));

		bw.setPropertyValue("stringArray", "foo,fi,fi,fum");
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));
	}

	public void testStringArrayPropertyWithCustomStringEditor() throws Exception {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);
		bw.registerCustomEditor(String.class, "stringArray", new PropertyEditorSupport() {
			public void setAsText(String text) {
				setValue(text.substring(1));
			}
		});

		bw.setPropertyValue("stringArray", new String[] {"4foo", "7fi", "6fi", "5fum"});
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		List list = new ArrayList();
		list.add("4foo");
		list.add("7fi");
		list.add("6fi");
		list.add("5fum");
		bw.setPropertyValue("stringArray", list);
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));

		Set set = new HashSet();
		set.add("4foo");
		set.add("7fi");
		set.add("6fum");
		bw.setPropertyValue("stringArray", set);
		assertTrue("stringArray length = 3", pt.stringArray.length == 3);
		List result = Arrays.asList(pt.stringArray);
		assertTrue("correct values", result.contains("foo") && result.contains("fi") && result.contains("fum"));

		bw.setPropertyValue("stringArray", "8one");
		assertTrue("stringArray length = 1", pt.stringArray.length == 1);
		assertTrue("correct values", pt.stringArray[0].equals("one"));

		bw.setPropertyValue("stringArray", "1foo,3fi,2fi,1fum");
		assertTrue("stringArray length = 4", pt.stringArray.length == 4);
		assertTrue("correct values", pt.stringArray[0].equals("foo") && pt.stringArray[1].equals("fi") &&
				pt.stringArray[2].equals("fi") && pt.stringArray[3].equals("fum"));
	}

	public void testStringArrayPropertyWithCustomStringArrayEditor() throws Exception {
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.registerCustomEditor(String.class, "name", new PropertyEditorSupport() {
			public void setValue(Object value) {
				if (value instanceof String[]) {
					setValue(StringUtils.arrayToDelimitedString(((String[]) value), "-"));
				}
				else {
					super.setValue(value);
				}
			}
		});
		bw.setPropertyValue("name", new String[] {"a1", "b2"});
		assertEquals("a1-b2", tb.getName());
	}

	public void testIntArrayProperty() {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);

		bw.setPropertyValue("intArray", new int[]{4, 5, 2, 3});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
				pt.intArray[2] == 2 && pt.intArray[3] == 3);

		bw.setPropertyValue("intArray", new String[]{"4", "5", "2", "3"});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
				pt.intArray[2] == 2 && pt.intArray[3] == 3);

		List list = new ArrayList();
		list.add(new Integer(4));
		list.add("5");
		list.add(new Integer(2));
		list.add("3");
		bw.setPropertyValue("intArray", list);
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
				pt.intArray[2] == 2 && pt.intArray[3] == 3);

		Set set = new HashSet();
		set.add("4");
		set.add(new Integer(5));
		set.add("3");
		bw.setPropertyValue("intArray", set);
		assertTrue("intArray length = 3", pt.intArray.length == 3);
		List result = new ArrayList();
		result.add(new Integer(pt.intArray[0]));
		result.add(new Integer(pt.intArray[1]));
		result.add(new Integer(pt.intArray[2]));
		assertTrue("correct values", result.contains(new Integer(4)) && result.contains(new Integer(5)) &&
				result.contains(new Integer(3)));

		bw.setPropertyValue("intArray", new Integer[]{new Integer(1)});
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", new Integer(1));
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", new String[]{"1"});
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", "1");
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);
	}

	public void testIntArrayPropertyWithCustomEditor() {
		PropsTest pt = new PropsTest();
		BeanWrapper bw = new BeanWrapperImpl(pt);
		bw.registerCustomEditor(int.class, new PropertyEditorSupport() {
			public void setAsText(String text) {
				setValue(new Integer(Integer.parseInt(text) + 1));
			}
		});

		bw.setPropertyValue("intArray", new int[]{4, 5, 2, 3});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
				pt.intArray[2] == 2 && pt.intArray[3] == 3);

		bw.setPropertyValue("intArray", new String[]{"3", "4", "1", "2"});
		assertTrue("intArray length = 4", pt.intArray.length == 4);
		assertTrue("correct values", pt.intArray[0] == 4 && pt.intArray[1] == 5 &&
				pt.intArray[2] == 2 && pt.intArray[3] == 3);

		bw.setPropertyValue("intArray", new Integer(1));
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", new String[]{"0"});
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);

		bw.setPropertyValue("intArray", "0");
		assertTrue("intArray length = 4", pt.intArray.length == 1);
		assertTrue("correct values", pt.intArray[0] == 1);
	}

	public void testIndividualAllValid() {
		TestBean t = new TestBean();
		String newName = "tony";
		int newAge = 65;
		String newTouchy = "valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", new Integer(newAge));
			bw.setPropertyValue(new PropertyValue("name", newName));
			bw.setPropertyValue(new PropertyValue("touchy", newTouchy));
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Validly set property must stick", t.getTouchy().equals(newTouchy));
			assertTrue("Validly set property must stick", t.getAge() == newAge);
		}
		catch (BeansException ex) {
			fail("Shouldn't throw exception when everything is valid");
		}
	}

	public void test2Invalid() {
		TestBean t = new TestBean();
		String newName = "tony";
		String invalidTouchy = ".valid";
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			//System.out.println(bw);
			MutablePropertyValues pvs = new MutablePropertyValues();
			pvs.addPropertyValue(new PropertyValue("age", "foobar"));
			pvs.addPropertyValue(new PropertyValue("name", newName));
			pvs.addPropertyValue(new PropertyValue("touchy", invalidTouchy));
			bw.setPropertyValues(pvs);
			fail("Should throw exception when everything is valid");
		}
		catch (PropertyAccessExceptionsException ex) {
			assertTrue("Must contain 2 exceptions", ex.getExceptionCount() == 2);
			// Test validly set property matches
			assertTrue("Validly set property must stick", t.getName().equals(newName));
			assertTrue("Invalidly set property must retain old value", t.getAge() == 0);
			assertTrue("New value of dodgy setter must be available through exception",
					ex.getPropertyAccessException("touchy").getPropertyChangeEvent().getNewValue().equals(invalidTouchy));
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than pvee");
		}
	}

	public void testTypeMismatch() {
		TestBean t = new TestBean();
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", "foobar");
			fail("Should throw exception on type mismatch");
		}
		catch (TypeMismatchException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than Type mismatch");
		}
	}

	public void testEmptyValueForPrimitiveProperty() {
		TestBean t = new TestBean();
		try {
			BeanWrapper bw = new BeanWrapperImpl(t);
			bw.setPropertyValue("age", "");
			fail("Should throw exception on type mismatch");
		}
		catch (TypeMismatchException ex) {
			// expected
		}
		catch (Exception ex) {
			fail("Shouldn't throw exception other than Type mismatch");
		}
	}

	public void testSetPropertyValuesIgnoresInvalidNestedOnRequest() {
		ITestBean rod = new TestBean();
		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue(new PropertyValue("name", "rod"));
		pvs.addPropertyValue(new PropertyValue("graceful.rubbish", "tony"));
		pvs.addPropertyValue(new PropertyValue("more.garbage", new Object()));
		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValues(pvs, true);
		assertTrue("Set valid and ignored invalid", rod.getName().equals("rod"));
		try {
			// Don't ignore: should fail
			bw.setPropertyValues(pvs, false);
			fail("Shouldn't have ignored invalid updates");
		}
		catch (NotWritablePropertyException ex) {
			// OK: but which exception??
		}
	}

	public void testGetNestedProperty() {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 35);
		rod.setSpouse(kerry);
		kerry.setSpouse(rod);
		BeanWrapper bw = new BeanWrapperImpl(rod);
		Integer KA = (Integer) bw.getPropertyValue("spouse.age");
		assertTrue("kerry is 35", KA.intValue() == 35);
		Integer RA = (Integer) bw.getPropertyValue("spouse.spouse.age");
		assertTrue("rod is 31, not" + RA, RA.intValue() == 31);
		ITestBean spousesSpouse = (ITestBean) bw.getPropertyValue("spouse.spouse");
		assertTrue("spousesSpouse = initial point", rod == spousesSpouse);
	}

	public void testGetNestedPropertyNullValue() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 35);
		rod.setSpouse(kerry);

		BeanWrapper bw = new BeanWrapperImpl(rod);
		try {
			bw.getPropertyValue("spouse.spouse.age");
			fail("Shouldn't have succeded with null path");
		}
		catch (NullValueInNestedPathException ex) {
			// ok
			assertTrue("it was the spouse.spouse property that was null, not " + ex.getPropertyName(),
					ex.getPropertyName().equals("spouse.spouse"));
		}
	}

	public void testSetNestedProperty() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new TestBean("kerry", 0);

		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValue("spouse", kerry);

		assertTrue("nested set worked", rod.getSpouse() == kerry);
		assertTrue("no back relation", kerry.getSpouse() == null);
		bw.setPropertyValue(new PropertyValue("spouse.spouse", rod));
		assertTrue("nested set worked", kerry.getSpouse() == rod);
		assertTrue("kerry age not set", kerry.getAge() == 0);
		bw.setPropertyValue(new PropertyValue("spouse.age", new Integer(35)));
		assertTrue("Set primitive on spouse", kerry.getAge() == 35);
	}

	public void testSetNestedPropertyNullValue() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		BeanWrapper bw = new BeanWrapperImpl(rod);
		try {
			bw.setPropertyValue("spouse.age", new Integer(31));
			fail("Shouldn't have succeeded with null path");
		}
		catch (NullValueInNestedPathException ex) {
			// expected
			assertTrue("it was the spouse property that was null, not " + ex.getPropertyName(),
					ex.getPropertyName().equals("spouse"));
		}
	}

	public void testSetNestedPropertyPolymorphic() throws Exception {
		ITestBean rod = new TestBean("rod", 31);
		ITestBean kerry = new Employee();

		BeanWrapper bw = new BeanWrapperImpl(rod);
		bw.setPropertyValue("spouse", kerry);
		bw.setPropertyValue("spouse.age", new Integer(35));
		bw.setPropertyValue("spouse.name", "Kerry");
		bw.setPropertyValue("spouse.company", "Lewisham");
		assertTrue("kerry name is Kerry", kerry.getName().equals("Kerry"));

		assertTrue("nested set worked", rod.getSpouse() == kerry);
		assertTrue("no back relation", kerry.getSpouse() == null);
		bw.setPropertyValue(new PropertyValue("spouse.spouse", rod));
		assertTrue("nested set worked", kerry.getSpouse() == rod);

		BeanWrapper kbw = new BeanWrapperImpl(kerry);
		assertTrue("spouse.spouse.spouse.spouse.company=Lewisham",
				"Lewisham".equals(kbw.getPropertyValue("spouse.spouse.spouse.spouse.company")));
	}

	public void testNewWrappedInstancePropertyValuesGet() {
		BeanWrapper bw = new BeanWrapperImpl();

		TestBean t = new TestBean("Tony", 50);
		bw.setWrappedInstance(t);
		assertEquals("Bean wrapper returns wrong property value",
				new Integer(t.getAge()), bw.getPropertyValue("age"));

		TestBean u = new TestBean("Udo", 30);
		bw.setWrappedInstance(u);
		assertEquals("Bean wrapper returns cached property value",
				new Integer(u.getAge()), bw.getPropertyValue("age"));
	}

	public void testNewWrappedInstanceNestedPropertyValuesGet() {
		BeanWrapper bw = new BeanWrapperImpl();

		TestBean t = new TestBean("Tony", 50);
		t.setSpouse(new TestBean("Sue", 40));
		bw.setWrappedInstance(t);
		assertEquals("Bean wrapper returns wrong nested property value",
				new Integer(t.getSpouse().getAge()), bw.getPropertyValue("spouse.age"));

		TestBean u = new TestBean("Udo", 30);
		u.setSpouse(new TestBean("Vera", 20));
		bw.setWrappedInstance(u);
		assertEquals("Bean wrapper returns cached nested property value",
				new Integer(u.getSpouse().getAge()), bw.getPropertyValue("spouse.age"));
	}

	public void testNullObject() {
		try {
			BeanWrapper bw = new BeanWrapperImpl((Object) null);
			fail("Must throw an exception when constructed with null object");
		}
		catch (IllegalArgumentException ex) {
			// expected
		}
	}

	public void testNestedProperties() {
		String doctorCompany = "";
		String lawyerCompany = "Dr. Sueem";
		TestBean tb = new TestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.setPropertyValue("doctor.company", doctorCompany);
		bw.setPropertyValue("lawyer.company", lawyerCompany);
		assertEquals(doctorCompany, tb.getDoctor().getCompany());
		assertEquals(lawyerCompany, tb.getLawyer().getCompany());
	}

	public void testIndexedProperties() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb6 = ((TestBean) bean.getSet().toArray()[0]);
		TestBean tb7 = ((TestBean) bean.getSet().toArray()[1]);
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key.3"));
		assertEquals("name0", tb0.getName());
		assertEquals("name1", tb1.getName());
		assertEquals("name2", tb2.getName());
		assertEquals("name3", tb3.getName());
		assertEquals("name6", tb6.getName());
		assertEquals("name7", tb7.getName());
		assertEquals("name4", tb4.getName());
		assertEquals("name5", tb5.getName());
		assertEquals("name0", bw.getPropertyValue("array[0].name"));
		assertEquals("name1", bw.getPropertyValue("array[1].name"));
		assertEquals("name2", bw.getPropertyValue("list[0].name"));
		assertEquals("name3", bw.getPropertyValue("list[1].name"));
		assertEquals("name6", bw.getPropertyValue("set[0].name"));
		assertEquals("name7", bw.getPropertyValue("set[1].name"));
		assertEquals("name4", bw.getPropertyValue("map[key1].name"));
		assertEquals("name5", bw.getPropertyValue("map[key.3].name"));
		assertEquals("name4", bw.getPropertyValue("map['key1'].name"));
		assertEquals("name5", bw.getPropertyValue("map[\"key.3\"].name"));
		assertEquals("nameX", bw.getPropertyValue("map[key4][0].name"));
		assertEquals("nameY", bw.getPropertyValue("map[key4][1].name"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0].name", "name5");
		pvs.addPropertyValue("array[1].name", "name4");
		pvs.addPropertyValue("list[0].name", "name3");
		pvs.addPropertyValue("list[1].name", "name2");
		pvs.addPropertyValue("set[0].name", "name8");
		pvs.addPropertyValue("set[1].name", "name9");
		pvs.addPropertyValue("map[key1].name", "name1");
		pvs.addPropertyValue("map['key.3'].name", "name0");
		pvs.addPropertyValue("map[key4][0].name", "nameA");
		pvs.addPropertyValue("map[key4][1].name", "nameB");
		bw.setPropertyValues(pvs);
		assertEquals("name5", tb0.getName());
		assertEquals("name4", tb1.getName());
		assertEquals("name3", tb2.getName());
		assertEquals("name2", tb3.getName());
		assertEquals("name1", tb4.getName());
		assertEquals("name0", tb5.getName());
		assertEquals("name5", bw.getPropertyValue("array[0].name"));
		assertEquals("name4", bw.getPropertyValue("array[1].name"));
		assertEquals("name3", bw.getPropertyValue("list[0].name"));
		assertEquals("name2", bw.getPropertyValue("list[1].name"));
		assertEquals("name8", bw.getPropertyValue("set[0].name"));
		assertEquals("name9", bw.getPropertyValue("set[1].name"));
		assertEquals("name1", bw.getPropertyValue("map[\"key1\"].name"));
		assertEquals("name0", bw.getPropertyValue("map['key.3'].name"));
		assertEquals("nameA", bw.getPropertyValue("map[key4][0].name"));
		assertEquals("nameB", bw.getPropertyValue("map[key4][1].name"));
	}

	public void testIndexedPropertiesWithDirectAccess() {
		IndexedTestBean bean = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		TestBean tb0 = bean.getArray()[0];
		TestBean tb1 = bean.getArray()[1];
		TestBean tb2 = ((TestBean) bean.getList().get(0));
		TestBean tb3 = ((TestBean) bean.getList().get(1));
		TestBean tb6 = ((TestBean) bean.getSet().toArray()[0]);
		TestBean tb7 = ((TestBean) bean.getSet().toArray()[1]);
		TestBean tb4 = ((TestBean) bean.getMap().get("key1"));
		TestBean tb5 = ((TestBean) bean.getMap().get("key2"));
		assertEquals(tb0, bw.getPropertyValue("array[0]"));
		assertEquals(tb1, bw.getPropertyValue("array[1]"));
		assertEquals(tb2, bw.getPropertyValue("list[0]"));
		assertEquals(tb3, bw.getPropertyValue("list[1]"));
		assertEquals(tb6, bw.getPropertyValue("set[0]"));
		assertEquals(tb7, bw.getPropertyValue("set[1]"));
		assertEquals(tb4, bw.getPropertyValue("map[key1]"));
		assertEquals(tb5, bw.getPropertyValue("map[key2]"));
		assertEquals(tb4, bw.getPropertyValue("map['key1']"));
		assertEquals(tb5, bw.getPropertyValue("map[\"key2\"]"));

		MutablePropertyValues pvs = new MutablePropertyValues();
		pvs.addPropertyValue("array[0]", tb5);
		pvs.addPropertyValue("array[1]", tb4);
		pvs.addPropertyValue("list[0]", tb3);
		pvs.addPropertyValue("list[1]", tb2);
		pvs.addPropertyValue("list[2]", tb0);
		pvs.addPropertyValue("list[4]", tb1);
		pvs.addPropertyValue("map[key1]", tb1);
		pvs.addPropertyValue("map['key2']", tb0);
		pvs.addPropertyValue("map[key5]", tb4);
		pvs.addPropertyValue("map['key9']", tb5);
		bw.setPropertyValues(pvs);
		assertEquals(tb5, bean.getArray()[0]);
		assertEquals(tb4, bean.getArray()[1]);
		assertEquals(tb3, ((TestBean) bean.getList().get(0)));
		assertEquals(tb2, ((TestBean) bean.getList().get(1)));
		assertEquals(tb0, ((TestBean) bean.getList().get(2)));
		assertEquals(null, ((TestBean) bean.getList().get(3)));
		assertEquals(tb1, ((TestBean) bean.getList().get(4)));
		assertEquals(tb1, ((TestBean) bean.getMap().get("key1")));
		assertEquals(tb0, ((TestBean) bean.getMap().get("key2")));
		assertEquals(tb4, ((TestBean) bean.getMap().get("key5")));
		assertEquals(tb5, ((TestBean) bean.getMap().get("key9")));
		assertEquals(tb5, bw.getPropertyValue("array[0]"));
		assertEquals(tb4, bw.getPropertyValue("array[1]"));
		assertEquals(tb3, bw.getPropertyValue("list[0]"));
		assertEquals(tb2, bw.getPropertyValue("list[1]"));
		assertEquals(tb0, bw.getPropertyValue("list[2]"));
		assertEquals(null, bw.getPropertyValue("list[3]"));
		assertEquals(tb1, bw.getPropertyValue("list[4]"));
		assertEquals(tb1, bw.getPropertyValue("map[\"key1\"]"));
		assertEquals(tb0, bw.getPropertyValue("map['key2']"));
		assertEquals(tb4, bw.getPropertyValue("map[\"key5\"]"));
		assertEquals(tb5, bw.getPropertyValue("map['key9']"));
	}

	public void testPrimitiveArray() {
		PrimitiveArrayBean tb = new PrimitiveArrayBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		bw.setPropertyValue("array", new String[]{"1", "2"});
		assertEquals(2, tb.getArray().length);
		assertEquals(1, tb.getArray()[0]);
		assertEquals(2, tb.getArray()[1]);
	}

	public void testPropertiesInProtectedBaseBean() {
		DerivedFromProtectedBaseBean bean = new DerivedFromProtectedBaseBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);
		bw.setPropertyValue("someProperty", "someValue");
		assertEquals("someValue", bw.getPropertyValue("someProperty"));
		assertEquals("someValue", bean.getSomeProperty());
	}

	public void testErrorMessageOfNestedProperty() {
		ITestBean parent = new TestBean();
		ITestBean child = new DifferentTestBean();
		child.setName("test");
		parent.setSpouse(child);
		BeanWrapper bw = new BeanWrapperImpl(parent);
		try {
			bw.getPropertyValue("spouse.bla");
		}
		catch (NotReadablePropertyException ex) {
			assertTrue(ex.getMessage().indexOf(TestBean.class.getName()) != -1);
		}
	}

	public void testMatchingCollections() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		Collection coll = new HashSet();
		coll.add("coll1");
		bw.setPropertyValue("collection", coll);
		Set set = new HashSet();
		set.add("set1");
		bw.setPropertyValue("set", set);
		SortedSet sortedSet = new TreeSet();
		sortedSet.add("sortedSet1");
		bw.setPropertyValue("sortedSet", sortedSet);
		List list = new LinkedList();
		list.add("list1");
		bw.setPropertyValue("list", list);
		assertEquals(coll, tb.getCollection());
		assertEquals(set, tb.getSet());
		assertEquals(sortedSet, tb.getSortedSet());
		assertEquals(list, tb.getList());
	}

	public void testNonMatchingCollections() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		Collection coll = new ArrayList();
		coll.add("coll1");
		bw.setPropertyValue("collection", coll);
		List set = new LinkedList();
		set.add("set1");
		bw.setPropertyValue("set", set);
		List sortedSet = new ArrayList();
		sortedSet.add("sortedSet1");
		bw.setPropertyValue("sortedSet", sortedSet);
		Set list = new HashSet();
		list.add("list1");
		bw.setPropertyValue("list", list);
		assertEquals(1, tb.getCollection().size());
		assertTrue(tb.getCollection().containsAll(coll));
		assertEquals(1, tb.getSet().size());
		assertTrue(tb.getSet().containsAll(set));
		assertEquals(1, tb.getSortedSet().size());
		assertTrue(tb.getSortedSet().containsAll(sortedSet));
		assertEquals(1, tb.getList().size());
		assertTrue(tb.getList().containsAll(list));
	}

	public void testCollectionsWithArrayValues() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		Collection coll = new HashSet();
		coll.add("coll1");
		bw.setPropertyValue("collection", coll.toArray());
		List set = new LinkedList();
		set.add("set1");
		bw.setPropertyValue("set", set.toArray());
		List sortedSet = new ArrayList();
		sortedSet.add("sortedSet1");
		bw.setPropertyValue("sortedSet", sortedSet.toArray());
		Set list = new HashSet();
		list.add("list1");
		bw.setPropertyValue("list", list.toArray());
		assertEquals(1, tb.getCollection().size());
		assertTrue(tb.getCollection().containsAll(coll));
		assertEquals(1, tb.getSet().size());
		assertTrue(tb.getSet().containsAll(set));
		assertEquals(1, tb.getSortedSet().size());
		assertTrue(tb.getSortedSet().containsAll(sortedSet));
		assertEquals(1, tb.getList().size());
		assertTrue(tb.getList().containsAll(list));
	}

	public void testCollectionsWithIntArrayValues() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		Collection coll = new HashSet();
		coll.add(new Integer(0));
		bw.setPropertyValue("collection", new int[] {0});
		List set = new LinkedList();
		set.add(new Integer(1));
		bw.setPropertyValue("set", new int[] {1});
		List sortedSet = new ArrayList();
		sortedSet.add(new Integer(2));
		bw.setPropertyValue("sortedSet", new int[] {2});
		Set list = new HashSet();
		list.add(new Integer(3));
		bw.setPropertyValue("list", new int[] {3});
		assertEquals(1, tb.getCollection().size());
		assertTrue(tb.getCollection().containsAll(coll));
		assertEquals(1, tb.getSet().size());
		assertTrue(tb.getSet().containsAll(set));
		assertEquals(1, tb.getSortedSet().size());
		assertTrue(tb.getSortedSet().containsAll(sortedSet));
		assertEquals(1, tb.getList().size());
		assertTrue(tb.getList().containsAll(list));
	}

	public void testCollectionsWithIntegerValues() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		Collection coll = new HashSet();
		coll.add(new Integer(0));
		bw.setPropertyValue("collection", new Integer(0));
		List set = new LinkedList();
		set.add(new Integer(1));
		bw.setPropertyValue("set", new Integer(1));
		List sortedSet = new ArrayList();
		sortedSet.add(new Integer(2));
		bw.setPropertyValue("sortedSet", new Integer(2));
		Set list = new HashSet();
		list.add(new Integer(3));
		bw.setPropertyValue("list", new Integer(3));
		assertEquals(1, tb.getCollection().size());
		assertTrue(tb.getCollection().containsAll(coll));
		assertEquals(1, tb.getSet().size());
		assertTrue(tb.getSet().containsAll(set));
		assertEquals(1, tb.getSortedSet().size());
		assertTrue(tb.getSortedSet().containsAll(sortedSet));
		assertEquals(1, tb.getList().size());
		assertTrue(tb.getList().containsAll(list));
	}

	public void testCollectionsWithStringValues() {
		IndexedTestBean tb = new IndexedTestBean();
		BeanWrapper bw = new BeanWrapperImpl(tb);
		List set = new LinkedList();
		set.add("set1");
		bw.setPropertyValue("set", "set1");
		List sortedSet = new ArrayList();
		sortedSet.add("sortedSet1");
		bw.setPropertyValue("sortedSet", "sortedSet1");
		Set list = new HashSet();
		list.add("list1");
		bw.setPropertyValue("list", "list1");
		assertEquals(1, tb.getSet().size());
		assertTrue(tb.getSet().containsAll(set));
		assertEquals(1, tb.getSortedSet().size());
		assertTrue(tb.getSortedSet().containsAll(sortedSet));
		assertEquals(1, tb.getList().size());
		assertTrue(tb.getList().containsAll(list));
	}

	public void testSetNumberProperties() {
    NumberPropertyBean bean = new NumberPropertyBean();
		BeanWrapper bw = new BeanWrapperImpl(bean);

		String byteValue = " " + Byte.MAX_VALUE + " ";
		String shortValue = " " + Short.MAX_VALUE + " ";
		String intValue = " " + Integer.MAX_VALUE + " ";
		String longValue = " " + Long.MAX_VALUE + " ";
		String floatValue = " " + Float.MAX_VALUE + " ";
		String doubleValue = " " + Double.MAX_VALUE + " ";

		bw.setPropertyValue("myPrimitiveByte", byteValue);
		bw.setPropertyValue("myByte", byteValue);

		bw.setPropertyValue("myPrimitiveShort", shortValue);
		bw.setPropertyValue("myShort", shortValue);

		bw.setPropertyValue("myPrimitiveInt", intValue);
		bw.setPropertyValue("myInteger", intValue);

		bw.setPropertyValue("myPrimitiveLong", longValue);
		bw.setPropertyValue("myLong", longValue);

		bw.setPropertyValue("myPrimitiveFloat", floatValue);
		bw.setPropertyValue("myFloat", floatValue);

		bw.setPropertyValue("myPrimitiveDouble", doubleValue);
		bw.setPropertyValue("myDouble", doubleValue);

		assertEquals(Byte.MAX_VALUE, bean.getMyPrimitiveByte());
		assertEquals(Byte.MAX_VALUE, bean.getMyByte().byteValue());

		assertEquals(Short.MAX_VALUE, bean.getMyPrimitiveShort());
		assertEquals(Short.MAX_VALUE, bean.getMyShort().shortValue());

		assertEquals(Integer.MAX_VALUE, bean.getMyPrimitiveInt());
		assertEquals(Integer.MAX_VALUE, bean.getMyInteger().intValue());

		assertEquals(Long.MAX_VALUE, bean.getMyPrimitiveLong());
		assertEquals(Long.MAX_VALUE, bean.getMyLong().longValue());

		assertEquals(Float.MAX_VALUE, bean.getMyPrimitiveFloat(), 0.001);
		assertEquals(Float.MAX_VALUE, bean.getMyFloat().floatValue(), 0.001);

		assertEquals(Double.MAX_VALUE, bean.getMyPrimitiveDouble(), 0.001);
		assertEquals(Double.MAX_VALUE, bean.getMyDouble().doubleValue(), 0.001);

	}


	private static class DifferentTestBean extends TestBean {
		// class to test naming of beans in a BeanWrapper error message
	}


	private static class NoRead {

		public void setAge(int age) {
		}
	}


	private static class PropsTest {

		private Properties props;

		private String name;

		private String[] stringArray;

		private int[] intArray;

		public void setProperties(Properties p) {
			props = p;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setStringArray(String[] sa) {
			this.stringArray = sa;
		}

		public void setIntArray(int[] intArray) {
			this.intArray = intArray;
		}
	}


	private static class GetterBean {

		private String name;

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			if (this.name == null) {
				throw new RuntimeException("name property must be set");
			}
			return name;
		}
	}


	private static class ThrowsException {

		public void doSomething(Throwable t) throws Throwable {
			throw t;
		}
	}


	private static class PrimitiveArrayBean {

		private int[] array;

		public int[] getArray() {
			return array;
		}

		public void setArray(int[] array) {
			this.array = array;
		}
	}

	private static class NumberPropertyBean {
		private byte myPrimitiveByte;
		private Byte myByte;

		private short myPrimitiveShort;
		private Short myShort;

		private int myPrimitiveInt;
		private Integer myInteger;

		private long myPrimitiveLong;
		private Long myLong;

		private float myPrimitiveFloat;
		private Float myFloat;

		private double myPrimitiveDouble;
		private Double myDouble;

		public byte getMyPrimitiveByte() {
			return myPrimitiveByte;
		}

		public void setMyPrimitiveByte(byte myPrimitiveByte) {
			this.myPrimitiveByte = myPrimitiveByte;
		}

		public Byte getMyByte() {
			return myByte;
		}

		public void setMyByte(Byte myByte) {
			this.myByte = myByte;
		}

		public short getMyPrimitiveShort() {
			return myPrimitiveShort;
		}

		public void setMyPrimitiveShort(short myPrimitiveShort) {
			this.myPrimitiveShort = myPrimitiveShort;
		}

		public Short getMyShort() {
			return myShort;
		}

		public void setMyShort(Short myShort) {
			this.myShort = myShort;
		}

		public int getMyPrimitiveInt() {
			return myPrimitiveInt;
		}

		public void setMyPrimitiveInt(int myPrimitiveInt) {
			this.myPrimitiveInt = myPrimitiveInt;
		}

		public Integer getMyInteger() {
			return myInteger;
		}

		public void setMyInteger(Integer myInteger) {
			this.myInteger = myInteger;
		}

		public long getMyPrimitiveLong() {
			return myPrimitiveLong;
		}

		public void setMyPrimitiveLong(long myPrimitiveLong) {
			this.myPrimitiveLong = myPrimitiveLong;
		}

		public Long getMyLong() {
			return myLong;
		}

		public void setMyLong(Long myLong) {
			this.myLong = myLong;
		}

		public float getMyPrimitiveFloat() {
			return myPrimitiveFloat;
		}

		public void setMyPrimitiveFloat(float myPrimitiveFloat) {
			this.myPrimitiveFloat = myPrimitiveFloat;
		}

		public Float getMyFloat() {
			return myFloat;
		}

		public void setMyFloat(Float myFloat) {
			this.myFloat = myFloat;
		}

		public double getMyPrimitiveDouble() {
			return myPrimitiveDouble;
		}

		public void setMyPrimitiveDouble(double myPrimitiveDouble) {
			this.myPrimitiveDouble = myPrimitiveDouble;
		}

		public Double getMyDouble() {
			return myDouble;
		}

		public void setMyDouble(Double myDouble) {
			this.myDouble = myDouble;
		}
	}
}
