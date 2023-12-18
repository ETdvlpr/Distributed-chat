package RMI;

import java.rmi.Remote; 
import java.rmi.RemoteException;  
import java.util.ArrayList;
import java.util.HashMap;

// Creating Remote interface for our application 
public interface clientInterface extends Remote {  
   void addMessage(messageInterface m) throws RemoteException;
   void updateUserList(ArrayList<clientInterface> clients) throws RemoteException;
   String getName() throws RemoteException;
   void broadCastMessage(String Message) throws RemoteException;
   void initVectorClocks(HashMap<String, Integer> vectorClocks) throws RemoteException;
}
