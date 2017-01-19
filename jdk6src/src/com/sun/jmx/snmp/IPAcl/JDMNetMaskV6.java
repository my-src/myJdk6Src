/*
 * @(#)file      JDMNetMaskV6.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.7
 * @(#)date      06/11/01
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

/* Generated By:JJTree: Do not edit this line. JDMNetMaskV6.java */

package com.sun.jmx.snmp.IPAcl;

import java.net.UnknownHostException;

class JDMNetMaskV6 extends JDMNetMask {
  public JDMNetMaskV6(int id) {
    super(id);
  }

  public JDMNetMaskV6(Parser p, int id) {
    super(p, id);
  }
    protected PrincipalImpl createAssociatedPrincipal() 
    throws UnknownHostException { 
      return new NetMaskImpl(address.toString(), Integer.parseInt(mask));
  }
}
