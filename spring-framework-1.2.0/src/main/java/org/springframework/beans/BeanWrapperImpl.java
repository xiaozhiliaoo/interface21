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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.propertyeditors.ByteArrayPropertyEditor;
import org.springframework.beans.propertyeditors.CharacterEditor;
import org.springframework.beans.propertyeditors.ClassEditor;
import org.springframework.beans.propertyeditors.CustomBooleanEditor;
import org.springframework.beans.propertyeditors.CustomCollectionEditor;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.beans.propertyeditors.FileEditor;
import org.springframework.beans.propertyeditors.InputStreamEditor;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.beans.propertyeditors.PropertiesEditor;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.beans.propertyeditors.URLEditor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceArrayPropertyEditor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of the BeanWrapper interface that should be sufficient
 * for all typical use cases. Caches introspection results for efficiency.
 *
 * <p>Note: This class never tries to load a class by name, as this can pose
 * class loading problems in J2EE applications with multiple deployment modules.
 * The caller is responsible for loading a target class.
 *
 * <p>Note: Auto-registers default property editors from the
 * <code>org.springframework.beans.propertyeditors</code> package, which apply
 * in addition to the JDK's standard PropertyEditors. Applications can call
 * BeanWrapper's <code>registerCustomEditor</code> method to register an editor
 * for the particular instance (i.e. they're not shared across the application).
 *
 * <p>BeanWrapperImpl will convert collection and array values to the
 * corresponding target collections or arrays, if necessary. Custom property
 * editors that deal with collections or arrays can either be written via
 * PropertyEditor's <code>setValue</code>, or against a comma-delimited String
 * via <code>setAsText</code>, as String arrays are converted in such a format
 * if the array itself is not assignable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @see #registerCustomEditor
 * @see PropertyEditorManager
 * @see java.beans.PropertyEditorSupport#setAsText
 * @see java.beans.PropertyEditorSupport#setValue
 * @see ByteArrayPropertyEditor
 * @see ClassEditor
 * @see CharacterEditor
 * @see CustomBooleanEditor
 * @see CustomNumberEditor
 * @see CustomCollectionEditor
 * @see FileEditor
 * @see InputStreamEditor
 * @see org.springframework.jndi.JndiTemplateEditor
 * @see LocaleEditor
 * @see PropertiesEditor
 * @see PropertyValuesEditor
 * @see ResourceArrayPropertyEditor
 * @see org.springframework.core.io.ResourceEditor
 * @see StringArrayPropertyEditor
 * @see org.springframework.transaction.interceptor.TransactionAttributeEditor
 * @see org.springframework.transaction.interceptor.TransactionAttributeSourceEditor
 * @see URLEditor
 * @since 15 April 2001
 */
public class BeanWrapperImpl implements BeanWrapper {

	/**
	 * We'll create a lot of these objects, so we don't want a new logger every time
	 */
	private static final Log logger = LogFactory.getLog(BeanWrapperImpl.class);


	//---------------------------------------------------------------------
	// Instance data
	//---------------------------------------------------------------------

	/**
	 * The wrapped object
	 */
	private Object object;

	/**
	 * The nested path of the object
	 */
	private String nestedPath = "";

	private Object rootObject;

	/**
	 * Registry for default PropertyEditors
	 */
	private final Map defaultEditors;

	/**
	 * Map with custom PropertyEditor instances
	 */
	private Map customEditors;

	/**
	 * Cached introspections results for this object, to prevent encountering
	 * the cost of JavaBeans introspection every time.
	 */
	private CachedIntrospectionResults cachedIntrospectionResults;

	/* Map with cached nested BeanWrappers */
	private Map nestedBeanWrappers;


	//---------------------------------------------------------------------
	// Constructors
	//---------------------------------------------------------------------

	/**
	 * Create new empty BeanWrapperImpl. Wrapped instance needs to be set afterwards.
	 * @see #setWrappedInstance
	 */
	public BeanWrapperImpl() {
		this.defaultEditors = new HashMap(32);
		registerDefaultEditors();
	}

	/**
	 * Create new BeanWrapperImpl for the given object.
	 * @param object object wrapped by this BeanWrapper
	 */
	public BeanWrapperImpl(Object object) {
		this();
		setWrappedInstance(object);
	}

	/**
	 * Create new BeanWrapperImpl, wrapping a new instance of the specified class.
	 * @param clazz class to instantiate and wrap
	 */
	public BeanWrapperImpl(Class clazz) {
		this();
		setWrappedInstance(BeanUtils.instantiateClass(clazz));
	}

	/**
	 * Create new BeanWrapperImpl for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper.
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
	public BeanWrapperImpl(Object object, String nestedPath, Object rootObject) {
		this();
		setWrappedInstance(object, nestedPath, rootObject);
	}

	/**
	 * Create new BeanWrapperImpl for the given object,
	 * registering a nested path that the object is in.
	 * @param object object wrapped by this BeanWrapper.
	 * @param nestedPath the nested path of the object
	 * @param superBw the containing BeanWrapper (must not be null)
	 */
	private BeanWrapperImpl(Object object, String nestedPath, BeanWrapperImpl superBw) {
		this.defaultEditors = superBw.defaultEditors;
		setWrappedInstance(object, nestedPath, superBw.getWrappedInstance());
	}

	/**
	 * Register default editors in this class, for restricted environments.
	 * We're not using the JRE's PropertyEditorManager to avoid potential
	 * SecurityExceptions when running in a SecurityManager.
	 * <p>Registers a <code>CustomNumberEditor</code> for all primitive number types,
	 * their corresponding wrapper types, <code>BigInteger</code> and <code>BigDecimal</code>.
	 */
	private void registerDefaultEditors() {
		// Simple editors, without parameterization capabilities.
		// The JDK does not contain a default editor for any of these target types.
		this.defaultEditors.put(byte[].class, new ByteArrayPropertyEditor());
		this.defaultEditors.put(Class.class, new ClassEditor());
		this.defaultEditors.put(File.class, new FileEditor());
		this.defaultEditors.put(InputStream.class, new InputStreamEditor());
		this.defaultEditors.put(Locale.class, new LocaleEditor());
		this.defaultEditors.put(Properties.class, new PropertiesEditor());
		this.defaultEditors.put(Resource[].class, new ResourceArrayPropertyEditor());
		this.defaultEditors.put(String[].class, new StringArrayPropertyEditor());
		this.defaultEditors.put(URL.class, new URLEditor());

		// Default instances of collection editors.
		// Can be overridden by registering custom instances of those as custom editors.
		this.defaultEditors.put(Collection.class, new CustomCollectionEditor(Collection.class));
		this.defaultEditors.put(Set.class, new CustomCollectionEditor(Set.class));
		this.defaultEditors.put(SortedSet.class, new CustomCollectionEditor(SortedSet.class));
		this.defaultEditors.put(List.class, new CustomCollectionEditor(List.class));

		// Default instances of character and boolean editors.
		// Can be overridden by registering custom instances of those as custom editors.
		PropertyEditor characterEditor = new CharacterEditor(false);
		PropertyEditor booleanEditor = new CustomBooleanEditor(false);

		// The JDK does not contain a default editor for char!
		this.defaultEditors.put(char.class, characterEditor);
		this.defaultEditors.put(Character.class, characterEditor);

		// Spring's CustomBooleanEditor accepts more flag values than the JDK's default editor.
		this.defaultEditors.put(boolean.class, booleanEditor);
		this.defaultEditors.put(Boolean.class, booleanEditor);

		// The JDK does not contain default editors for number wrapper types!
		// Override JDK primitive number editors with our own CustomNumberEditor.
		PropertyEditor byteEditor = new CustomNumberEditor(Byte.class, false);
		PropertyEditor shortEditor = new CustomNumberEditor(Short.class, false);
		PropertyEditor integerEditor = new CustomNumberEditor(Integer.class, false);
		PropertyEditor longEditor = new CustomNumberEditor(Long.class, false);
		PropertyEditor floatEditor = new CustomNumberEditor(Float.class, false);
		PropertyEditor doubleEditor = new CustomNumberEditor(Double.class, false);

		this.defaultEditors.put(byte.class, byteEditor);
		this.defaultEditors.put(Byte.class, byteEditor);

		this.defaultEditors.put(short.class, shortEditor);
		this.defaultEditors.put(Short.class, shortEditor);

		this.defaultEditors.put(int.class, integerEditor);
		this.defaultEditors.put(Integer.class, integerEditor);

		this.defaultEditors.put(long.class, longEditor);
		this.defaultEditors.put(Long.class, longEditor);

		this.defaultEditors.put(float.class, floatEditor);
		this.defaultEditors.put(Float.class, floatEditor);

		this.defaultEditors.put(double.class, doubleEditor);
		this.defaultEditors.put(Double.class, doubleEditor);

		this.defaultEditors.put(BigDecimal.class, new CustomNumberEditor(BigDecimal.class, false));
		this.defaultEditors.put(BigInteger.class, new CustomNumberEditor(BigInteger.class, false));
	}


	//---------------------------------------------------------------------
	// Implementation of BeanWrapper
	//---------------------------------------------------------------------

	/**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 */
	public void setWrappedInstance(Object object) {
		setWrappedInstance(object, "", null);
	}

	/**
	 * Switch the target object, replacing the cached introspection results only
	 * if the class of the new object is different to that of the replaced object.
	 * @param object new target
	 * @param nestedPath the nested path of the object
	 * @param rootObject the root object at the top of the path
	 */
	public void setWrappedInstance(Object object, String nestedPath, Object rootObject) {
		if (object == null) {
			throw new IllegalArgumentException("Cannot set BeanWrapperImpl target to a null object");
		}
		this.object = object;
		this.nestedPath = (nestedPath != null ? nestedPath : "");
		this.rootObject = (!"".equals(this.nestedPath) ? rootObject : object);
		this.nestedBeanWrappers = null;
		setIntrospectionClass(object.getClass());
	}

	public Object getWrappedInstance() {
		return this.object;
	}

	public Class getWrappedClass() {
		return this.object.getClass();
	}

	/**
	 * Return the nested path of the object wrapped by this BeanWrapper.
	 */
	public String getNestedPath() {
		return this.nestedPath;
	}

	/**
	 * Return the root object at the top of the path of this BeanWrapper.
	 * @see #getNestedPath
	 */
	public Object getRootInstance() {
		return this.rootObject;
	}

	/**
	 * Return the class of the root object at the top of the path of this BeanWrapper.
	 * @see #getNestedPath
	 */
	public Class getRootClass() {
		return (this.rootObject != null ? this.rootObject.getClass() : null);
	}

	/**
	 * Set the class to introspect.
	 * Needs to be called when the target object changes.
	 * @param clazz the class to introspect
	 */
	protected void setIntrospectionClass(Class clazz) {
		if (this.cachedIntrospectionResults == null ||
				!this.cachedIntrospectionResults.getBeanClass().equals(clazz)) {
			this.cachedIntrospectionResults = CachedIntrospectionResults.forClass(clazz);
		}
	}


	public void registerCustomEditor(Class requiredType, PropertyEditor propertyEditor) {
		registerCustomEditor(requiredType, null, propertyEditor);
	}

	public void registerCustomEditor(Class requiredType, String propertyPath, PropertyEditor propertyEditor) {
		if (requiredType == null && propertyPath == null) {
			throw new IllegalArgumentException("Either requiredType or propertyPath is required");
		}
		if (this.customEditors == null) {
			this.customEditors = new HashMap();
		}
		if (propertyPath != null) {
			this.customEditors.put(propertyPath, new CustomEditorHolder(propertyEditor, requiredType));
		}
		else {
			this.customEditors.put(requiredType, propertyEditor);
		}
	}

	public PropertyEditor findCustomEditor(Class requiredType, String propertyPath) {
		if (this.customEditors == null) {
			return null;
		}
		if (propertyPath != null) {
			// check property-specific editor first
			PropertyEditor editor = getCustomEditor(propertyPath, requiredType);
			if (editor == null) {
				List strippedPaths = new LinkedList();
				addStrippedPropertyPaths(strippedPaths, "", propertyPath);
				for (Iterator it = strippedPaths.iterator(); it.hasNext() && editor == null;) {
					String strippedPath = (String) it.next();
					editor = getCustomEditor(strippedPath, requiredType);
				}
			}
			if (editor != null) {
				return editor;
			}
			else if (requiredType == null) {
				requiredType = getPropertyType(propertyPath);
			}
		}
		// no property-specific editor -> check type-specific editor
		return getCustomEditor(requiredType);
	}

	/**
	 * Get custom editor that has been registered for the given property.
	 * @return the custom editor, or null if none specific for this property
	 */
	private PropertyEditor getCustomEditor(String propertyName, Class requiredType) {
		CustomEditorHolder holder = (CustomEditorHolder) this.customEditors.get(propertyName);
		return (holder != null ? holder.getPropertyEditor(requiredType) : null);
	}

	/**
	 * Get custom editor for the given type. If no direct match found,
	 * try custom editor for superclass (which will in any case be able
	 * to render a value as String via <code>getAsText</code>).
	 * @return the custom editor, or null if none found for this type
	 * @see PropertyEditor#getAsText
	 */
	private PropertyEditor getCustomEditor(Class requiredType) {
		if (requiredType != null) {
			PropertyEditor editor = (PropertyEditor) this.customEditors.get(requiredType);
			if (editor == null) {
				for (Iterator it = this.customEditors.keySet().iterator(); it.hasNext();) {
					Object key = it.next();
					if (key instanceof Class && ((Class) key).isAssignableFrom(requiredType)) {
						editor = (PropertyEditor) this.customEditors.get(key);
					}
				}
			}
			return editor;
		}
		return null;
	}


	/**
	 * Add property paths with all variations of stripped keys and/or indexes.
	 * Invokes itself recursively with nested paths
	 * @param strippedPaths the result list to add to
	 * @param nestedPath the current nested path
	 * @param propertyPath the property path to check for keys/indexes to strip
	 */
	private void addStrippedPropertyPaths(List strippedPaths, String nestedPath, String propertyPath) {
		int startIndex = propertyPath.indexOf(PROPERTY_KEY_PREFIX_CHAR);
		if (startIndex != -1) {
			int endIndex = propertyPath.indexOf(PROPERTY_KEY_SUFFIX_CHAR);
			if (endIndex != -1) {
				String prefix = propertyPath.substring(0, startIndex);
				String key = propertyPath.substring(startIndex, endIndex + 1);
				String suffix = propertyPath.substring(endIndex + 1, propertyPath.length());
				// strip the first key
				strippedPaths.add(nestedPath + prefix + suffix);
				// search for further keys to strip, with the first key stripped
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix, suffix);
				// search for further keys to strip, with the first key not stripped
				addStrippedPropertyPaths(strippedPaths, nestedPath + prefix + key, suffix);
			}
		}
	}

	/**
	 * Determine the first (or last) nested property separator in the
	 * given property path, ignoring dots in keys (like "map[my.key]").
	 * @param propertyPath the property path to check
	 * @param last whether to return the last separator rather than the first
	 * @return the index of the nested property separator, or -1 if none
	 */
	private int getNestedPropertySeparatorIndex(String propertyPath, boolean last) {
		boolean inKey = false;
		int i = (last ? propertyPath.length() - 1 : 0);
		while ((last && i >= 0) || i < propertyPath.length()) {
			switch (propertyPath.charAt(i)) {
				case PROPERTY_KEY_PREFIX_CHAR:
				case PROPERTY_KEY_SUFFIX_CHAR:
					inKey = !inKey;
					break;
				case NESTED_PROPERTY_SEPARATOR_CHAR:
					if (!inKey) {
						return i;
					}
			}
			if (last)
				i--;
			else
				i++;
		}
		return -1;
	}

	/**
	 * Get the last component of the path. Also works if not nested.
	 * @param bw BeanWrapper to work on
	 * @param nestedPath property path we know is nested
	 * @return last component of the path (the property on the target bean)
	 */
	private String getFinalPath(BeanWrapper bw, String nestedPath) {
		if (bw == this) {
			return nestedPath;
		}
		return nestedPath.substring(getNestedPropertySeparatorIndex(nestedPath, true) + 1);
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	protected BeanWrapperImpl getBeanWrapperForPropertyPath(String propertyPath) throws BeansException {
		int pos = getNestedPropertySeparatorIndex(propertyPath, false);
		// handle nested properties recursively
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			BeanWrapperImpl nestedBw = getNestedBeanWrapper(nestedProperty);
			return nestedBw.getBeanWrapperForPropertyPath(nestedPath);
		}
		else {
			return this;
		}
	}

	/**
	 * Retrieve a BeanWrapper for the given nested property.
	 * Create a new one if not found in the cache.
	 * <p>Note: Caching nested BeanWrappers is necessary now,
	 * to keep registered custom editors for nested properties.
	 * @param nestedProperty property to create the BeanWrapper for
	 * @return the BeanWrapper instance, either cached or newly created
	 */
	private BeanWrapperImpl getNestedBeanWrapper(String nestedProperty) throws BeansException {
		if (this.nestedBeanWrappers == null) {
			this.nestedBeanWrappers = new HashMap();
		}
		// get value of bean property
		PropertyTokenHolder tokens = getPropertyNameTokens(nestedProperty);
		Object propertyValue = getPropertyValue(tokens);
		String canonicalName = tokens.canonicalName;
		String propertyName = tokens.actualName;
		if (propertyValue == null) {
			throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + canonicalName);
		}

		// lookup cached sub-BeanWrapper, create new one if not found
		BeanWrapperImpl nestedBw = (BeanWrapperImpl) this.nestedBeanWrappers.get(canonicalName);
		if (nestedBw == null || nestedBw.getWrappedInstance() != propertyValue) {
			if (logger.isDebugEnabled()) {
				logger.debug("Creating new nested BeanWrapper for property '" + canonicalName + "'");
			}
			nestedBw =
					new BeanWrapperImpl(propertyValue, this.nestedPath + canonicalName + NESTED_PROPERTY_SEPARATOR, this);
			// inherit all type-specific PropertyEditors
			if (this.customEditors != null) {
				for (Iterator it = this.customEditors.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					if (entry.getKey() instanceof Class) {
						Class requiredType = (Class) entry.getKey();
						PropertyEditor editor = (PropertyEditor) entry.getValue();
						nestedBw.registerCustomEditor(requiredType, editor);
					}
					else if (entry.getKey() instanceof String) {
						String editorPath = (String) entry.getKey();
						int pos = getNestedPropertySeparatorIndex(editorPath, false);
						if (pos != -1) {
							String editorNestedProperty = editorPath.substring(0, pos);
							String editorNestedPath = editorPath.substring(pos + 1);
							if (editorNestedProperty.equals(canonicalName) || editorNestedProperty.equals(propertyName)) {
								CustomEditorHolder editorHolder = (CustomEditorHolder) entry.getValue();
								nestedBw.registerCustomEditor(
										editorHolder.getRegisteredType(), editorNestedPath, editorHolder.getPropertyEditor());
							}
						}
					}
				}
			}
			this.nestedBeanWrappers.put(canonicalName, nestedBw);
		}
		else {
			if (logger.isDebugEnabled()) {
				logger.debug("Using cached nested BeanWrapper for property '" + canonicalName + "'");
			}
		}
		return nestedBw;
	}

	private PropertyTokenHolder getPropertyNameTokens(String propertyName) {
		PropertyTokenHolder tokens = new PropertyTokenHolder();
		String actualName = null;
		List keys = new ArrayList(2);
		int searchIndex = 0;
		while (searchIndex != -1) {
			int keyStart = propertyName.indexOf(PROPERTY_KEY_PREFIX, searchIndex);
			searchIndex = -1;
			if (keyStart != -1) {
				int keyEnd = propertyName.indexOf(PROPERTY_KEY_SUFFIX, keyStart + PROPERTY_KEY_PREFIX.length());
				if (keyEnd != -1) {
					if (actualName == null) {
						actualName = propertyName.substring(0, keyStart);
					}
					String key = propertyName.substring(keyStart + PROPERTY_KEY_PREFIX.length(), keyEnd);
					if (key.startsWith("'") && key.endsWith("'")) {
						key = key.substring(1, key.length() - 1);
					}
					else if (key.startsWith("\"") && key.endsWith("\"")) {
						key = key.substring(1, key.length() - 1);
					}
					keys.add(key);
					searchIndex = keyEnd + PROPERTY_KEY_SUFFIX.length();
				}
			}
		}
		tokens.actualName = (actualName != null ? actualName : propertyName);
		tokens.canonicalName = tokens.actualName;
		if (!keys.isEmpty()) {
			tokens.canonicalName +=
					PROPERTY_KEY_PREFIX +
					StringUtils.collectionToDelimitedString(keys, PROPERTY_KEY_SUFFIX + PROPERTY_KEY_PREFIX) +
					PROPERTY_KEY_SUFFIX;
			tokens.keys = (String[]) keys.toArray(new String[keys.size()]);
		}
		return tokens;
	}


	public Object getPropertyValue(String propertyName) throws BeansException {
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		return nestedBw.getPropertyValue(tokens);
	}

	protected Object getPropertyValue(PropertyTokenHolder tokens) throws BeansException {
		String propertyName = tokens.canonicalName;
		String actualName = tokens.actualName;
		PropertyDescriptor pd = getPropertyDescriptorInternal(tokens.actualName);
		if (pd == null || pd.getReadMethod() == null) {
			throw new NotReadablePropertyException(getRootClass(), this.nestedPath + propertyName);
		}
		if (logger.isDebugEnabled())
			logger.debug("About to invoke read method [" + pd.getReadMethod() + "] on object of class [" +
					this.object.getClass().getName() + "]");
		try {
			Object value = pd.getReadMethod().invoke(this.object, (Object[]) null);
			if (tokens.keys != null) {
				// apply indexes and map keys
				for (int i = 0; i < tokens.keys.length; i++) {
					String key = tokens.keys[i];
					if (value == null) {
						throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
								"Cannot access indexed value of property referenced in indexed " +
								"property path '" + propertyName + "': returned null");
					}
					else if (value.getClass().isArray()) {
						value = Array.get(value, Integer.parseInt(key));
					}
					else if (value instanceof List) {
						List list = (List) value;
						value = list.get(Integer.parseInt(key));
					}
					else if (value instanceof Set) {
						// apply index to Iterator in case of a Set
						Set set = (Set) value;
						int index = Integer.parseInt(key);
						if (index < 0 || index >= set.size()) {
							throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
									"Cannot get element with index " + index + " from Set of size " +
									set.size() + ", accessed using property path '" + propertyName + "'");
						}
						Iterator it = set.iterator();
						for (int j = 0; it.hasNext(); j++) {
							Object elem = it.next();
							if (j == index) {
								value = elem;
								break;
							}
						}
					}
					else if (value instanceof Map) {
						Map map = (Map) value;
						value = map.get(key);
					}
					else {
						throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
								"Property referenced in indexed property path '" + propertyName +
								"' is neither an array nor a List nor a Set nor a Map; returned value was [" + value + "]");
					}
				}
			}
			return value;
		}
		catch (InvocationTargetException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Getter for property '" + actualName + "' threw exception", ex);
		}
		catch (IllegalAccessException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Illegal attempt to get property '" + actualName + "' threw exception", ex);
		}
		catch (IndexOutOfBoundsException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Index of out of bounds in property path '" + propertyName + "'", ex);
		}
		catch (NumberFormatException ex) {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"Invalid index in property path '" + propertyName + "'", ex);
		}
	}

	public void setPropertyValue(String propertyName, Object value) throws BeansException {
		BeanWrapperImpl nestedBw = null;
		try {
			nestedBw = getBeanWrapperForPropertyPath(propertyName);
		}
		catch (NotReadablePropertyException ex) {
			throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
					"Nested property in path '" + propertyName + "' does not exist", ex);
		}
		PropertyTokenHolder tokens = getPropertyNameTokens(getFinalPath(nestedBw, propertyName));
		nestedBw.setPropertyValue(tokens, value);
	}

	protected void setPropertyValue(PropertyTokenHolder tokens, Object value) throws BeansException {
		String propertyName = tokens.canonicalName;

		if (tokens.keys != null) {
			// apply indexes and map keys: fetch value for all keys but the last one
			PropertyTokenHolder getterTokens = new PropertyTokenHolder();
			getterTokens.canonicalName = tokens.canonicalName;
			getterTokens.actualName = tokens.actualName;
			getterTokens.keys = new String[tokens.keys.length - 1];
			System.arraycopy(tokens.keys, 0, getterTokens.keys, 0, tokens.keys.length - 1);
			Object propValue = null;
			try {
				propValue = getPropertyValue(getterTokens);
			}
			catch (NotReadablePropertyException ex) {
				throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName,
						"Cannot access indexed value in property referenced " +
						"in indexed property path '" + propertyName + "'", ex);
			}
			// set value for last key
			String key = tokens.keys[tokens.keys.length - 1];
			if (propValue == null) {
				throw new NullValueInNestedPathException(getRootClass(), this.nestedPath + propertyName,
						"Cannot access indexed value in property referenced " +
						"in indexed property path '" + propertyName + "': returned null");
			}
			else if (propValue.getClass().isArray()) {
				Class requiredType = propValue.getClass().getComponentType();
				Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, requiredType);
				try {
					Array.set(propValue, Integer.parseInt(key), newValue);
				}
				catch (IllegalArgumentException ex) {
					PropertyChangeEvent pce =
							new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, null, newValue);
					throw new TypeMismatchException(pce, requiredType, ex);
				}
				catch (IndexOutOfBoundsException ex) {
					throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
							"Invalid array index in property path '" + propertyName + "'", ex);
				}
			}
			else if (propValue instanceof List) {
				Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, null);
				List list = (List) propValue;
				int index = Integer.parseInt(key);
				if (index < list.size()) {
					list.set(index, newValue);
				}
				else if (index >= list.size()) {
					for (int i = list.size(); i < index; i++) {
						try {
							list.add(null);
						}
						catch (NullPointerException ex) {
							throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
									"Cannot set element with index " + index + " in List of size " +
									list.size() + ", accessed using property path '" + propertyName +
									"': List does not support filling up gaps with null elements");
						}
					}
					list.add(newValue);
				}
			}
			else if (propValue instanceof Map) {
				Object newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, null);
				Map map = (Map) propValue;
				map.put(key, newValue);
			}
			else {
				throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
						"Property referenced in indexed property path '" + propertyName +
						"' is neither an array nor a List nor a Map; returned value was [" + value + "]");
			}
		}
		else {
			if (!isWritableProperty(propertyName)) {
				throw new NotWritablePropertyException(getRootClass(), this.nestedPath + propertyName);
			}
			PropertyDescriptor pd = getPropertyDescriptor(propertyName);
			Method writeMethod = pd.getWriteMethod();
			Object newValue = null;
			try {
				// old value may still be null
				newValue = doTypeConversionIfNecessary(propertyName, propertyName, null, value, pd.getPropertyType());

				if (pd.getPropertyType().isPrimitive() && (newValue == null || "".equals(newValue))) {
					throw new IllegalArgumentException("Invalid value [" + value + "] for property '" +
							pd.getName() + "' of primitive type [" + pd.getPropertyType() + "]");
				}

				if (logger.isDebugEnabled()) {
					logger.debug("About to invoke write method [" + writeMethod + "] on object of class [" +
							this.object.getClass().getName() + "]");
				}
				writeMethod.invoke(this.object, new Object[]{newValue});
				if (logger.isDebugEnabled()) {
					String msg = "Invoked write method [" + writeMethod + "] with value ";
					// only cause toString invocation of new value in case of simple property
					if (newValue == null || BeanUtils.isSimpleProperty(pd.getPropertyType())) {
						logger.debug(msg + PROPERTY_KEY_PREFIX + newValue + PROPERTY_KEY_SUFFIX);
					}
					else {
						logger.debug(msg + "of type [" + pd.getPropertyType().getName() + "]");
					}
				}
			}
			catch (InvocationTargetException ex) {
				PropertyChangeEvent propertyChangeEvent =
						new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, null, value);
				if (ex.getTargetException() instanceof ClassCastException) {
					throw new TypeMismatchException(propertyChangeEvent, pd.getPropertyType(), ex.getTargetException());
				}
				else {
					throw new MethodInvocationException(propertyChangeEvent, ex.getTargetException());
				}
			}
			catch (IllegalArgumentException ex) {
				PropertyChangeEvent pce =
						new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, null, value);
				throw new TypeMismatchException(pce, pd.getPropertyType(), ex);
			}
			catch (IllegalAccessException ex) {
				PropertyChangeEvent pce =
						new PropertyChangeEvent(this.rootObject, this.nestedPath + propertyName, null, value);
				throw new MethodInvocationException(pce, ex);
			}
		}
	}

	public void setPropertyValue(PropertyValue pv) throws BeansException {
		setPropertyValue(pv.getName(), pv.getValue());
	}

	/**
	 * Bulk update from a Map.
	 * Bulk updates from PropertyValues are more powerful: this method is
	 * provided for convenience.
	 * @param map map containing properties to set, as name-value pairs.
	 * The map may include nested properties.
	 * @throws BeansException if there's a fatal, low-level exception
	 */
	public void setPropertyValues(Map map) throws BeansException {
		setPropertyValues(new MutablePropertyValues(map));
	}

	public void setPropertyValues(PropertyValues pvs) throws BeansException {
		setPropertyValues(pvs, false);
	}

	public void setPropertyValues(PropertyValues propertyValues, boolean ignoreUnknown) throws BeansException {
		List propertyAccessExceptions = new ArrayList();
		PropertyValue[] pvs = propertyValues.getPropertyValues();
		for (int i = 0; i < pvs.length; i++) {
			try {
				// This method may throw any BeansException, which won't be caught
				// here, if there is a critical failure such as no matching field.
				// We can attempt to deal only with less serious exceptions.
				setPropertyValue(pvs[i]);
			}
			catch (NotWritablePropertyException ex) {
				if (!ignoreUnknown) {
					throw ex;
				}
				// otherwise, just ignore it and continue...
			}
			catch (PropertyAccessException ex) {
				propertyAccessExceptions.add(ex);
			}
		}

		// If we encountered individual exceptions, throw the composite exception.
		if (!propertyAccessExceptions.isEmpty()) {
			Object[] paeArray =
					propertyAccessExceptions.toArray(new PropertyAccessException[propertyAccessExceptions.size()]);
			throw new PropertyAccessExceptionsException(this, (PropertyAccessException[]) paeArray);
		}
	}

	private PropertyChangeEvent createPropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
		return new PropertyChangeEvent((this.rootObject != null ? this.rootObject : "constructor"),
				(propertyName != null ? this.nestedPath + propertyName : null),
				oldValue, newValue);
	}

	/**
	 * Convert the value to the required type (if necessary from a String).
	 * Conversions from String to any type use the <code>setAsText</code> method
	 * of the PropertyEditor class. Note that a PropertyEditor must be registered
	 * for the given class for this to work. This is a standard Java Beans API.
	 * A number of property editors are automatically registered by this class.
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * @return the new value, possibly the result of type conversion
	 * @throws TypeMismatchException if type conversion failed
	 * @see PropertyEditor#setAsText(String)
	 */
	public Object doTypeConversionIfNecessary(Object newValue, Class requiredType) throws TypeMismatchException {
		return doTypeConversionIfNecessary(null, null, null, newValue, requiredType);
	}

	/**
	 * Convert the value to the required type (if necessary from a String),
	 * for the specified property.
	 * @param propertyName name of the property
	 * @param oldValue previous value, if available (may be null)
	 * @param newValue proposed change value
	 * @param requiredType the type we must convert to
	 * (or null if not known, for example in case of a collection element)
	 * @return the new value, possibly the result of type conversion
	 * @throws TypeMismatchException if type conversion failed
	 */
	protected Object doTypeConversionIfNecessary(String propertyName, String fullPropertyName,
			Object oldValue, Object newValue, Class requiredType) throws TypeMismatchException {

		Object convertedValue = newValue;
		if (convertedValue != null) {

			// Custom editor for this type?
			PropertyEditor pe = findCustomEditor(requiredType, fullPropertyName);

			// Value not of required type?
			if (pe != null ||
					(requiredType != null &&
					(requiredType.isArray() || !requiredType.isAssignableFrom(convertedValue.getClass())))) {

				if (requiredType != null) {
					if (pe == null) {
						// No custom editor -> check BeanWrapperImpl's default editors.
						pe = (PropertyEditor) this.defaultEditors.get(requiredType);
						if (pe == null) {
							// No BeanWrapper default editor -> check standard JavaBean editors.
							pe = PropertyEditorManager.findEditor(requiredType);
						}
					}
				}

				if (pe != null && !(convertedValue instanceof String)) {
					// Not a String -> use PropertyEditor's setValue.
					// With standard PropertyEditors, this will return the very same object;
					// we just want to allow special PropertyEditors to override setValue
					// for type conversion from non-String values to the required type.
					try {
						pe.setValue(convertedValue);
						convertedValue = pe.getValue();
					}
					catch (IllegalArgumentException ex) {
						throw new TypeMismatchException(
								createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType, ex);
					}
				}

				if (requiredType != null && !requiredType.isArray() && convertedValue instanceof String[]) {
					// Convert String array to a comma-separated String.
					// Only applies if no PropertyEditor converted the String array before.
					// The CSV String will be passed into a PropertyEditor's setAsText method, if any.
					if (logger.isDebugEnabled()) {
						logger.debug("Converting String array to comma-delimited String [" + convertedValue + "]");
					}
					convertedValue = StringUtils.arrayToCommaDelimitedString((String[]) convertedValue);
				}

				if (pe != null && convertedValue instanceof String) {
					// Use PropertyEditor's setAsText in case of a String value.
					if (logger.isDebugEnabled()) {
						logger.debug("Converting String to [" + requiredType + "] using property editor [" + pe + "]");
					}
					try {
						pe.setAsText((String) convertedValue);
						convertedValue = pe.getValue();
					}
					catch (IllegalArgumentException ex) {
						throw new TypeMismatchException(
								createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType, ex);
					}
				}

				if (requiredType != null) {
					// Array required -> apply appropriate conversion of elements.
					if (requiredType.isArray()) {
						Class componentType = requiredType.getComponentType();
						if (convertedValue instanceof Collection) {
							// Convert Collection elements to array elements.
							Collection coll = (Collection) convertedValue;
							Object result = Array.newInstance(componentType, coll.size());
							int i = 0;
							for (Iterator it = coll.iterator(); it.hasNext(); i++) {
								Object value = doTypeConversionIfNecessary(
										propertyName, propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
										null, it.next(), componentType);
								Array.set(result, i, value);
							}
							return result;
						}
						else if (convertedValue != null && convertedValue.getClass().isArray()) {
							// Convert Collection elements to array elements.
							int arrayLength = Array.getLength(convertedValue);
							Object result = Array.newInstance(componentType, arrayLength);
							for (int i = 0; i < arrayLength; i++) {
								Object value = doTypeConversionIfNecessary(
										propertyName, propertyName + PROPERTY_KEY_PREFIX + i + PROPERTY_KEY_SUFFIX,
										null, Array.get(convertedValue, i), componentType);
								Array.set(result, i, value);
							}
							return result;
						}
						else {
							// A plain value: convert it to an array with a single component.
							Object result = Array.newInstance(componentType, 1);
							Object val = doTypeConversionIfNecessary(
									propertyName, propertyName + PROPERTY_KEY_PREFIX + 0 + PROPERTY_KEY_SUFFIX,
									null, convertedValue, componentType);
							Array.set(result, 0, val);
							return result;
						}
					}

					// Throw explicit TypeMismatchException with full context information
					// if the resulting value definitely doesn't match the required type.
					if (convertedValue != null && !requiredType.isPrimitive() &&
							!requiredType.isAssignableFrom(convertedValue.getClass())) {
						throw new TypeMismatchException(
								createPropertyChangeEvent(fullPropertyName, oldValue, newValue), requiredType);
					}
				}
			}
		}

		return convertedValue;
	}


	public PropertyDescriptor[] getPropertyDescriptors() {
		return this.cachedIntrospectionResults.getBeanInfo().getPropertyDescriptors();
	}

	public PropertyDescriptor getPropertyDescriptor(String propertyName) throws BeansException {
		if (propertyName == null) {
			throw new IllegalArgumentException("Can't find property descriptor for null property");
		}
		PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
		if (pd != null) {
			return pd;
		}
		else {
			throw new InvalidPropertyException(getRootClass(), this.nestedPath + propertyName,
					"No property '" + propertyName + "' found");
		}
	}

	/**
	 * Internal version of getPropertyDescriptor:
	 * Returns null if not found rather than throwing an exception.
	 */
	protected PropertyDescriptor getPropertyDescriptorInternal(String propertyName) throws BeansException {
		Assert.state(this.object != null, "BeanWrapper does not hold a bean instance");
		BeanWrapperImpl nestedBw = getBeanWrapperForPropertyPath(propertyName);
		return nestedBw.cachedIntrospectionResults.getPropertyDescriptor(getFinalPath(nestedBw, propertyName));
	}

	public Class getPropertyType(String propertyName) throws BeansException {
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				return pd.getPropertyType();
			}
			else {
				// maybe an indexed/mapped property
				Object value = getPropertyValue(propertyName);
				if (value != null) {
					return value.getClass();
				}
			}
		}
		catch (InvalidPropertyException ex) {
			// consider as not determinable
		}
		return null;
	}

	public boolean isReadableProperty(String propertyName) {
		// This is a programming error, although asking for a property
		// that doesn't exist is not.
		if (propertyName == null) {
			throw new IllegalArgumentException("Can't find readability status for null property");
		}
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getReadMethod() != null) {
					return true;
				}
			}
			else {
				// maybe an indexed/mapped property
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// cannot be evaluated, so can't be readable
		}
		return false;
	}

	public boolean isWritableProperty(String propertyName) {
		// This is a programming error, although asking for a property
		// that doesn't exist is not.
		if (propertyName == null) {
			throw new IllegalArgumentException("Can't find writability status for null property");
		}
		try {
			PropertyDescriptor pd = getPropertyDescriptorInternal(propertyName);
			if (pd != null) {
				if (pd.getWriteMethod() != null) {
					return true;
				}
			}
			else {
				// maybe an indexed/mapped property
				getPropertyValue(propertyName);
				return true;
			}
		}
		catch (InvalidPropertyException ex) {
			// cannot be evaluated, so can't be writable
		}
		return false;
	}


	//---------------------------------------------------------------------
	// Diagnostics
	//---------------------------------------------------------------------

	public String toString() {
		StringBuffer sb = new StringBuffer("BeanWrapperImpl: wrapping class [");
		sb.append(getWrappedClass().getName()).append("]");
		return sb.toString();
	}

	/**
	 * Holder for a registered custom editor with property name.
	 * Keeps the PropertyEditor itself plus the type it was registered for.
	 */
	private static class CustomEditorHolder {

		private final PropertyEditor propertyEditor;

		private final Class registeredType;

		private CustomEditorHolder(PropertyEditor propertyEditor, Class registeredType) {
			this.propertyEditor = propertyEditor;
			this.registeredType = registeredType;
		}

		private PropertyEditor getPropertyEditor() {
			return propertyEditor;
		}

		private Class getRegisteredType() {
			return registeredType;
		}

		private PropertyEditor getPropertyEditor(Class requiredType) {
			// Special case: If no required type specified, which usually only happens for
			// Collection elements, or required type is not assignable to registered type,
			// which usually only happens for generic properties of type Object -
			// then return PropertyEditor if not registered for Collection or array type.
			// (If not registered for Collection or array, it is assumed to be intended
			// for elements.)
			if (this.registeredType == null ||
					(requiredType != null &&
					(BeanUtils.isAssignable(this.registeredType, requiredType) ||
					BeanUtils.isAssignable(requiredType, this.registeredType))) ||
					(requiredType == null &&
					(!Collection.class.isAssignableFrom(this.registeredType) && !this.registeredType.isArray()))) {
				return this.propertyEditor;
			}
			else {
				return null;
			}
		}
	}


	private static class PropertyTokenHolder {

		private String canonicalName;

		private String actualName;

		private String[] keys;
	}

}
