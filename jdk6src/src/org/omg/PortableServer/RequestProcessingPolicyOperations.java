package org.omg.PortableServer;


/**
* org/omg/PortableServer/RequestProcessingPolicyOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../../src/share/classes/org/omg/PortableServer/poa.idl
* Wednesday, November 1, 2006 8:59:04 AM GMT
*/


/**
	 * This policy specifies how requests are processed by 
	 * the created POA.  The default is 
	 * USE_ACTIVE_OBJECT_MAP_ONLY.
	 */
public interface RequestProcessingPolicyOperations  extends org.omg.CORBA.PolicyOperations
{

  /**
	 * specifies the policy value
	 */
  org.omg.PortableServer.RequestProcessingPolicyValue value ();
} // interface RequestProcessingPolicyOperations
