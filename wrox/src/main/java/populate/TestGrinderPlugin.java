
package populate;

import java.util.Set;

import net.grinder.common.Test;
import net.grinder.plugininterface.*;
import net.grinder.plugininterface.GrinderPlugin;
import net.grinder.plugininterface.PluginException;
import net.grinder.plugininterface.PluginProcessContext;
import net.grinder.plugininterface.ThreadCallbacks;

public class TestGrinderPlugin implements GrinderPlugin {
	
	private Set testsFromPropertiesFile;

	/**
	 * @see GrinderPlugin#initialize(PluginProcessContext, Set)
	 */
	public void initialize(PluginProcessContext context, Set testsFromPropertiesFile) throws PluginException {
		this.testsFromPropertiesFile = testsFromPropertiesFile;
	}

	/**
	 * @see GrinderPlugin#createThreadCallbackHandler()
	 */
	public ThreadCallbacks createThreadCallbackHandler() throws PluginException {
		return new TestThreadCallbacks();
	}

	/**
	 * @see GrinderPlugin#getTests()
	 */
	public Set getTests() throws PluginException {
		return this.testsFromPropertiesFile;
	}
	
	
	private class TestThreadCallbacks implements ThreadCallbacks {
		
		private PluginThreadContext ctx;
		
		/**
		 * @see ThreadCallbacks#beginCycle()
		 */
		public void beginCycle() throws PluginException {
		}

		/**
		 * @see ThreadCallbacks#doTest(Test)
		 */
		public boolean doTest(Test test) throws PluginException {
			try {
				ctx.startTimer();
				Thread.sleep(45);
				return true;
			}
			catch (Exception ex) {
				return false;
			}
			finally {
				ctx.stopTimer();
			}
		}

		/**
		 * @see ThreadCallbacks#endCycle()
		 */
		public void endCycle() throws PluginException {
			//ctx.
		}

		/**
		 * @see ThreadCallbacks#initialize(PluginThreadContext)
		 */
		public void initialize(PluginThreadContext context) throws PluginException {
			this.ctx = context;
		}

	}

}
