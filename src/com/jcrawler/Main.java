/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: Main.java,v 1.3 2005/09/06 18:37:08 idumali Exp $
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import com.jcrawler.config.ConfigParser;
import com.jcrawler.scheduler.MainSchedulerThread;
import com.jcrawler.scheduler.MonitorThread;
import com.jcrawler.util.Crawler;
import com.jcrawler.util.Settings;

public class Main {

  private static Logger log = Logger.getLogger(Main.class);

  public static void main(String[] args) {

    log.info("LOADING CONFIGURATION. Please, stand by.");
    Settings sets = ConfigParser.getSettings();
    log.info("CONFIGURATION LOADED.");

    testConfigparser();
    System.out.println ( "Permission setting: " + ConfigParser.getSettings().getCrawlPermission());

    //ConfigParser.getSettings().setCrawlPermission( true );

    boolean isAnythingToCrawl = loadInitialUrls();

    if (!isAnythingToCrawl) {
      log.error("No starting URLs given to begin crawling from ");
      return;
    }

    startMainSchedulerThread();
    startMonitorThread();

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      while (true) {
        int c = br.read();
        if (c == 10) {
          log.info(" Program Stopped by user. ");
          Crawler.stopCrawler = true;

          //-- Shutdown connection pool
          UrlFetcher.connectionManager.shutdown();
          return;
        }
      }
    }
    catch (IOException ex) {
    }
  }

  private static void startMainSchedulerThread() {
    log.debug("Starting scheduler");
    Crawler.setStartTime(System.currentTimeMillis());

    MainSchedulerThread schedulerThread = new MainSchedulerThread
        (com.jcrawler.scheduler.FetcherTask.class,
         ConfigParser.getSettings().getInterval());
    schedulerThread.setPriority(Thread.MIN_PRIORITY);
    schedulerThread.setName("Scheduler Thread");
    schedulerThread.setDaemon(false);
    schedulerThread.start();

  }

  private static void startMonitorThread() {
    log.debug("Starting scheduler");
    Crawler.setStartTime(System.currentTimeMillis());

    MonitorThread monitor = new MonitorThread
        ( ConfigParser.getSettings().getMonitorInterval() );

    monitor.setPriority(Thread.MIN_PRIORITY);
    monitor.setName("Monitor Thread");
    monitor.setDaemon(false);
    monitor.start();

  }

  public static boolean loadInitialUrls() {
    Set urls = ConfigParser.getSettings().getCrawlUrls();
    if (urls != null) {
      Iterator it = urls.iterator();
      while (it.hasNext()) {
        String curURL = (String) it.next();
        Crawler.rawURLs.put(curURL);
      }
    }
    else {
      return false;
    }

    return true;
  }

  /**
   * Debug method to test if the config parser works properly
   */
  private static void testConfigparser() {
    log.info("======= CRAWLER CONFIGURATION ======");
    log.info("Connection Timout: " +
             ConfigParser.getSettings().getConnectionTimeout());
    log.info("Threads Interval: " + ConfigParser.getSettings().getInterval());
    log.info("Monitor Interval: " +
             ConfigParser.getSettings().getMonitorInterval());
    log.info("------- HTTP Headers ----- ");
    printMap(ConfigParser.getSettings().getHeaders());
    log.info("-------------------------- ");
    log.info("------- Crawl URLS ------- ");
    log.info( printSet(ConfigParser.getSettings().getCrawlUrls()) );
    log.info("-------------------------- ");
    log.info("Pattern Permission: " +
             ConfigParser.getSettings().getCrawlPermission());
    log.info("------- URL Patterns ------- ");
    log.info( printSet(ConfigParser.getSettings().getUrlPatterns()) );
    log.info("-------------------------- ");
    log.info("===== END CRAWLER CONFIGURATION =====");

  }

  public static String printSet(Set set) {
    String ret = "";
    if (set != null) {
      Iterator it = set.iterator();
      while (it.hasNext()) {
        String element = (String) it.next();
        ret = ret + "   " + element;
        if (it.hasNext() ) ret = ret + "\n";
      }
    }
    return ret;

  }

  private static void printMap(Map map) {
    if (map != null) {
      Set keyset = map.keySet();
      if (keyset != null) {
        Iterator it = keyset.iterator();
        String key, value;

        while (it.hasNext()) {
          key = (String) it.next();
          value = (String) map.get(key);
          log.info("   " + key + " = " + value);
        }
      }
    }
  }

}
