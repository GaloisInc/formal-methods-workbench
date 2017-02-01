package edu.umn.cs.crisys.smaccm.aadl2rtos.model.thread;

import edu.umn.cs.crisys.smaccm.aadl2rtos.model.port.*;


public class PortConnection {
	// increment a counter to maintain globally unique list of connection ids.
	private static int connectionIDCounter = 0;
	private int connectionID = 0;

	private String name;
	private ThreadInstance sourceThreadInstance;
	private ThreadInstance destThreadInstance;
	private OutputPort sourcePort; 
	private InputPort destPort;
	
	// MWW: Added for ground team to construct mailbox-type dataports
	private boolean useMailbox;
	
	public enum ConnectionType {
	    DATA_CONNECTION, EVENT_CONNECTION
	}
		
	public static void init() {
	  PortConnection.connectionIDCounter = 0;
	}
	
	public PortConnection(String name,
						ThreadInstance sourceThreadInstance, 
	                  ThreadInstance destThreadInstance, 
	                  OutputPort sourcePort, 
	                  InputPort destPort) {	
		this.name = name;
		this.connectionID = connectionIDCounter;
		this.sourceThreadInstance = sourceThreadInstance;
		this.destThreadInstance = destThreadInstance;
		this.sourcePort = sourcePort;
		this.destPort = destPort;
		this.useMailbox = false;
		
		PortConnection.connectionIDCounter++;
	}

	public ThreadInstance getSourceThreadInstance() {
	  return this.sourceThreadInstance;
	}
	
	public ThreadInstance getDestThreadInstance() {
	  return this.destThreadInstance;
	}

	public OutputPort getSourcePort() {
	  return this.sourcePort;
	}
	
	public InputPort getDestPort() {
	  return this.destPort;
	}
	
	public String getName() {
		return name;
	}
	
	public int getConnectionID() {
		return connectionID;
	}
	
	public boolean isMailbox() {
		return useMailbox;
	}
	
	public void setIsMailbox(boolean val) {
		useMailbox = val;
	}
}