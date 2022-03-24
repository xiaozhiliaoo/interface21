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

package org.springframework.web.servlet.i18n;

import java.util.Locale;

import javax.servlet.http.Cookie;

import junit.framework.TestCase;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * @author Alef Arendsen
 */
public class CookieLocaleResolverTests extends TestCase {

	public void testResolveLocale() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		Cookie cookie = new Cookie("LanguageKoekje", "nl");
		request.setCookies(new Cookie[] {cookie});

		CookieLocaleResolver resolver = new CookieLocaleResolver();
		// yup, koekje is the Dutch name for Cookie ;-)
		resolver.setCookieName("LanguageKoekje");
		Locale loc = resolver.resolveLocale(request);
		assertEquals(loc.getLanguage(), "nl");
	}

	public void testSetAndResolveLocale() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setLocale(request, response, new Locale("nl", ""));

		Cookie cookie = response.getCookie(CookieLocaleResolver.DEFAULT_COOKIE_NAME);
		assertNotNull(cookie);
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_NAME, cookie.getName());
		assertEquals(null, cookie.getDomain());
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_PATH, cookie.getPath());
		assertEquals(CookieLocaleResolver.DEFAULT_COOKIE_MAX_AGE, cookie.getMaxAge());

		request = new MockHttpServletRequest();
		request.setCookies(new Cookie[] {cookie});

		resolver = new CookieLocaleResolver();
		Locale loc = resolver.resolveLocale(request);
		assertEquals(loc.getLanguage(), "nl");
	}

	public void testCustomCookie() {
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();

		CookieLocaleResolver resolver = new CookieLocaleResolver();
		resolver.setCookieName("LanguageKoek");
		resolver.setCookieDomain(".springframework.org");
		resolver.setCookiePath("/mypath");
		resolver.setCookieMaxAge(10000);
		resolver.setLocale(request, response, new Locale("nl", ""));

		Cookie cookie = response.getCookie("LanguageKoek");
		assertNotNull(cookie);
		assertEquals("LanguageKoek", cookie.getName());
		assertEquals(".springframework.org", cookie.getDomain());
		assertEquals("/mypath", cookie.getPath());
		assertEquals(10000, cookie.getMaxAge());

		request = new MockHttpServletRequest();
		request.setCookies(new Cookie[] {cookie});

		resolver = new CookieLocaleResolver();
		resolver.setCookieName("LanguageKoek");
		Locale loc = resolver.resolveLocale(request);
		assertEquals(loc.getLanguage(), "nl");
	}

}
