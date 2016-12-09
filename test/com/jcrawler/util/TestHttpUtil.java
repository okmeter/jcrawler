/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: TestHttpUtil.java,v 1.1 2004/11/30 22:45:33 idumali Exp $
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

public class TestHttpUtil extends TestCase {

  public void testGetBaseUriFromUrl() throws  MalformedURLException {
    String baseUrl = null; String expected = null;

    baseUrl = HttpUtil.getBaseUriFromUrl(
          "http://junit.sourceforge.net:8080/doc/cookbook/cookbook.htm");
    expected = "/doc/cookbook";
    assertEquals( "Long Path",  expected, baseUrl);

    baseUrl = HttpUtil.getBaseUriFromUrl(
          "http://junit.sourceforge.net:8080/");
    expected = "";
    assertEquals( "No Path", expected, baseUrl);

    baseUrl = HttpUtil.getBaseUriFromUrl(
    "http://junit.sourceforge.net:8080/pi");
    expected = "";
    assertEquals( "Just file Path", expected, baseUrl);

    baseUrl = HttpUtil.getBaseUriFromUrl(
    "http://junit.sourceforge.net:8080/pi/");
    expected = "/pi";
    assertEquals( "One dir path", expected, baseUrl);
  }

  public void testCaonizeURL() throws MalformedURLException {
    String result = HttpUtil.canonizeURL("http://ika.ge/", "", "test/puku/tuku.do");
    assertEquals("1", "http://ika.ge/test/puku/tuku.do", result);

    result = HttpUtil.canonizeURL("http://ika.ge","/testDir", "test/puku/tuku.do");
    assertEquals("2", "http://ika.ge/testDir/test/puku/tuku.do", result);
  }


}
