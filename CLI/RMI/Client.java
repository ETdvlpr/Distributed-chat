package RMI;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

//must extend UnicastRemoteObject to be sent as paramater to remote method
public class Client extends UnicastRemoteObject implements clientInterface {

    private String userName;
    private HashMap<String, Integer> vectorClock;
    static serverInterface serverStub;
    private ArrayList<clientInterface> clients;
    private ArrayList<messageInterface> wait_queue;
    private boolean fetchingHistory;

    @Override
    public void broadCastMessage(String Message) throws RemoteException {
        vectorClock.put(userName, vectorClock.get(userName) + 1);
        Message msg = new Message(Message, userName, new HashMap<>(vectorClock));
        for (clientInterface client : clients) {
            try {
                client.addMessage(msg);
            } catch (ConnectException ex) {
            }
        }
        serverStub.addMessage(msg);
    }

    @Override
    public String getName() {
        return userName;
    }

    @Override
    public void addMessage(messageInterface m) throws RemoteException {
        if (sendNow(m)) {
            if (!m.getSender().equals(userName)) {
                System.out.println(String.format("\033[35m[%s]:\033[0m %s", m.getSender(), m.getText()));
            }
            if (!wait_queue.isEmpty()) {
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
        for (clientInterface client : clients) {
            try {
                String clientName = client.getName();
                updateClock(clientName, 0);
            } catch (RemoteException ex) {
            }
        }
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
    public void initVectorClocks(HashMap<String, Integer> vectorClocks) {
        vectorClocks.entrySet().forEach((entry) -> {
            updateClock(entry.getKey(), entry.getValue());
        });
    }

    private boolean sendNow(messageInterface msg) throws RemoteException {
        //if your vector clock of the sender is greater than the senders own clock
        //assume the message is already displayed
        if (vectorClock.get(msg.getSender()) != null && vectorClock.get(msg.getSender()) > msg.getVectorClocks().get(msg.getSender())) {
            return false;
        }
        for (Map.Entry<String, Integer> entry : msg.getVectorClocks().entrySet()) {
            String msgClient = entry.getKey();
            int msgClock = entry.getValue();
            // inspect all clocks except the senders
            if (!msgClient.equals(msg.getSender())) {
                updateClock(msgClient, 0); // create entry if not exist
                if (msgClock > vectorClock.get(msgClient)) {
// if clock of anyone else is greater than your own vector clock of that client, then delay message
                    wait_queue.add(msg);
                    if (wait_queue.size() > 3 && !fetchingHistory) {
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

    public Client(String userName) throws RemoteException {
        this.userName = userName;
        wait_queue = new ArrayList<>();
        vectorClock = new HashMap<>();
        vectorClock.put(userName, 0);
    }

    public HashMap<String, Integer> getVectorClock() {
        return vectorClock;
    }

    public static void main(String[] args) {
        String IP = (args.length > 1) ? args[1] : "localhost";
        try {

            // Getting the registry 
            Registry registry = LocateRegistry.getRegistry(IP, 1099);

            // Looking up the registry for the remote object 
            serverStub = (serverInterface) registry.lookup("Chat");

            Scanner in = new Scanner(System.in);
            System.out.print("Enter your name: ");
            String userName = in.nextLine().trim();

            while (serverStub.CheckUserNameTaken(userName)) {
                System.out.print("The user name '" + userName + "' is taken, please choose a different one: ");
                userName = in.nextLine().trim();
            }
            Client c = new Client(userName);
            // Calling the remote method using the obtained object 
            serverStub.registerClient(c);

            String message = in.nextLine().trim();
            while (!message.equals("Quit") || !message.equals("quit")) {
                c.broadCastMessage(message);
                message = in.nextLine().trim();
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
