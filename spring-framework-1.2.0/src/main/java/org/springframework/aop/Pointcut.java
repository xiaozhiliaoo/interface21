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

package org.springframework.aop;


/**
 * Core Spring pointcut abstraction. A pointcut is composed of ClassFilters and MethodMatchers.
 * Both these basic terms and a Pointcut itself can be combined to build up combinations.
 * @author Rod Johnson
 */
public interface Pointcut {

	ClassFilter getClassFilter();
	
	MethodMatcher getMethodMatcher();
	
	// could add getFieldMatcher() without breaking most existing code
	
	Pointcut TRUE = TruePointcut.INSTANCE; 

}
