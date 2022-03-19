package com.interface21.beans.factory.support;

import com.interface21.beans.PropertyValues;

/** 
	 * Child beans have properties and a parent.
	 * They do not define a class: this can be obtained by navigating
	 * to their parent. Any depth of ancestry(祖先) is permitted.
	 */
public class ChildBeanDefinitionImpl extends AbstractBeanDefinition implements ChildBeanDefinition {

	private String parentName;

	/** Creates new BeanDefinition */
	public ChildBeanDefinitionImpl(String parentName, PropertyValues pvs, boolean singleton) {
		super(pvs, singleton);
		this.parentName = parentName;
	}

	public String getParentName() {
		return parentName;
	}
} 