/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: Crawler.java,v 1.4 2005/02/03 01:43:54 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/



package com.jcrawler.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import com.jcrawler.config.ConfigParser;
import com.jcrawler.Main;

/**
 *
 * <p>Crawler settings class </p>
 * @author not attributable
 * @version 1.0
 */
public class Crawler {

  private static Logger log = Logger.getLogger(Crawler.class);

    /**
   * Number of processed URLs. The modulus of this is, actually, used
   * separated by number of threads, to determine which thread should
   * process the current url.
   */
  private static volatile long counter = 0;
  private static long startTime;

  public static boolean stopCrawler = false;

  public static volatile long fetchedCounter = 0;

  public static volatile long numOfActiveThreads = 0;

  /**
   * FIFO of URLS found by processing HTMLs.
   * crawler threads are getting URLs to process
   * from this FIFO.
   */
  public static BoundedFIFO rawURLs = new BoundedFIFO(2000);

  /**
   * Set of URLs already parsed so that we do not get into an endless
   * loop because of some cross-references on sites.
   *
   * YES! You are absolutely right: iteration over processedURLs is not
   * synchronized or thread-safe. Only the mutation is - put() is synced.
   * Why? Because, the only iteration that happens on them
   * is the call to contains() and if it gives a wrong result every now
   * and then - IT IS GOOD ENOUGH. It will still prevent us from getting
   * in an endless loop. <p> At the same time, have I synchronized this
   * damned Set crawler would become way slower, even worse - it's speed
   * would degrade as this set increases (more crawler works, more would
   * it slow down). Slow down of the crawler would result in a necessary
   * crash of it, because threads are created every N millisecond - no matter
   * what and if they queue-up waiting for this stupid thread, soon enough
   * Java will have too many live threads.
   *
   * So - F... processedUrls Set and I don't care if it is not synced. It is
   * good enough this way! Actually, it's the best, this way.
   * @return Set
   */

  private static Set processedURLs = Collections.synchronizedSet(new HashSet());

  public static long getCounter() {
    return Crawler.counter;
  }

  /**
   * Increase the counter - number of processed URLs
   */
  public static void incCounter() {
    Crawler.counter++;
  }

  /**
   * YES! You are absolutely right processedURLs are not synchronized
   * or thread-safe. See explanation in the comment for processedURLs
   * property.
   * @return Set
   */
  public static Set getUrls() {
    return processedURLs;
  }

  public static long getStartTime() {
    return Crawler.startTime;
  }

  public static void setStartTime(long startTime) {
    Crawler.startTime = startTime;
  }

  public static final Object watch = new Integer(0);
  public static final Object cookieWatch = new Integer(0);
  /**
   * Ok, why do we need this property? To speed things up.
   * The story is that we do not want a contains() method
   * to be called on getUrls() while somebody is putting
   * something in that Set. if we use synchronize(), however,
   * to contains() calls will, also, wait for each-other
   * which is absolutely unnecessary and as the size of the
   * urls set grows - becomes a serious performance bottleneck.
   */
  public static boolean checking = false;



  /**
   * Counts number of iterations for which there are no URLs to process.
   * After ten iterations, crawler will restart. Used by: restartIfNeeded()
   * method.
   */
  private static int idleCounter = 0;

  /**
   * One of the "dangers" of unique crawling is that you will run
   * out of URLs eventually - once all unique ones are crawled.
   *
   * Once we detect that all uniwue URLs have been parsed, we try
   * to restart the process.
   *
   */

  public static void restartIfNeeded() {

   if ( idleCounter != -1 && Crawler.rawURLs.length() == 0 && Crawler.fetchedCounter > 3 ) {
      idleCounter ++;
    } else {
      idleCounter = 0;
    }

    if ( idleCounter > 20 ) {
      log.info ( "Crawler Restart requested. \n " +
                           "This means all the unique URLs have been " +
                           "accessed in this iteration and we are going " +
                           "into the next one");

      idleCounter = -1; //-- Do not disturb, while restarting!
      restartCrawler();
      idleCounter  = 0;

    }
  }

  public static void restartCrawler() {
    Crawler.getUrls().clear();
    Main.loadInitialUrls();
  }

  /**
   * Determines if the URL should be crawled down or not.
   * @param url
   * @return
   */
  public static boolean crawlOrNot(String url) {
    boolean flag = false;

//    synchronized (Crawler.watch) {

      //-- Has this URL already been processed? Then do not process.
       if (!Crawler.getUrls().contains(url)) {
          flag = true;
        } else {
          return false;
        }

      //-- Is this URL allowed from the configuration XML file settings?
      boolean matchFlag = false;
      Set urlPatterns = ConfigParser.getSettings().getUrlPatternsCompiled();
      if (urlPatterns != null) {
        Iterator itPatterns = urlPatterns.iterator();
        while (itPatterns.hasNext()) {
          Pattern pattern = (Pattern) itPatterns.next();
          Matcher m = pattern.matcher(url);
          matchFlag = m.matches();

          if (matchFlag) {
            break;
          }

        }

      }

      //-- If permissionMode = Denied, crawl all but that URLS
      if (ConfigParser.getSettings().getCrawlPermission() ==
          Settings.CRAWL_DENIED) {
        if (matchFlag) {
          flag = false;
          log.debug("DENIED URL " + url);
        } else {
          log.debug("NOT DENIED URL " + url);
          flag = true && flag;
        }
      }

      //-- If permissionMode = Allowed, crawl only that URLS
      if (ConfigParser.getSettings().getCrawlPermission() ==
          Settings.CRAWL_ALLOWED) {
        if (matchFlag) {
          flag = true && flag;
          log.debug("ALLOWED URL " + url);
        } else {
          log.debug("NOT ALLOWED URL " + url);
          flag = false;
        }
      }

  //-- enf synch cond  }

    return flag;
  }

  /**
   * Returns current time in a human-readable way with the precision of milliseconds
   * @return
   */
  public static String getCurrentTime() {
    DateFormat sdf = new SimpleDateFormat("HH:mm:ss::SSS");
    Date date = new Date(System.currentTimeMillis());

    return sdf.format(date);
  }

}
