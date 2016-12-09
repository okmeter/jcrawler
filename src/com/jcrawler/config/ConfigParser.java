/*
 *   @Author George Kvizhinadze
 *   CVS-ID: $Id: ConfigParser.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler.config;

import org.apache.commons.digester.Digester;
import com.jcrawler.util.Settings;
import java.io.File;
import org.xml.sax.*;
import java.io.*;
import org.apache.log4j.Logger;
import com.jcrawler.util.ParamMapEntry;
import java.util.regex.Pattern;
import java.util.Iterator;
import java.util.Set;

public class ConfigParser {
  private static Logger log = Logger.getLogger(ConfigParser.class);
  private static Settings settings = null;

  public static Settings getSettings() {
    return settings;
  }

  static {
    try {
      settings = parseConfiguration();
      //-- Compile regexpes to improve performance.
      compileUrlPatterns();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    catch (SAXException ex) {
      ex.printStackTrace();
    }

  }

  private static void compileUrlPatterns() {
    //-- Compile regexpes to improve performance.
    Set urlPatterns = ConfigParser.getSettings().getUrlPatterns();
    Iterator itPatterns = urlPatterns.iterator();
    final int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL |
        Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;
    while (itPatterns.hasNext()) {
      String currRegexp = (String) itPatterns.next();
      Pattern pattern = Pattern.compile(currRegexp, flags);
      ConfigParser.getSettings().getUrlPatternsCompiled().add( pattern );
    }

  }

  private static Settings parseConfiguration() throws SAXException,
      IOException {

    Settings set = new Settings();
    String configFileName = "." + File.separator + "conf" + File.separator +
                               "crawlerConfig.xml";
    File configFile = new File( configFileName );

    log.debug(configFile.getAbsolutePath());

    Digester digester = new Digester();
    digester.clear();
    digester.setValidating(false);
    digester.setUseContextClassLoader(true);

    digester.addObjectCreate("settings", Settings.class);

    digester.addBeanPropertySetter("settings/interval", "interval");
    digester.addBeanPropertySetter("settings/monitorInterval",
                                   "monitorInterval");

    digester.addBeanPropertySetter("settings/connectionTimeout",
                                   "connectionTimeout");

    digester.addSetProperties("settings/url-patterns",
                              "permission", "crawlPermission");

    digester.addObjectCreate("settings/crawl-urls/url", ParamMapEntry.class);
    digester.addSetNext("settings/crawl-urls/url", "addCrawlUrl");
    digester.addBeanPropertySetter("settings/crawl-urls/url",
                                   "key");

    digester.addObjectCreate("settings/url-patterns/pattern", ParamMapEntry.class);
    digester.addSetNext("settings/url-patterns/pattern", "addUrlPattern");
    digester.addBeanPropertySetter("settings/url-patterns/pattern",
                                   "key");

    digester.addObjectCreate("settings/headers/header", ParamMapEntry.class);
    digester.addSetNext("settings/headers/header", "addHeader");
    digester.addSetProperties("settings/headers/header",
                              "name", "key");
    digester.addBeanPropertySetter("settings/headers/header",
                                   "value");

    set = (Settings) digester.parse(configFile);
    return set;
  }
}
