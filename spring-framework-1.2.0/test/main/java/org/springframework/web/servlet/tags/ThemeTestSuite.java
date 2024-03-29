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

package org.springframework.web.servlet.tags;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import com.mockobjects.servlet.MockPageContext;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.servlet.support.RequestContext;

/**
 * @author Juergen Hoeller
 * @author Alef Arendsen
 */
public class ThemeTestSuite extends AbstractTagTest {

	public void testThemeTag() throws JspException {
		MockPageContext pc = createPageContext();
		final StringBuffer message = new StringBuffer();
		ThemeTag tag = new ThemeTag() {
			protected void writeMessage(String msg) throws IOException {
				message.append(msg);
			}
		};
		tag.setPageContext(pc);
		tag.setCode("themetest");
		assertTrue("Correct doStartTag return value", tag.doStartTag() == Tag.EVAL_BODY_INCLUDE);
		assertEquals("theme test message", message.toString());
	}

	public void testRequestContext() throws ServletException {
		MockPageContext pc = createPageContext();
		RequestContext rc = new RequestContext((HttpServletRequest) pc.getRequest());
		assertEquals("theme test message", rc.getThemeMessage("themetest"));
		assertEquals("theme test message", rc.getThemeMessage("themetest", (String[]) null));
		assertEquals("theme test message", rc.getThemeMessage("themetest", "default"));
		assertEquals("theme test message", rc.getThemeMessage("themetest", null, "default"));
		assertEquals("default", rc.getThemeMessage("themetesta", "default"));
		assertEquals("default", rc.getThemeMessage("themetesta", null, "default"));
		MessageSourceResolvable resolvable = new DefaultMessageSourceResolvable(new String[] {"themetest"}, null);
		assertEquals("theme test message", rc.getThemeMessage(resolvable));
	}

}
