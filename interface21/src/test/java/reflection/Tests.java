package reflection;/*

package reflection;

import com.interface21.load.AbstractTest;

import java.beans.Introspector;
import java.lang.reflect.Method;

public class Tests  {
		
		public abstract static class BaseTest extends AbstractTest {
			protected Fixture fixture;
			
			public void setFixture(Object fixture) {
				this.fixture = (Fixture) fixture;
			}
		}
		
		
		public static class ClassForName extends BaseTest {
			protected void runPass(int i) throws Exception {
				Class.forName(fixture.randomClassName());
			}

		}
		
		public static class ClassNewInstance extends BaseTest {
			protected void runPass(int i) throws Exception {
				fixture.randomClass().newInstance();
			}

		}
		
		public static class BeansIntrospect extends BaseTest {
			protected void runPass(int i) throws Exception {
				Introspector.getBeanInfo(fixture.randomClass());
			}

		}
		
		public static class NewNewInstance extends BaseTest {
			protected void runPass(int i) throws Exception {
				new Fixture.TestController();
				//new Fixture.LittleClass();
			}

		}
		
		public static class ClassGetMethods extends BaseTest {
			protected void runPass(int i) throws Exception {
				fixture.randomClass().getMethods();
			}
		}
		
		public static class MethodInvoke extends BaseTest {
			protected void runPass(int i) throws Exception {
				Class clazz = fixture.randomClass();
				Method fooMethod = fixture.fooMethod(clazz);
				Object o = fixture.getObject(clazz);
				fooMethod.invoke(o, new Object[] {new Integer(i) });
			}
		}
		
		public static class DirectInvoke extends BaseTest {
			protected void runPass(int i) throws Exception {
				Class clazz = fixture.randomClass();
				Fixture.TestController tc = (Fixture.TestController) fixture.getObject(clazz);
				//Fixture.LittleClass tc = (Fixture.LittleClass) fixture.getObject(clazz);
				String s = tc.foo(i);
			}
		}

		public static class StringAdd extends BaseTest {
			protected void runPass(int i) throws Exception {
				String s = i + "string" + i;
			}
		}

	}
*/
