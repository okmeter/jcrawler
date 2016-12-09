/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: DebugSingle.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler.debug;

import org.apache.log4j.Logger;
import com.jcrawler.CookieManager;
import com.jcrawler.UrlFetchException;
import com.jcrawler.UrlFetcher;
import com.jcrawler.config.ConfigParser;

public class DebugSingle {

  private static Logger log = Logger.getLogger(DebugSingle.class);

  public static void main(String[] args) throws Exception {

    // Let's initialize setting so that crawlOrNot method does not get
    // in our way.
    ConfigParser.getSettings().getUrlPatterns().clear();
    ConfigParser.getSettings().setCrawlPermission(false);

    //String strURL = "http://uat-demosite.digijava.org:9022/";
    String strURL = "http://amd64-tenders.dgmarket.com/eproc/SearchResult.do~3~~";
    //String strURL = "http://amd64-appalti.dgmarket.com/um~user/newSession.do?dgsessionid=cdf2624e02d64763d43fde823c3927cf&rfr=http%3A%2F%2Famd64-appalti.dgmarket.com%7E%2Feproc%2FSearchResult.do%7E3%7E%7E";
    //String strURL = "http://uat-login.digijava.org:9022/um~user/login.do?rfr=http%3A%2F%2Fuat-demosite.digijava.org%3A9022%7E%2F&autoLogin=true";

    try {
      String content = UrlFetcher.fetch(strURL);
      log.debug ("CONTENT of URL: " + strURL + " has been fetched." );
      //log.debug ( content );
    }
    catch (UrlFetchException ex) {
      log.warn("Could not fetch URL: " + strURL);
    }

    CookieManager.printAllCookies();

  }
}
