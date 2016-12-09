/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: HttpUtil.java,v 1.2 2005/01/25 11:43:29 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/



package com.jcrawler.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import com.jcrawler.UrlFetcher;

public class HttpUtil {

  private static Logger log = Logger.getLogger(UrlFetcher.class);

  public static String getBaseUriFromUrl(String url) throws
      MalformedURLException {
    URL javaURL = new URL(url);
    String path = javaURL.getPath();
    int index = path.lastIndexOf("/");
    if (index == -1) {
      return "";
    }
    else {
      return path.substring(0, index);
    }

  }

  /**
   * URLs, in anchors, can come in three flavours:
   * <li> Canonical (begining with "http://")
   * <li> Absolute, non-canonical (begining with "/")
   * <li> Relative (not begining with either "http" or "/")
   * @param domain
   * @param baseUrl
   * @param link
   * @return
   */
  public static String canonizeURL(String domain, String baseUrl, String link) {
    link = link.trim();
    String ret = "";

    if (link.startsWith("javascript") || link.startsWith("mailto:")) {
      ret = ""; //Illegal URL
    }
    else if (link.startsWith("http")) {
      ret = link;
    }
    else if (link.startsWith("www.")) {
      ret = "http://"+link;
    }
    else if (link.startsWith("/")) {
      int indx = 0;
      if (domain.endsWith("/")) {
        indx = 1;
      }
      ret = domain.substring(indx) + link;
    }
    else {
      String slash2 = "/";

      if ( ! domain.endsWith("/")) domain = domain + "/";
      if ( baseUrl.startsWith ( "/") ) baseUrl = baseUrl.substring(1);
      if ( link.startsWith ( "/") ) link = link.substring(1);
      if ( baseUrl.equals("")) { slash2="";}
      if (baseUrl.endsWith("/")) { slash2 = "";  }
      if (link.equals("")) { slash2 = "";  }

      //System.out.println( domain + "%1%" + baseUrl + "%3%" +  slash2 + "%4%" +  link );

      ret = domain + baseUrl + slash2 + link;

    }
    return ret;
  }

  public static String getDomainFromUrl(String url) throws
      MalformedURLException {
    URL javaURL = new URL(url);
    return javaURL.getProtocol() + "://" + javaURL.getHost()+(javaURL.getPort()!=-1?":"+javaURL.getPort():"");
  }

}
