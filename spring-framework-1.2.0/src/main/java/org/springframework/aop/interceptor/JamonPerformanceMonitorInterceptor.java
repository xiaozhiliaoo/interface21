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

package org.springframework.aop.interceptor;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;

/**
 * Performance monitor interceptor that uses <b>JAMon</b> library
 * to perform the performance measurement on the intercepted method
 * and output the stats.
 *
 * <p>This code is inspired by Thierry Templier's blog.
 *
 * @author Dmitriy Kopylenko
 * @author Juergen Hoeller
 * @since 1.1.3
 * @see com.jamonapi.MonitorFactory
 * @see PerformanceMonitorInterceptor
 */
public class JamonPerformanceMonitorInterceptor extends AbstractTraceInterceptor {

	/**
	 * Create a new JamonPerformanceMonitorInterceptor with a static logger.
	 */
	public JamonPerformanceMonitorInterceptor() {
	}

	/**
	 * Create a new JamonPerformanceMonitorInterceptor with a dynamic or static logger,
	 * according to the given flag.
	 * @param useDynamicLogger whether to use a dynamic logger or a static logger
	 * @see #setUseDynamicLogger
	 */
	public JamonPerformanceMonitorInterceptor(boolean useDynamicLogger) {
		setUseDynamicLogger(useDynamicLogger);
	}

	protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {
		String name = invocation.getMethod().getDeclaringClass().getName() + "." + invocation.getMethod().getName();
		Monitor monitor = MonitorFactory.start(name);
		try {
			return invocation.proceed();
		}
		finally {
			monitor.stop();
			logger.trace("JAMon performance statistics for method [" + name + "]:\n" + monitor);
		}
	}

}
