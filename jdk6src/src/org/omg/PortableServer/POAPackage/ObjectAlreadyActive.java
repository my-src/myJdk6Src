package org.omg.PortableServer.POAPackage;


/**
* org/omg/PortableServer/POAPackage/ObjectAlreadyActive.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ../../../../src/share/classes/org/omg/PortableServer/poa.idl
* Wednesday, November 1, 2006 8:59:05 AM GMT
*/

public final class ObjectAlreadyActive extends org.omg.CORBA.UserException
{

  public ObjectAlreadyActive ()
  {
    super(ObjectAlreadyActiveHelper.id());
  } // ctor


  public ObjectAlreadyActive (String $reason)
  {
    super(ObjectAlreadyActiveHelper.id() + "  " + $reason);
  } // ctor

} // class ObjectAlreadyActive
