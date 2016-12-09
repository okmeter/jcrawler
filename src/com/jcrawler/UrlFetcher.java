/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: UrlFetcher.java,v 1.4 2005/09/06 18:37:29 idumali Exp $
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
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import com.jcrawler.config.ConfigParser;
import com.jcrawler.util.Crawler;
import com.jcrawler.util.HttpUtil;
import com.jcrawler.util.DashBoard;
import java.util.Collections;
import com.jcrawler.scheduler.MonitorThread;

/**
 * This class is used to fetch an HTML of a page from a given URL.
 */
public class UrlFetcher {

  private static Logger log = Logger.getLogger(UrlFetcher.class);
  //Used in parse() for the initial capacity of a set
  private static final int AVERAGE_NUM_OF_LINKS = 100;
  private static Pattern pattern;
  public static MultiThreadedHttpConnectionManager connectionManager;

  static {
    final int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL |
        Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;

    // Match groups 1 and 3 are just for debugging, we are only interested in group 2nd.
    //String regexp = "<a.*?\\shref\\s*=\\s*([\\\"\\']*)(.*?)([\\\"\\'\\s]).*?>";
    String regexp =
        "<a.*?\\shref\\s*=\\s*([\\\"\\']*)(.*?)([\\\"\\'\\s].*?>|>)";

    log.debug("Regular Expression to catch all anchors: " + regexp);
    UrlFetcher.pattern = Pattern.compile(regexp, flags);

    //-- Reusable conneciton manager.
    connectionManager = new MultiThreadedHttpConnectionManager();
  }

  /**
   * Method fetching a url and returning HTML output.
   *
   * @param urlString String
   * @throws NullPointerException
   * @return String
   */

  public static String fetch(String urlString) throws UrlFetchException {

    log.debug("Fetching URL " + urlString);

    synchronized ( Crawler.getUrls() ) {
      Crawler.getUrls().add( urlString );
    }

    String content = "";

    // Prepare HTTP client instance
    HttpClient httpclient = new HttpClient(connectionManager);
    httpclient.setConnectionTimeout(ConfigParser.getSettings().
                                    getConnectionTimeout());
    httpclient.setState(CookieManager.getHttpState(urlString));

    // Prepare HTTP GET method
    GetMethod httpget = null;
    try {
      httpget = new GetMethod(urlString);
    }
    catch (IllegalArgumentException ex1) {
      throw new UrlFetchException();
    }

    Map headers = ConfigParser.getSettings().getHeaders();
    if (headers != null) {
      Set headerKeys = headers.keySet();
      if (headerKeys != null) {
        Iterator itHeaders = headerKeys.iterator();
        String key, value;

        while (itHeaders.hasNext()) {
          key = (String) itHeaders.next();
          value = (String) headers.get(key);
          //log.debug( key + "=" + value);
          httpget.addRequestHeader(key, value);
        }
      }
    }

    // Execute HTTP GET
    int result = 0;
    try {

      long startTime = System.currentTimeMillis();
      result = httpclient.executeMethod(httpget);

      content = httpget.getResponseBodyAsString();

      long endTime = System.currentTimeMillis();
      DashBoard.add( urlString, endTime - startTime );

      //log.debug( "Content: " );
      //log.debug( content );

      // Save the cookies
      Cookie[] cookies = httpclient.getState().getCookies();
      for (int i = 0; i < cookies.length; i++) {
        CookieManager.addCookie(cookies[i]);
      }

      MonitorThread.calculateCurrentSpeed();

      synchronized (Crawler.watch) {
        Crawler.fetchedCounter++;
      }
      log.info("FETCHED " + Crawler.fetchedCounter + "th URL: " + urlString);

      //log.debug ( "Response code: " + result );

      //CookieManager.printAllCookies();

      String redirectLocation;
      Header locationHeader = httpget.getResponseHeader("location");
      if (locationHeader != null) {
        redirectLocation = locationHeader.getValue();
        log.debug("Redirect Location: " + redirectLocation);

        if (redirectLocation != null) {
          // Perform Redirect!
          content = fetch(redirectLocation);
        }
        else {
          // The response is invalid and did not provide the new location for
          // the resource.  Report an error or possibly handle the response
          // like a 404 Not Found error.
          log.error("Error redirecting");
        }

      }

    }
    catch (Exception ex) {
      throw (new UrlFetchException(ex));
    }
    finally {
      // Release current connection to the connection pool once you are done
      httpget.releaseConnection();
    }

    /*log.debug( " C O O K I E S: " );
    CookieManager.printAllCookies(); */
    return content;

  }

  /**
   * Method parsing HTML and returning a set of links found in there.
   *
   * @param url String needed to compute the absolute pathes from the relative pathes in HTML.
   * @param html String
   * @return java.util.Set or null if no URLs found after parse.
   * @todo In the current implementation HTML FRAME-based sites are not parsed.
   */

  public static Set parse(String url, String html) {

    Set anchors = Collections.synchronizedSet(new HashSet(UrlFetcher.AVERAGE_NUM_OF_LINKS));

    Matcher matcher = UrlFetcher.pattern.matcher(html);

    // Debug code
    //matcher.find();
    //  for ( int i=0; i<=matcher.groupCount(); i++) {
    //    log.debug("#"+matcher.group(i)+"#");
    //  }

    String domain = null;
    try {
      domain = HttpUtil.getDomainFromUrl(url);
    }
    catch (MalformedURLException ex) {
      // We can not parse URL that is malformed.
      return new HashSet();
    }

    String baseURI = null;
    try {
      baseURI = HttpUtil.getBaseUriFromUrl(url);
    }
    catch (MalformedURLException ex1) {
      // We can not parse URL that is malformed.
      return new HashSet();
    }

    String currentUrl = "";
    boolean wrongURL = false;

    while (matcher.find()) {

/*      System.out.println("----------");
      System.out.print( domain + "   " );
      System.out.print( baseURI + "   " );
      System.out.println( matcher.group(2));      */

      currentUrl = HttpUtil.canonizeURL(domain, baseURI, matcher.group(2));

      wrongURL = false;
      try {
        URL javaCurrentURL;
        javaCurrentURL = new URL(currentUrl);
      }
      catch (MalformedURLException ex2) {
        wrongURL = true;
      }

      //log.debug (  currentUrl );

      synchronized (Crawler.watch) {
        if (Crawler.crawlOrNot(currentUrl) == true && wrongURL == false) {
          //synchronized ( anchors ) {
            anchors.add(currentUrl);
          //}
        }
      }
    }

    return anchors;
  }

}
