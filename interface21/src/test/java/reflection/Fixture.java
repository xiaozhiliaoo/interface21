package reflection;/*
package reflection;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.interface21.context.support.AbstractApplicationContext;
import com.interface21.load.AbstractTest;
import com.interface21.web.servlet.mvc.AbstractCommandController;
import com.wrox.expertj2ee.ticket.web.TicketController;

public class Fixture {

	private String[] classNames;

	private Class[] classes;
	
	*/
/** Class -> object *//*

	private HashMap objectHash;

	private HashMap fooMethodHash;
 
	public void setClassNames(String[] names) throws Exception {
		this.classNames = names;
		classes = new Class[names.length];
		fooMethodHash = new HashMap();
		objectHash = new HashMap();
		for (int i = 0; i < names.length; i++) {
			classes[i] = Class.forName(names[i]);
			fooMethodHash.put(classes[i], classes[i].getMethod("foo", new Class[] {int.class}));
			objectHash.put(classes[i], classes[i].newInstance());
		}
	}

	protected String randomClassName() {
		return classNames[AbstractTest.randomIndex(classNames.length)];
	}

	protected Class randomClass() {
		return classes[AbstractTest.randomIndex(classes.length)];
	}

	protected Method fooMethod(Class clazz) {
		return (Method) fooMethodHash.get(clazz);
	}
	
	protected Object getObject(Class clazz) {
		return objectHash.get(clazz);
	}
	
	
	public static class TestController extends TicketController {
		public String foo(int i) {
			return "This is a string with a number " + i + " in it";
		}
	}
	
	public static class LittleClass {
		public String foo(int i) {
			return "This is a string with a number " + i + " in it";
		}
	}

}*/
