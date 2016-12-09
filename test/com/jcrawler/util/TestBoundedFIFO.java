/*
 *   @Author Irakli Nadareishvili
 *   CVS-ID: $Id: TestBoundedFIFO.java,v 1.1 2004/11/30 22:45:33 idumali Exp $
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

public class TestBoundedFIFO extends TestCase {

  public void testFIFOResize() {

      BoundedFIFO bf = new BoundedFIFO(30, 20);

      for (int i = 0; i < 149; i++) {
        bf.put( new Integer(i) );
      }

      assertEquals( "Length 1", 149, bf.length());
      assertEquals( "Size 1", 150, bf.getMaxSize());

      bf.put( new Integer(12) );
      assertEquals( "Length 2", 150, bf.length());
      assertEquals( "Size 2", 170, bf.getMaxSize());

      for (int i = 0; i <= 150; i++) {
            Integer k = (Integer) bf.get();
      }
      assertEquals( "Length 3", 0, bf.length());
      assertEquals( "Size 3", 170, bf.getMaxSize());
  }


}

