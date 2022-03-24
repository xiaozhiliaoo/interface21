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

package org.springframework.beans.factory.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.springframework.beans.TestBean;
import org.springframework.beans.factory.HasMap;
import org.springframework.beans.factory.config.ListFactoryBean;
import org.springframework.beans.factory.config.MapFactoryBean;
import org.springframework.beans.factory.config.SetFactoryBean;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for collections in XML bean definitions.
 * @author Juergen Hoeller
 * @since 19.12.2004
 */
public class XmlBeanCollectionTests extends TestCase {

	public void testRefSubelement() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		//assertTrue("5 beans in reftypes, not " + xbf.getBeanDefinitionCount(), xbf.getBeanDefinitionCount() == 5);
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		assertTrue(jen.getSpouse() == dave);
	}

	public void testPropertyWithLiteralValueSubelement() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		TestBean verbose = (TestBean) xbf.getBean("verbose");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testPropertyWithIdRefLocalAttrSubelement() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		TestBean verbose = (TestBean) xbf.getBean("verbose2");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testPropertyWithIdRefBeanAttrSubelement() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		TestBean verbose = (TestBean) xbf.getBean("verbose3");
		assertTrue(verbose.getName().equals("verbose"));
	}

	public void testRefSubelementsBuildCollection() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		TestBean jen = (TestBean) xbf.getBean("jenny");
		TestBean dave = (TestBean) xbf.getBean("david");
		TestBean rod = (TestBean) xbf.getBean("rod");

		// Must be a list to support ordering
		// Our bean doesn't modify the collection:
		// of course it could be a different copy in a real object.
		List friends = (List) rod.getFriends();
		assertTrue(friends.size() == 2);

		assertTrue("First friend must be jen, not " + friends.get(0), friends.get(0) == jen);
		assertTrue(friends.get(1) == dave);
		// Should be ordered
	}

	public void testRefSubelementsBuildCollectionWithPrototypes() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));

		TestBean jen = (TestBean) xbf.getBean("pJenny");
		TestBean dave = (TestBean) xbf.getBean("pDavid");
		TestBean rod = (TestBean) xbf.getBean("pRod");
		List friends = (List) rod.getFriends();
		assertTrue(friends.size() == 2);
		assertTrue("First friend must be jen, not " + friends.get(0),
				friends.get(0).toString().equals(jen.toString()));
		assertTrue("Jen not same instance", friends.get(0) != jen);
		assertTrue(friends.get(1).toString().equals(dave.toString()));
		assertTrue("Dave not same instance", friends.get(1) != dave);

		TestBean rod2 = (TestBean) xbf.getBean("pRod");
		List friends2 = (List) rod2.getFriends();
		assertTrue(friends2.size() == 2);
		assertTrue("First friend must be jen, not " + friends2.get(0),
				friends2.get(0).toString().equals(jen.toString()));
		assertTrue("Jen not same instance", friends2.get(0) != friends.get(0));
		assertTrue(friends2.get(1).toString().equals(dave.toString()));
		assertTrue("Dave not same instance", friends2.get(1) != friends.get(1));
	}

	public void testRefSubelementsBuildCollectionFromSingleElement() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		TestBean loner = (TestBean) xbf.getBean("loner");
		TestBean dave = (TestBean) xbf.getBean("david");
		assertTrue(loner.getFriends().size() == 1);
		assertTrue(loner.getFriends().contains(dave));
	}

	public void testBuildCollectionFromMixtureOfReferencesAndValues() throws Exception {
		// Ensure that a test runner like Eclipse, that keeps the same JVM up,
		// will get fresh static values.
		MixedCollectionBean.resetStaticState();
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		MixedCollectionBean jumble = (MixedCollectionBean) xbf.getBean("jumble");
		assertEquals(1, MixedCollectionBean.nrOfInstances);
		assertTrue("Expected 3 elements, not " + jumble.getJumble().size(),
				jumble.getJumble().size() == 4);
		List l = (List) jumble.getJumble();
		assertTrue(l.get(0).equals(xbf.getBean("david")));
		assertTrue(l.get(1).equals("literal"));
		assertTrue(l.get(2).equals(xbf.getBean("jenny")));
		assertTrue(l.get(3).equals("rod"));
	}

	public void testEmptyMap() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("emptyMap");
		assertTrue(hasMap.getMap().size() == 0);
	}

	public void testMapWithLiteralsOnly() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("literalMap");
		assertTrue(hasMap.getMap().size() == 3);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("fi").equals("fum"));
		assertTrue(hasMap.getMap().get("fa") == null);
	}

	public void testMapWithLiteralsAndReferences() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("mixedMap");
		assertTrue(hasMap.getMap().size() == 3);
		assertTrue(hasMap.getMap().get("foo").equals(new Integer(10)));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getMap().get("jenny") == jenny);
		assertTrue(hasMap.getMap().get(new Integer(5)).equals("david"));
	}

	public void testMapWithLiteralsAndPrototypeReferences() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));

		TestBean jenny = (TestBean) xbf.getBean("pJenny");
		HasMap hasMap = (HasMap) xbf.getBean("pMixedMap");
		assertTrue(hasMap.getMap().size() == 2);
		assertTrue(hasMap.getMap().get("foo").equals("bar"));
		assertTrue(hasMap.getMap().get("jenny").toString().equals(jenny.toString()));
		assertTrue("Not same instance", hasMap.getMap().get("jenny") != jenny);

		HasMap hasMap2 = (HasMap) xbf.getBean("pMixedMap");
		assertTrue(hasMap2.getMap().size() == 2);
		assertTrue(hasMap2.getMap().get("foo").equals("bar"));
		assertTrue(hasMap2.getMap().get("jenny").toString().equals(jenny.toString()));
		assertTrue("Not same instance", hasMap2.getMap().get("jenny") != hasMap.getMap().get("jenny"));
	}

	public void testMapWithLiteralsReferencesAndList() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("mixedMapWithList");
		assertTrue(hasMap.getMap().size() == 4);
		assertTrue(hasMap.getMap().get(null).equals("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getMap().get("jenny").equals(jenny));

		// Check list
		List l = (List) hasMap.getMap().get("list");
		assertNotNull(l);
		assertTrue(l.size() == 4);
		assertTrue(l.get(0).equals("zero"));
		assertTrue(l.get(3) == null);

		// Check nested map in list
		Map m = (Map) l.get(1);
		assertNotNull(m);
		assertTrue(m.size() == 2);
		assertTrue(m.get("fo").equals("bar"));
		assertTrue("Map element 'jenny' should be equal to jenny bean, not " + m.get("jen"),
				m.get("jen").equals(jenny));

		// Check nested list in list
		l = (List) l.get(2);
		assertNotNull(l);
		assertTrue(l.size() == 2);
		assertTrue(l.get(0).equals(jenny));
		assertTrue(l.get(1).equals("ba"));

		// Check nested map
		m = (Map) hasMap.getMap().get("map");
		assertNotNull(m);
		assertTrue(m.size() == 2);
		assertTrue(m.get("foo").equals("bar"));
		assertTrue("Map element 'jenny' should be equal to jenny bean, not " + m.get("jenny"),
				m.get("jenny").equals(jenny));
	}

	public void testEmptySet() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("emptySet");
		assertTrue(hasMap.getSet().size() == 0);
	}

	public void testPopulatedSet() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("set");
		assertTrue(hasMap.getSet().size() == 3);
		assertTrue(hasMap.getSet().contains("bar"));
		TestBean jenny = (TestBean) xbf.getBean("jenny");
		assertTrue(hasMap.getSet().contains(jenny));
		assertTrue(hasMap.getSet().contains(null));
		Iterator it = hasMap.getSet().iterator();
		assertEquals("bar", it.next());
		assertEquals(jenny, it.next());
		assertEquals(null, it.next());
	}

	public void testEmptyProps() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("emptyProps");
		assertTrue(hasMap.getProps().size() == 0);
	}

	public void testPopulatedProps() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("props");
		assertTrue(hasMap.getProps().size() == 2);
		assertTrue(hasMap.getProps().get("foo").equals("bar"));
		assertTrue(hasMap.getProps().get("2").equals("TWO"));
	}

	public void testObjectArray() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("objectArray");
		assertTrue(hasMap.getObjectArray().length == 2);
		assertTrue(hasMap.getObjectArray()[0].equals("one"));
		assertTrue(hasMap.getObjectArray()[1].equals(xbf.getBean("jenny")));
	}

	public void testClassArray() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("classArray");
		assertTrue(hasMap.getClassArray().length == 2);
		assertTrue(hasMap.getClassArray()[0].equals(String.class));
		assertTrue(hasMap.getClassArray()[1].equals(Exception.class));
	}

	public void testIntegerArray() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		HasMap hasMap = (HasMap) xbf.getBean("integerArray");
		assertTrue(hasMap.getIntegerArray().length == 3);
		assertTrue(hasMap.getIntegerArray()[0].intValue() == 0);
		assertTrue(hasMap.getIntegerArray()[1].intValue() == 1);
		assertTrue(hasMap.getIntegerArray()[2].intValue() == 2);
	}

	public void testProps() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));

		HasMap hasMap = (HasMap) xbf.getBean("props");
		assertEquals(2, hasMap.getProps().size());
		assertEquals("bar", hasMap.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap.getProps().getProperty("2"));

		HasMap hasMap2 = (HasMap) xbf.getBean("propsViaMap");
		assertEquals(2, hasMap2.getProps().size());
		assertEquals("bar", hasMap2.getProps().getProperty("foo"));
		assertEquals("TWO", hasMap2.getProps().getProperty("2"));
	}

	public void testListFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		List list = (List) xbf.getBean("listFactory");
		assertTrue(list instanceof LinkedList);
		assertTrue(list.size() == 2);
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	public void testPrototypeListFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		List list = (List) xbf.getBean("pListFactory");
		assertTrue(list instanceof LinkedList);
		assertTrue(list.size() == 2);
		assertEquals("bar", list.get(0));
		assertEquals("jenny", list.get(1));
	}

	public void testSetFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		Set set = (Set) xbf.getBean("setFactory");
		assertTrue(set instanceof TreeSet);
		assertTrue(set.size() == 2);
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	public void testPrototypeSetFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		Set set = (Set) xbf.getBean("pSetFactory");
		assertTrue(set instanceof TreeSet);
		assertTrue(set.size() == 2);
		assertTrue(set.contains("bar"));
		assertTrue(set.contains("jenny"));
	}

	public void testMapFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		Map map = (Map) xbf.getBean("mapFactory");
		assertTrue(map instanceof TreeMap);
		assertTrue(map.size() == 2);
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	public void testPrototypeMapFactory() throws Exception {
		XmlBeanFactory xbf = new XmlBeanFactory(new ClassPathResource("collections.xml", getClass()));
		Map map = (Map) xbf.getBean("pMapFactory");
		assertTrue(map instanceof TreeMap);
		assertTrue(map.size() == 2);
		assertEquals("bar", map.get("foo"));
		assertEquals("jenny", map.get("jen"));
	}

	public void testCollectionFactoryDefaults() throws Exception {
		ListFactoryBean listFactory = new ListFactoryBean();
		listFactory.setSourceList(new LinkedList());
		listFactory.afterPropertiesSet();
		assertTrue(listFactory.getObject() instanceof ArrayList);

		SetFactoryBean setFactory = new SetFactoryBean();
		setFactory.setSourceSet(new TreeSet());
		setFactory.afterPropertiesSet();
		assertTrue(setFactory.getObject() instanceof HashSet);

		MapFactoryBean mapFactory = new MapFactoryBean();
		mapFactory.setSourceMap(new TreeMap());
		mapFactory.afterPropertiesSet();
		assertTrue(mapFactory.getObject() instanceof HashMap);
	}

}
