/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: TestUrlFetcher.java,v 1.1 2004/11/30 22:45:33 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler;

import junit.framework.*;
import java.net.*;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import com.jcrawler.config.ConfigParser;
import java.net.*;
import java.io.*;


public class TestUrlFetcher extends TestCase {

 Collection htmls, expecteds;

 public void setUp() {
   // Let's initialize setting so that crawlOrNot method does not get
   // in our way.
   ConfigParser.getSettings().getUrlPatterns().clear();
   ConfigParser.getSettings().setCrawlPermission(false);
 }

 /**
  * @todo
  */
 public void testFetcherfetch() {


    assertEquals ( "1", 0, 0);
  }

  public void testFetcherParser() throws  MalformedURLException {

    String url = null;
    String expected = null;
    Set urls = null; String html = null;

    for ( int i= 1; i<=2; i++ ) {

      switch ( i) {
        case  1:
          url = "http://ika.ge/testDir/";
          setUpSubDir();
          break;
        case 2:
          url = "http://ika.ge";
          setUpRootDir();
          break;
      }

      Iterator it = htmls.iterator(); int counter = 0;
      Iterator itExp = expecteds.iterator();

      while (it.hasNext()) {
        counter++;
        String sCounter = Integer.toString(i) + "." + Integer.toString(counter);

        html = (String) it.next();
        expected = (String) itExp.next();

        if (expected.equals("null")) {
          expected = null;
        }

        urls = UrlFetcher.parse(url, html);
        //System.out.println(sCounter + "  " + expected + "   " + set2String(urls));

        assertEquals(sCounter, expected, set2String(urls));
      }
    }

  }

  /**
   * For the purpose of the testFetcherParser only. We know that
   * our set, in this particular case, has only one object of
   * String type and want to return it. So we get the first element
   * and ignore the rest.
   *
   * @param set Set
   * @return String
   */
  public static String set2String ( Set set ) {
    if ( set != null ) {
        Iterator it = set.iterator();
        if ( it.hasNext() ) {
          String str = (String) it.next();
          return str;
        } else {
          return null;
        }
    } else {
      return null;
    }
  }

  /**
   * Setup when current Url is on in a sublevel
   */
  public void setUpSubDir() {
    htmls = new ArrayList(10);
    expecteds = new ArrayList(10);

    /* 1 */
    htmls.add(new String("<A onclick=\"trackInfo(this);\" LinkArea=\"Support\" LinkID=\"WP_S3_TopicMore\" HREF= http://search.microsoft.com >"));
    expecteds.add( new String("http://search.microsoft.com") );

    /* 2 */
    htmls.add(new String(
        "<a href=\"http://www.sanet.ge/wnew/adslnewlines.html\" target=_top>"));
    expecteds.add(new String("http://www.sanet.ge/wnew/adslnewlines.html"));

    /* 3 */
    htmls.add(new String("<a href=\"http://www.sanet.ge/2yknew.html\" target=\"_top\"><img src=/pic/sanet/1999/2000.gif alt=\"Year 2000 problem.\" border=0></a>"));
    expecteds.add(new String("http://www.sanet.ge/2yknew.html"));

    /* 4 */
    htmls.add(new String("<a href=\"www.test.com\" > testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 5 */
    htmls.add(new String(
        "<a href=\"www.test.com\\index.do?index#asdasd\" > testlink </a>"));
    expecteds.add(new String("http://www.test.com\\index.do?index#asdasd"));

    /* 6 */
    htmls.add(new String("<a href=\"www.test.com\"> testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 7 */
    htmls.add(new String("<a href=www.test.com> testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 8 */
    htmls.add(new String("<a href=www.test.com > testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 9 */
    htmls.add(new String(
        "<a testarg=\"testval\" href=\"www.test.com\" > testlink </a>"));
     expecteds.add(new String("http://www.test.com"));

     /* 10 */
    htmls.add(new String("<a testarg href=\"www.test.com\" > testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 11 */
    htmls.add(new String(
        "<a testarg href=\"www.test.com\" testarg=\"testval\"> testlink </a>"));
    expecteds.add(new String("http://www.test.com"));

    /* 12 */
    htmls.add(new String(
        "<a testarg href=\"/test/puku/tuku.do\" testarg=\"testval\"> testlink </a>"));
    expecteds.add(new String("http://ika.ge/test/puku/tuku.do"));

    /* 13 */
    htmls.add(new String(
     "<a testarg href=\"test/puku/tuku.do\" testarg=\"testval\"> testlink </a>"));
    expecteds.add(new String("http://ika.ge/testDir/test/puku/tuku.do"));

    /* 14 */
    htmls.add(new String(
     "<a testarg href=\"tuku.do\" testarg=\"testval\"> testlink </a>"));
     expecteds.add(new String("http://ika.ge/testDir/tuku.do"));
  }


  /**
   * Setup when current Url is on the toop level
   */
  public void setUpRootDir() {
    htmls = new ArrayList(10);
    expecteds = new ArrayList(10);

    /* 1 */
    htmls.add(new String(
        "<a testarg href=\"/test/puku/tuku.do\" testarg=\"testval\"> testlink </a>"));
    expecteds.add(new String("http://ika.ge/test/puku/tuku.do"));

    /* 2 */
    htmls.add(new String(
     "<a testarg href=\"test/puku/tuku.do\" testarg=\"testval\"> testlink </a>"));
    expecteds.add(new String("http://ika.ge/test/puku/tuku.do"));

    /* 3 */
    htmls.add(new String(
     "<a testarg href=\"tuku.do\" testarg=\"testval\"> testlink </a>"));
     expecteds.add(new String("http://ika.ge/tuku.do"));
  }


}
