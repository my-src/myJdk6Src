/*
 * @(#)file      SnmpSocket.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.9
 * @(#)date      06/11/01
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */


package com.sun.jmx.snmp.daemon;


// JAVA imports
//
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.io.IOException;

// SNMP Runtime imports
//
import com.sun.jmx.trace.Trace;


/**
 * This class creates an SNMP Datagram Socket. This class has methods helpful
 * to send SNMP inform request packets to an arbitrary port of a specified device.
 * It also runs a thread that is devoted to receiving SNMP inform response on the socket.
 * <BR>
 * A socket imposes an upper limit on size of inform response packet. Any 
 * packet which exceeds this limit is truncated. By default, this
 * limit is {@link SnmpAdaptorServer#bufferSize}. 
 */

final class SnmpSocket implements java.lang.Runnable {

    
    // VARIABLES
    //----------
    
    private DatagramSocket      _socket = null;
    private SnmpResponseHandler _dgramHdlr = null;
    private Thread              _sockThread = null;
    private byte[]              _buffer = null;
    private transient boolean   isClosing = false;

    int                         _socketPort = 0;
    int                         responseBufSize = 1024;
    String                      dbgTag = "SnmpSocket";
    
    // CONSTRUCTORS
    //-------------
    
    /**
     * Creates a new <CODE>SnmpSocket</CODE> object.
     * @param rspHdlr A Datagram handler.
     * @param bufferSize The SNMP adaptor buffer size.
     * @exception SocketException A socket could not be created.
     */
    public SnmpSocket(SnmpResponseHandler rspHdlr, InetAddress addr, int bufferSize) throws SocketException {
        super();
        
        if (isTraceOn()) {
            trace("constructor", "Creating new SNMP datagram socket");
        }
    
        // TIME BOMB HERE
        _socket = new DatagramSocket(0, addr);
        _socketPort = _socket.getLocalPort();
        responseBufSize = bufferSize;
        _buffer = new byte[responseBufSize];
        _dgramHdlr = rspHdlr;
        _sockThread = new Thread(this, "SnmpSocket");
        _sockThread.start();
    }

    // PUBLIC METHODS
    //---------------
    
    /**
     * Sends a datagram packet to a specified device at specified port.
     * @param buff The packet data.
     * @param length The packet length.
     * @param addr The destination address.
     * @param port The destination port number.
     * @exception IOException Signals that an I/O exception of some sort has occurred.
     */
    public synchronized void sendPacket(byte[] buff, int length, InetAddress addr, int port) throws IOException {
        DatagramPacket dgrmpkt;
        dgrmpkt = new DatagramPacket(buff, length, addr, port);
        sendPacket(dgrmpkt);
    }
    
    /**
     * Sends a datagram packet to a specified device at specified port.
     * @param dgrmpkt The datagram packet.
     * @exception IOException Signals that an I/O exception of some sort has occurred.
     */
    public synchronized void sendPacket(DatagramPacket dgrmpkt) throws IOException {
	
        try {
            if (isValid()) {
                if (isTraceOn()) {
                    trace("sendPacket", "Sending DatagramPacket. Length = " + dgrmpkt.getLength() +
                          " through socket = " + _socket.toString());
                }
                _socket.send(dgrmpkt);
            } else
                throw new IOException("Invalid state of SNMP datagram socket.");
        } catch (IOException e) {
            if (isDebugOn()) {
                debug("sendPacket", "Io error while sending");
                debug("sendPacket", e.getMessage());
            }
            throw e;
        } 
    }

    /**
     * Checks if the socket is initialised correctly and if it is still active.
     * @return <CODE>true</CODE> if the socket is initialised correctly and if it is still active,
     * <CODE>false</CODE> otherwise.
     */
    public synchronized boolean isValid() {
        return _socket != null && _sockThread != null && _sockThread.isAlive();
    }
    
    /**
     * Closes the socket and its associated resources.
     */
    public synchronized void close() {
        
        isClosing = true;

        if (isTraceOn()) {
            trace("close", "Closing and destroying the SNMP datagram socket -> " + toString());
        }
        
        try {
            // We send an empty datagram packet to fix bug 4293791 (it's a jdk 1.1 bug)
            //
            DatagramSocket sn = new java.net.DatagramSocket(0);
            byte[] ob = new byte[1];
            DatagramPacket pk = new DatagramPacket(ob , 1, java.net.InetAddress.getLocalHost(), _socketPort);
            sn.send(pk);
            sn.close();
        } catch (Exception e) {}
                
        // First close the datagram socket.
        // This may generates an IO exception at the run method (_socket.receive).
        //
        if (_socket != null) {
            _socket.close() ;
            _socket = null ;
        }
    
        // Then stop the thread socket.
        //
        if (_sockThread != null && _sockThread.isAlive()) {
            _sockThread.interrupt();
            try {
                // Wait until the thread die.
                //
                _sockThread.join();
            } catch (InterruptedException e) {
                // Ignore...
            }
            _sockThread = null ;
        }
    }

    /**
     * Dispatcher method for this socket thread. This is the dispatcher method
     * which goes in an endless-loop and waits for receiving datagram packets on the socket.
     */
    public void run() {
        Thread.currentThread().setPriority(8);

        while (true) {
            try {
                DatagramPacket dgrmpkt = new DatagramPacket (_buffer, _buffer.length);

                if (isTraceOn()) {
                    trace("run", "[" + Thread.currentThread().toString() + "]:" + "Blocking for receiving packet");
                }

                _socket.receive(dgrmpkt);
                
                // If the corresponding session is being destroyed, stop handling received responses.
                //
                if (isClosing)
                    break;

                if (isTraceOn()) {
                    trace("run", "[" + Thread.currentThread().toString() + "]:" + "Received a packet");
                }

                if (dgrmpkt.getLength() <= 0)
                    continue;

                if (isTraceOn()) {
                    trace("run", "[" + Thread.currentThread().toString() + "]:" + "Received a packet from : " + 
                          dgrmpkt.getAddress().toString() + ", Length = " + dgrmpkt.getLength());
                }

                handleDatagram(dgrmpkt);
                
                // We are closing the snmp socket while handling the datagram.
                //
                if (isClosing)
                    break;
			
            } catch (IOException io) {
                // If the IO exception has been generated because of closing this SNMP socket,
                // (call to _socket.close while _socket is blocked for receiving packet) simply terminate closing properly.
                //
                if (isClosing) {
                    break;
                }
                if (isDebugOn()) {
                    debug("run", io.getMessage());
                    debug("run", io);
                }
            } catch (Exception e) {
                // If the exception (NullPointerException) has been generated because of closing this SNMP socket,
                // (call to _socket = null while _socket is blocked for receiving packet) simply terminate closing properly.
                //
                if (isClosing) {
                    break;
                }
                if (isDebugOn()) {
                    debug("run", "Exception in socket thread...");
                    debug("run", e);
                }
            } catch (ThreadDeath d) {
                if (isDebugOn()) {
                    debug("run", "Socket Thread DEAD..." + toString());
                    debug("run", d);
                }
                close();
                throw d;  // rethrow dead thread.
            } catch (Error err) {
                if (isDebugOn()) {
                    debug("run", err);
                }
                handleJavaError(err);
            }
        }
    }
    
    /**
     * Finalizer of the <CODE>SnmpSocket</CODE> objects.
     * This method is called by the garbage collector on an object 
     * when garbage collection determines that there are no more references to the object.
     * <P>Closes the datagram socket and stops the socket thread associated to this SNMP socket.
     */
    public synchronized void finalize() {
        close();
    }
    
    // PRIVATE METHODS
    //----------------
    
    /*
     * Keep this locked so that send can't happen.
     */
    private synchronized void handleJavaError(Throwable thr) {
        if (thr instanceof OutOfMemoryError) {
            if (isDebugOn()) {
                debug("handleJavaError", thr);
            }
            Thread.currentThread().yield();
            return ;
        }
        if (_socket != null) {
            _socket.close();
            _socket = null;
        }

        if (isDebugOn()) {
            debug("handleJavaError", "Global Internal error");
        }
        Thread.currentThread().yield();
    }

    private synchronized void handleDatagram(DatagramPacket dgrmpkt) {
        _dgramHdlr.processDatagram(dgrmpkt);
    }

    // TRACES & DEBUG
    //---------------
    
    boolean isTraceOn() {
        return Trace.isSelected(Trace.LEVEL_TRACE, Trace.INFO_ADAPTOR_SNMP);
    }

    void trace(String clz, String func, String info) {
        Trace.send(Trace.LEVEL_TRACE, Trace.INFO_ADAPTOR_SNMP, clz, func, info);
    }

    void trace(String func, String info) {
        trace(dbgTag, func, info);
    }
    
    boolean isDebugOn() {
        return Trace.isSelected(Trace.LEVEL_DEBUG, Trace.INFO_ADAPTOR_SNMP);
    }

    void debug(String clz, String func, String info) {
        Trace.send(Trace.LEVEL_DEBUG, Trace.INFO_ADAPTOR_SNMP, clz, func, info);
    }

    void debug(String clz, String func, Throwable exception) {
        Trace.send(Trace.LEVEL_DEBUG, Trace.INFO_ADAPTOR_SNMP, clz, func, exception);
    }

    void debug(String func, String info) {
        debug(dbgTag, func, info);
    }
    
    void debug(String func, Throwable exception) {
        debug(dbgTag, func, exception);
    }
}
