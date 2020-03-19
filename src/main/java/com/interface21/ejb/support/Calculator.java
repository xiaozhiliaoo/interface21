
package com.interface21.ejb.support;

import java.rmi.RemoteException;

public interface Calculator {

	int getUncacheableValue() throws RemoteException;
	
	int getCacheableValue() throws RemoteException;
	
}

