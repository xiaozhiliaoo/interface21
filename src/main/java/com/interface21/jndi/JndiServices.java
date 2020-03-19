package com.interface21.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import java14.java.util.logging.Logger;


/**
 * Simple helper for JNDI lookup
 * @author Rod Johnson
 * @version
 */
public class JndiServices {
	
	/**
	* Create a logging category that is available
	* to subclasses. 
	*/
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private ContextFactory contextFactory;
	
	public JndiServices() {
		this.contextFactory = new DefaultContextFactory();
	}
	
	public JndiServices(ContextFactory contextFactory) {
		this.contextFactory = contextFactory;
	}
	
	public void bind(final String name, final Object object) throws NamingException {
		execute(new ContextCallback() {
			public void doInContext(Context ctx) throws NamingException {
				ctx.bind(name, object);
				logger.info("Bound JNDI object with name " + name);
			}
		});
	}
	
	public void unbind(final String name) throws NamingException {
		execute(new ContextCallback() {
			public void doInContext(Context ctx) throws NamingException {
				ctx.unbind(name);
				logger.info("Unbound JNDI object with name " + name);
			}
		});
	}
	
	/**
	 * If this returns null, caller should deal with it
	 */
	public Object lookup(final String name) throws NamingException {
		class LookupCallback implements ContextCallback {
			private Object o;
			
			public void doInContext(Context ctx) throws NamingException {
				o = ctx.lookup(name);
				logger.fine("Looked up JNDI object with name '" + name + "'");
			}
			
			public Object getObject() {
				return o;
			}
		}
		LookupCallback lc = new LookupCallback();
		execute(lc);
		return lc.getObject();
	}
	
	
	/** Open up what is done so clients can provide a context callback
	 */
	public void execute(ContextCallback cc) throws NamingException {
		Context ctx = null;
		
		try {
			ctx = contextFactory.getContext();
			cc.doInContext(ctx);
		}
		// catch (NamingException ex) {
		//    throw new NamingException(("Couldn't bind JNDI object to name [" + name + "]", ex);
		// }
		finally {
			try {
				if (ctx != null)
					ctx.close();
			}
			catch (NamingException ex) {
				//
			}
		}
	}   // execute
	
	
	private class DefaultContextFactory implements ContextFactory {
		public Context getContext() throws NamingException {
			//Hashtable m = new Hashtable();
			//m.put(weblogic.jndi.WLContext.REPLICATE_BINDINGS, "false");
			//return new InitialContext(m);
			return new InitialContext();
		}
	}
	
}
