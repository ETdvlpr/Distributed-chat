package RMI;

import java.rmi.Remote; 
import java.rmi.RemoteException;  
import java.util.ArrayList;

// Creating Remote interface for our application 
public interface serverInterface extends Remote {  
   boolean CheckUserNameTaken(String UserName) throws RemoteException;
   void registerClient(clientInterface client) throws RemoteException;
   void deRegister(clientInterface client) throws RemoteException;
   void addMessage(messageInterface msg) throws RemoteException;
   ArrayList<messageInterface> getHistory() throws RemoteException;
}
