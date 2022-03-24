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

package org.springframework.web.servlet.view;

import java.util.Map;

import org.springframework.beans.TestBean;

/**
 * used for VTL and FTL macro tests
 * 
 * @author Darren Davison
 * @since 25-Jan-05
 */
public class DummyMacroRequestContext {
    Map msgMap;
    String contextPath;
    TestBean command;

    public String getMessage(String code) {
        return (String) msgMap.get(code);
    }
    public String getMessage(String code, String defaultMsg) {
        String msg = (String) msgMap.get(code);
        return (msg == null) ? defaultMsg : msg; 
    }
    public DummyBindStatus getBindStatus(String path) throws IllegalStateException {
        return new DummyBindStatus();
    }
    public DummyBindStatus getBindStatus(String path, boolean htmlEscape) throws IllegalStateException {
        return new DummyBindStatus();
    }

    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    public void setMsgMap(Map msgMap) {
        this.msgMap = msgMap;
    }
    public TestBean getCommand() {
        return command;
    }
    public void setCommand(TestBean command) {
        this.command = command;
    }
    
    public class DummyBindStatus {      
        public String getExpression() {
            return "name";
        }
        public String getValue() {
            return "Darren";
        }
    }
}

