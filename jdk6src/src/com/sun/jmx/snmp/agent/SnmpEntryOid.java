/* 
 * @(#)file      SnmpEntryOid.java 
 * @(#)author    Sun Microsystems, Inc. 
 * @(#)version   1.5 
 * @(#)date      06/11/01 
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */ 
package com.sun.jmx.snmp.agent;

import com.sun.jmx.snmp.SnmpOid;

/**
 * This class only adds a new constructor to SnmpOid...
 *
 **/ 
class SnmpEntryOid extends SnmpOid {
    /**
     * Constructs a new <CODE>SnmpOid</CODE> from the specified 
     * component array, starting at given position.
     *
     * @param oid   The original OID array
     * @param start The position at which to begin.
     *
     **/
    public SnmpEntryOid(long[] oid, int start) {
        final int subLength = oid.length - start;
        final long[] subOid = new long[subLength];
	java.lang.System.arraycopy(oid, start, subOid, 0, subLength) ;
	components = subOid;
	componentCount = subLength;
    }
}
