package RMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

//message template object
public class Message extends UnicastRemoteObject implements messageInterface{

    private String text, sender;
    private HashMap<String, Integer> vectorClocks;
    
    public Message(String text, String sender, HashMap<String, Integer> vectorClocks) throws RemoteException{
        this.text = text;
        this.sender = sender;
        this.vectorClocks = vectorClocks;
    }
    /**
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * @return the sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the vectorClocks
     */
    public HashMap<String, Integer> getVectorClocks() {
        return vectorClocks;
    }
}
