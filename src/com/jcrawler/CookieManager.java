/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: CookieManager.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************//*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: CookieManager.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.log4j.Logger;
import com.jcrawler.util.Crawler;
import com.jcrawler.util.HttpUtil;
import java.util.*;
import java.net.URL;

/**
 *
 * <p>Non-persistent cookie manager class. Cookies are kept
 * in-memory, only while the Crawler runs.</p>
 *
 */
public class CookieManager {

  private static Logger log = Logger.getLogger(CookieManager.class);

  static {
    cookies = new HashMap();
  }

  /** All cookies. Contains pair: domain - <code>list</code>. Where <code>list</code>
   * is a collection of all cookies for that domain */
  private static Map cookies;

  /** Add a cookie to the cookie manager */
  public static void addCookie(Cookie cookie) {
    synchronized (Crawler.cookieWatch) {
      Collection domainCookies = getDomainCookies(cookie.getDomain());
      if (domainCookies == null) {
        domainCookies = Collections.synchronizedSet(new HashSet());
      }

      domainCookies.add(cookie);

      cookies.put(cookie.getDomain(), domainCookies);
    }
    log.debug("Registering cookie " + cookie + " for domain " + cookie.getDomain());
  }

  /** Return all cookies for a particular domain */
  private static Collection getDomainCookies(String domain) {
    return (Collection) cookies.get(domain);
  }

  public static HttpState getHttpState(String urlString) {

    HttpState httpState = new HttpState();

    // We agreed that cookie domains always begin with a dot.
    String domain = null;
    try {
      domain = new URL(urlString).getHost();

      Collection domainCookies;
      Cookie curCookie;

      synchronized (Crawler.cookieWatch) {
        domainCookies = getDomainCookies(domain);
      }
      if (domainCookies != null) {
        Iterator it = domainCookies.iterator();
        while (it.hasNext()) {
          curCookie = (Cookie) it.next();
          httpState.addCookie(curCookie);
        }
      }

      httpState.setCookiePolicy(CookiePolicy.RFC2109);
    }
    catch (MalformedURLException ex) {
      log.warn("Could not get domain for URL:" + urlString);
    }
    log.debug("HTTP State for URL: " + urlString + "(" + domain + ") is :" + httpState.toString());
    return httpState;
  }

  public static void printAllCookies() {
    synchronized (Crawler.cookieWatch) {
      Set set = cookies.keySet();

      if (set != null) {
        Iterator it = set.iterator();

        while (it.hasNext()) {
          String key = (String) it.next();
          log.debug("Domain: " + key);

          Collection list = (Collection) cookies.get(key);
          Cookie cookie;

          if (list != null) {
            Iterator it2 = list.iterator();
            while (it2.hasNext()) {
              cookie = (Cookie) (it2.next());
              printCookie(cookie);
            }
          }
        }
      }
    }

  }

  public static void printCookie(Cookie cookie) {
    log.debug("   " +
              " Comment " + cookie.getComment() +
              " Domain " + cookie.getDomain() +
              " Date " + cookie.getExpiryDate() +
              " Name " + cookie.getName() +
              " Value " + cookie.getValue() +
              " Path " + cookie.getPath() +
              " Secure " + cookie.getSecure() +
              " Version " + cookie.getVersion() +
              "");

  }

}
