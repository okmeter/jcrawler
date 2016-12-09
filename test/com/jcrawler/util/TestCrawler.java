/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: TestCrawler.java,v 1.1 2004/11/30 22:45:33 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/


package com.jcrawler.util;


import junit.framework.*;
import java.net.*;
import com.jcrawler.config.ConfigParser;

public class TestCrawler extends TestCase {

  public void setUp() {
    Crawler.getUrls().clear();
    ConfigParser.getSettings().getUrlPatterns().clear();
    ConfigParser.getSettings().setCrawlPermission(false);
  }

  public void testCrawlOrNot() {
    boolean result;

    //-- Make sure that already-crawled URLs do not get re-crawled.
    result = Crawler.crawlOrNot("http://ika.ge");
    assertEquals ("1 ", true,result);

    Crawler.getUrls().add( new String("http://ika.ge"));
    result = Crawler.crawlOrNot("http://ika.ge");
    assertEquals ("2 ", false,result);
    Crawler.getUrls().clear();

    // This setting affects the following two asserts
    ConfigParser.getSettings().getUrlPatterns().add( new String(".*?amd64\\-.*"));

    //This setting affects the following two asserts
    ConfigParser.getSettings().setCrawlPermission(true);

    result = Crawler.crawlOrNot("http://amd64-dgmarket.com");
    assertEquals ("3 ", true, result);

    result = Crawler.crawlOrNot("http://topics.developmentgateway.org");
    assertEquals ("4 ", false, result);

    // This setting affects the following two asserts
    ConfigParser.getSettings().setCrawlPermission(false);

    result = Crawler.crawlOrNot("http://amd64-dgmarket.com");
    assertEquals ("5 ", false, result);

    result = Crawler.crawlOrNot("http://topics.developmentgateway.org");
    assertEquals ("6 ", true, result);

    ConfigParser.getSettings().getUrlPatterns().clear();
    ConfigParser.getSettings().setCrawlPermission(true);
    ConfigParser.getSettings().getUrlPatterns().add( new String(".*?amd64\\-.*"));

    result = Crawler.crawlOrNot("http://www.developmentgateway.org/");
    assertEquals ("7 ", false, result);

    result = Crawler.crawlOrNot("http://amd64-www.dgmarket.com/");
    assertEquals ("8 ", true, result);




  }


}

