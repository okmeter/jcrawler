/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: UrlLoaderThread.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.log4j.Logger;
import com.jcrawler.util.Crawler;
import java.util.Set;

public class UrlLoaderThread
    extends Thread {

  private static Logger log = Logger.getLogger(UrlLoaderThread.class);

  private static String[] urls;

  public UrlLoaderThread(String configFilePath, int _maxThreads) {
    String s;
    StringBuffer szFInput = new StringBuffer();

    try {
      BufferedReader in = new BufferedReader(new FileReader(
          configFilePath));
      while ( (s = in.readLine()) != null) {
        szFInput.append(s + "\n");
      }
      in.close();
    }
    catch (java.io.IOException e) {
      log.error("getAndProcessUrls() failed ", e);
    }

    urls = new String(szFInput).split("\n");

  }

  public void run() {

    for (int i = 0; i < urls.length; i++) {
        if (!Crawler.getUrls().contains(urls[i])) {
          synchronized (Crawler.watch) {
            Crawler.rawURLs.put(urls[i]);
          }
      }
    }
  }

}
