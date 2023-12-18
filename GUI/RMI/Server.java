package RMI;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server implements serverInterface {

    private ArrayList<clientInterface> clients;
    private HashSet<String> user_names;
    private ArrayList<messageInterface> history;

    @Override
    public ArrayList<messageInterface> getHistory() throws RemoteException {
        System.out.println("Sending history");
        return history;
    }

    @Override
    public boolean CheckUserNameTaken(String UserName) throws RemoteException {
        return user_names.contains(UserName);
    }

    @Override
    public void addMessage(messageInterface msg) throws RemoteException {
        history.add(msg);
    }

    @Override
    public void deRegister(clientInterface client) throws RemoteException {
        clients.remove(client);
        String userName = client.getName();
        user_names.remove(userName);
        System.out.println(String.format("%s has left the room", client.getName()));
    }

    @Override
    public void registerClient(clientInterface client) throws RemoteException {
        try {
            clients.add(client);
            String UserName = client.getName();
            user_names.add(UserName);
            Message m = new Message("Welcome " + UserName, "Server", new HashMap<>());
            client.addMessage(m);
            if (!history.isEmpty()) {
                client.initVectorClocks(history.get(history.size() - 1).getVectorClocks());
            }
            System.out.println(String.format("%s registered on %s", UserName, RemoteServer.getClientHost()));
            for (clientInterface client1 : clients) {
                try {
                    client1.updateUserList(clients);
                } catch (ConnectException ex) {
                }
            }
        } catch (ServerNotActiveException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Server() {
        history = new ArrayList<>();
        clients = new ArrayList<>();
        user_names = new HashSet<>();
    }
}
