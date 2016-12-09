/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: MainSchedulerThread.java,v 1.3 2005/02/03 01:43:52 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler.scheduler;

import org.apache.log4j.Logger;
import com.jcrawler.util.Crawler;

public class MainSchedulerThread
    extends Thread {

  private static Logger log = Logger.getLogger(MainSchedulerThread.class);

  private Class taskClass;
  private long milliseconds;

  /**
   * @todo make sure that task class extends Thread.
   */
  public MainSchedulerThread(Class taskClass, long milliseconds) {
    this.taskClass = taskClass;
    this.milliseconds = milliseconds;
  }

  public void run() {
    Thread myT;

    while (!Crawler.stopCrawler) {
      try {

        Crawler.restartIfNeeded();

        myT = (Thread) taskClass.newInstance();
        myT.setPriority(Thread.MIN_PRIORITY);

        Crawler.incCounter();

        myT.setName("THREAD#" + Crawler.getCounter() + " CREATED " +
                    Crawler.getCurrentTime());
        myT.setDaemon(true);
        myT.start();

      }
      catch (IllegalAccessException ex2) {
        log.error("Can not create instance of " + taskClass.getClass(), ex2);
      }
      catch (InstantiationException ex2) {
        log.error("Can not create instance of " + taskClass.getClass(), ex2);
      }

      try {
        this.sleep(this.milliseconds);
      }
      catch (InterruptedException ex) {
        log.warn("Schdeuler interrupted");
      }
    }
  }

}
