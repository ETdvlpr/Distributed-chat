package RMI;


import UI.UI;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

//must extend UnicastRemoteObject to be sent as paramater to remote method
public class Client extends UnicastRemoteObject implements clientInterface {

    private String userName;
    private HashMap<String, Integer> vectorClock;
    private serverInterface serverStub;
    private ArrayList<clientInterface> clients;
    private ArrayList<messageInterface> wait_queue;
    private HashSet<String> active_users;
    private boolean fetchingHistory;

    @Override
    public void broadCastMessage(String Message) throws RemoteException {
        active_users.clear();
        vectorClock.put(userName, vectorClock.get(userName) + 1);
        Message msg = new Message(Message, userName, new HashMap<>(vectorClock));
        for (clientInterface client : clients) {
            try {
                client.addMessage(msg);
                active_users.add(client.getName());
            } catch (ConnectException ex) {
            }
        }
        serverStub.addMessage(msg);
        UI.showUsers();
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public void addMessage(messageInterface m) throws RemoteException {
        if(sendNow(m)){
            UI.showUsers();
            UI.showMessage(m);
            if(!wait_queue.isEmpty()){
                for (Iterator<messageInterface> it = wait_queue.iterator(); it.hasNext();) {
                    addMessage(it.next());
                }
            }
        }
//        for (Map.Entry<String, Integer> entry : vectorClock.entrySet()) {
//            System.out.println(entry.getKey() + " : " + entry.getValue());
//        }
    }

    @Override
    public void updateUserList(ArrayList<clientInterface> clients) {
        this.clients = clients;
        active_users.clear();
        for (clientInterface client : clients) {
            try {
                String clientName = client.getName();
                updateClock(clientName, 0);
                active_users.add(clientName);
            } catch (RemoteException ex) {
            }
        }
        UI.showUsers();
    }

    private void updateClock(String name, Integer value) {
        if (!vectorClock.containsKey(name)) {
            vectorClock.put(name, 0);
        } else {
            if (vectorClock.get(name) < value) { // update your clock if clock sent has increased
                vectorClock.put(name, value);
            }
        }
    }

    @Override
    public void initVectorClocks(HashMap<String, Integer> vectorClocks){
        vectorClocks.entrySet().forEach((entry) -> {
            updateClock(entry.getKey(), entry.getValue());
        });
    }

    private boolean sendNow(messageInterface msg) throws RemoteException {
        //if your vector clock of the sender is greater than the senders own clock
        //assume the message is already displayed
        if(vectorClock.get(msg.getSender()) != null && vectorClock.get(msg.getSender()) > msg.getVectorClocks().get(msg.getSender())) 
            return false;
        for (Map.Entry<String, Integer> entry : msg.getVectorClocks().entrySet()) {
            String msgClient = entry.getKey();
            int msgClock = entry.getValue();
            // inspect all clocks except the senders
            if (!msgClient.equals(msg.getSender())) {
                updateClock(msgClient, 0); // create entry if not exist
                if (msgClock > vectorClock.get(msgClient)) {
// if clock of anyone else is greater than your own vector clock of that client, then delay message
                    wait_queue.add(msg);
                    if(wait_queue.size() > 3 && !fetchingHistory){
                        fetchingHistory = true;
                        ArrayList<messageInterface> x = serverStub.getHistory();
                        for (messageInterface mHistory : x) {
                            addMessage(mHistory);
                        }
                        fetchingHistory = false;
                    }
                    return false;
                }
            }
        }
        // update sender clock to whatever value sent
        updateClock(msg.getSender(), msg.getVectorClocks().get(msg.getSender()));
        return true;
    }

    public Client(String userName, serverInterface serverStub) throws RemoteException {
        this.userName = userName;
        this.serverStub = serverStub;
        active_users = new HashSet<>();
        wait_queue = new ArrayList<>();
        vectorClock = new HashMap<>();
        vectorClock.put(userName, 0);
    }

    public HashSet<String> getActiveUsers() {
        return active_users;
    }

    public HashMap<String, Integer> getVectorClock() {
        return vectorClock;
    }
}
