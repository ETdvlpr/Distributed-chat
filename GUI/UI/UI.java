package UI;


import RMI.serverInterface;
import RMI.Client;
import RMI.messageInterface;
import RMI.Server;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Dave
 */
public class UI extends Application {

    private static TextArea messageArea;
    private static TableView<ActiveUser> userTable;
    private TableColumn<ActiveUser, String> userColumn;
    private TableColumn<ActiveUser, ImageView> activeColumn;
    private TextArea sendArea;
    private Button sendBtn, loginBtn;
    private TextField userNameFld;
    private Stage primaryStage;
    private static Label errorLbl;
    private static Client client;
    private static serverInterface serverStub;
    public static Image active_image, inactive_image;

    public static void main(String[] args) {
        String IP = (args.length > 1) ? args[1] : "localhost";
        try {
            // Getting the registry 
            Registry registry = LocateRegistry.getRegistry(IP, 1099);
            // Looking up the registry for the remote object 
            serverStub = (serverInterface) registry.lookup("Chat");

            launch();
        } catch (ConnectException e) {
            try {
                // Instantiating the implementation class 
                Server Server = new Server();
                Registry registry = LocateRegistry.createRegistry(1099);

                // (here we are exporting the remote object to the stub) 
                serverInterface stub = (serverInterface) UnicastRemoteObject.exportObject(Server, 1099);
                // Binding the remote object (stub) in the registry 
                registry.bind("Chat", stub);
                System.err.println("Server ready");
            } catch (AlreadyBoundException | RemoteException ex) {
                System.err.println("Server exception: " + ex.toString());
                ex.printStackTrace();
            }
        } catch (NotBoundException | RemoteException e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        userNameFld = new TextField();
        userNameFld.setPrefSize(260, 50);
        userNameFld.setLayoutX(185);
        userNameFld.setLayoutY(102);
        userNameFld.setFont(new Font(20));
        userNameFld.setPromptText("User name");
        loginBtn = new Button("Login");
        loginBtn.setLayoutX(290);
        loginBtn.setLayoutY(188);
        errorLbl = new Label();
        errorLbl.setTextFill(Paint.valueOf("#FF0000"));
        errorLbl.setLayoutX(14);
        errorLbl.setLayoutY(378);
        AnchorPane root = new AnchorPane(userNameFld, loginBtn, errorLbl);
        Scene scene = new Scene(root, 650, 400);

        loginBtn.setOnAction(new loginHandler());

        primaryStage = new Stage();
        primaryStage.setScene(scene);
        primaryStage.setTitle("CHAT ROOM");
        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            try {
                serverStub.deRegister(client);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
            Platform.exit();
            System.exit(0);
        });
        primaryStage.show();
        this.primaryStage = primaryStage;
    }

    private Scene getChatScene() {
        active_image = new Image("UI/active.png");
        inactive_image = new Image("UI/inactive.png");
        AnchorPane root = new AnchorPane();
        userTable = new TableView<>();
        userTable.setPrefSize(150, 292);
        userTable.setLayoutX(14);
        userTable.setLayoutY(14);
        userColumn = new TableColumn<>("Live Users");
        userColumn.setPrefWidth(127);
        userColumn.setCellValueFactory(
                new PropertyValueFactory<>("Name")
        );
        activeColumn = new TableColumn<>();
        activeColumn.setPrefWidth(20);
        activeColumn.setCellValueFactory(
                new PropertyValueFactory<>("Active")
        );
        userTable.getColumns().add(activeColumn);
        userTable.getColumns().add(userColumn);
        messageArea = new TextArea();
        messageArea.setPrefSize(430, 292);
        messageArea.setLayoutX(178);
        messageArea.setLayoutY(14);
        messageArea.setEditable(false);
        sendArea = new TextArea();
        sendArea.setPrefSize(468, 57);
        sendArea.setLayoutX(14);
        sendArea.setLayoutY(318);
        sendBtn = new Button("Send");
        sendBtn.setPrefSize(112, 57);
        sendBtn.setLayoutX(490);
        sendBtn.setLayoutY(318);
        errorLbl = new Label();
        errorLbl.setTextFill(Paint.valueOf("#FF0000"));
        errorLbl.setLayoutX(14);
        errorLbl.setLayoutY(378);

        sendBtn.setOnAction((ActionEvent event) -> {
            try {
                String text = sendArea.getText().trim();
                if (!text.equals("")) {
                    client.broadCastMessage(text);
                }
            } catch (RemoteException ex) {
                showError(ex.getMessage());
                ex.printStackTrace();
            }
        });

        root.getChildren().add(userTable);
        root.getChildren().add(messageArea);
        root.getChildren().add(sendArea);
        root.getChildren().add(sendBtn);
        root.getChildren().add(errorLbl);
        Scene scene = new Scene(root, 630, 400);
        return scene;
    }

    private class loginHandler implements EventHandler<ActionEvent> {

        public loginHandler() {
        }

        @Override
        public void handle(ActionEvent event) {
            if (userNameFld.getText().trim().equals("")) {
                errorLbl.setText("Please, fill in a valid User Name");
            } else {
                try {
                    String userName = userNameFld.getText().trim();
                    if (serverStub.CheckUserNameTaken(userName)) {
                        errorLbl.setText("User name '" + userName + "' is taken, please choose a different one.");
                    } else {
                        primaryStage.setTitle("CHAT ROOM [" + userName + "]");
                        primaryStage.setScene(getChatScene());
                        client = new Client(userName, serverStub);
                        // Calling the remote method using the obtained object
                        serverStub.registerClient(client);
                    }
                } catch (RemoteException ex) {
                    errorLbl.setText(ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void showMessage(messageInterface m) {
        try {
            if (!m.getSender().equals(client.getName())) {
                messageArea.appendText(String.format("\n[%s] %s \n\t%s", m.getSender(), m.getTimeStamp().split(" ")[1], m.getText().replace("\n", "\n\t")));
            } else {
                messageArea.appendText(String.format("\n[%s]: %s \n\t%s", m.getSender(), m.getTimeStamp().split(" ")[1], m.getText().replace("\n", "\n\t")));
            }
        } catch (RemoteException ex) {
            showError(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void showUsers() {
        ObservableList<ActiveUser> userList = FXCollections.observableArrayList();
        client.getVectorClock().entrySet().forEach((entry) -> {
            String name = entry.getKey();
            int clockValue = entry.getValue();
// we don't track the servers logical clock as it usually contacts nodes individually and not through multicast
            if (!name.equals("Server")) { 
                userList.add(new ActiveUser(name, clockValue, client.getActiveUsers().contains(name)));
            }
        });
        userTable.setItems(userList);
    }

    public static void showError(String error) {
        errorLbl.setText(error);
    }
}
