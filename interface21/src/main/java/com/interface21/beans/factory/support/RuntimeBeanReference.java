package com.interface21.beans.factory.support;

/** 
* Placeholder class used for the value of a PropertyValue
* object when it's a reference to another bean in this
* factory to be resolved at runtime.
*/
public class RuntimeBeanReference {
	private String beanName;

	public RuntimeBeanReference(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return beanName;
	}
}