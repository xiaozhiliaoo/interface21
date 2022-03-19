/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.aop;

/**
 * Superinterface for advisors that perform one or more AOP <b>introductions</b>.
 *
 * <p>This interface cannot be implemented directly; subinterfaces must
 * provide the advice type implementing the introduction.
 *
 * <p>Introduction is the implementation of additional interfaces
 * (not implemented by a target) via AOP advice.
 *
 * @author Rod Johnson
 * @since 04-Apr-2003
 * @version $Id: IntroductionAdvisor.java,v 1.4 2004/04/01 15:35:45 jhoeller Exp $
 * @see org.springframework.aop.IntroductionInterceptor
 */
public interface IntroductionAdvisor extends Advisor {
	
	/**
	 * Return the filter determining which target classes this introduction
	 * should apply to. The class part of a pointcut. Note that method
	 * matching doesn't make sense to introductions.
	 */
	ClassFilter getClassFilter();
	
	/**
	 * Return the additional interfaces introduced by this Advisor.
	 */
	Class[] getInterfaces();
	
	/**
	 * Can the advised interfaces be implemented by the 
	 * introduction advice? Invoked before adding an IntroductionAdvisor.
	 * @throws IllegalArgumentException if the advised interfaces can't be
	 * implemented by the introduction advice.
	 */
	void validateInterfaces() throws IllegalArgumentException;

}
