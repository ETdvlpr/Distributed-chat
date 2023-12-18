package RMI;

import java.rmi.Remote; 
import java.rmi.RemoteException;  
import java.util.HashMap;

// Creating Remote interface for our application 
public interface messageInterface extends Remote {
    String getText() throws RemoteException;
    String getSender() throws RemoteException;
    String getTimeStamp()throws RemoteException;
    HashMap<String, Integer> getVectorClocks() throws RemoteException;
}
