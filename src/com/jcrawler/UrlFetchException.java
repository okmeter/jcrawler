/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: UrlFetchException.java,v 1.1 2004/11/30 22:47:42 idumali Exp $
 *
 *   Copyright (c) 2004 Development Gateway Foundation, Inc. All rights reserved.
 *   This program and the accompanying materials are made available under the
 *   terms of the Common Public License v1.0 which accompanies this
 *   distribution, and is available at:
 *   http://www.opensource.org/licenses/cpl.php
 *
 *****************************************************************************/

package com.jcrawler;

public class UrlFetchException
    extends Exception {
  public UrlFetchException() {
  }

  public UrlFetchException(String message) {
    super(message);
  }

  public UrlFetchException(String message, Throwable cause) {
    super(message, cause);
  }



  public UrlFetchException(Throwable cause) {
    super(cause);
  }
}
