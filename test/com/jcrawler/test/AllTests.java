/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: AllTests.java,v 1.1 2004/11/30 22:45:33 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/


package com.jcrawler.test;

import com.jcrawler.util.TestHttpUtil;
import com.jcrawler.util.TestBoundedFIFO;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.jcrawler.TestUrlFetcher;
import com.jcrawler.util.TestCrawler;

public class AllTests
    extends TestCase {

  public AllTests(String s) {
    super(s);
  }

  public static Test suite() {
    TestSuite suite = new TestSuite();
  suite.addTestSuite(TestHttpUtil.class);
    suite.addTestSuite(TestBoundedFIFO.class);
    suite.addTestSuite(TestUrlFetcher.class);
    suite.addTestSuite(TestCrawler.class);
    return suite;
  }
}
