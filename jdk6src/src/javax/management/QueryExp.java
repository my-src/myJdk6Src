/*
 * @(#)QueryExp.java	4.18 05/11/17
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.management;

// java import
import java.io.Serializable;


/**
 * <p>Represents relational constraints that can be used in database
 * query "where clauses".  Instances of QueryExp are returned by the
 * static methods of the {@link Query} class.</p>
 *
 * <p>It is possible, but not
 * recommended, to create custom queries by implementing this
 * interface.  In that case, it is better to extend the {@link
 * QueryEval} class than to implement the interface directly, so that
 * the {@link #setMBeanServer} method works correctly.
 *
 * @since 1.5
 */
public interface QueryExp extends Serializable {
     
    
     /**
      * Applies the QueryExp on an MBean.
      *
      * @param name The name of the MBean on which the QueryExp will be applied.
      *
      * @return  True if the query was successfully applied to the MBean, false otherwise
      *
      * @exception BadStringOperationException
      * @exception BadBinaryOpValueExpException
      * @exception BadAttributeValueExpException 
      * @exception InvalidApplicationException
      */
     public boolean apply(ObjectName name) throws BadStringOperationException, BadBinaryOpValueExpException,
	 BadAttributeValueExpException, InvalidApplicationException ;

     /**
      * Sets the MBean server on which the query is to be performed.
      *
      * @param s The MBean server on which the query is to be performed.
      */
     public void setMBeanServer(MBeanServer s) ;

 }
