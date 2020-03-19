package com.interface21.load;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.interface21.beans.BeansException;
import com.interface21.beans.factory.support.ListableBeanFactoryImpl;

public class PropertiesTestSuiteLoader  {
	
	public static void main(String[] args) {
		String file = "c:\\work\\book\\ticket\\framework\\test\\com\\interface21\\load\\test.properties";	
		if (args.length > 0)
			file = args[0];
		
		ListableBeanFactoryImpl lbf = new ListableBeanFactoryImpl();
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
			
			System.out.println("Loading properties file '" + file + "'. Looking for bean definitions...");
			
			lbf.registerBeanDefinitions(props, null);
			
			// Get the BeanFactoryTestSuite instance
			BeanFactoryTestSuite pst = (BeanFactoryTestSuite) lbf.getBean("suite");
			
			// Give it access to the other beans
			pst.init(lbf);
			
			// Run the tests
			pst.runAllTests(true);
			
		}
		catch (IOException ex) {
			System.out.println("Cannot find properties file: " + ex);
		}
		catch (BeansException ex) {
			System.out.println("Cannot find bean: " + ex);
			ex.printStackTrace();
		}
	}

}