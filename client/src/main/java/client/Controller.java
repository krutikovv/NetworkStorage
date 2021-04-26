package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private HBox authPanel;
    @FXML
    private HBox msgPanel;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ListView<String> clientList;
    @FXML
    private ListView<String> serverList;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private HBox controlPanel;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private RegController regController;
    private StringBuilder dirClient = new StringBuilder("C:");
    private StringBuilder dirServer = new StringBuilder("e:\\JAVA_projects\\NetworkStorage\\server\\localserver\\");

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        serverList.setVisible(authenticated);
        serverList.setManaged(authenticated);
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        controlPanel.setVisible(authenticated);
        controlPanel.setManaged(authenticated);
        if (!authenticated) {
            nickname = "";
        }
        setTitle(nickname);
        refreshList(clientList, dirClient);
        refreshList(serverList, dirServer);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(this::run);
        setAuthenticated(false);
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.startsWith(Command.AUTH_OK)) {
                                nickname = str.split("\\s")[1];
                                setAuthenticated(true);
                                break;
                            }

                            if (str.equals(Command.END)) {
                                System.out.println("client disconnected");
                                throw new RuntimeException("server disconnected us");
                            }

                            if (str.equals(Command.REG_OK)) {
                                regController.regOk();
                            }

                            if (str.equals(Command.REG_NO)) {
                                regController.regNo();
                            }
                        }  //                            textArea.appendText(str + "\n");

                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                System.out.println("client disconnected");
                                break;
                            }
                        } else {
//                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(Command.END);
        } catch (IOException e) {
            e.printStackTrace();
        }
        textField.clear();
            textField.requestFocus();
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("%s %s %s", Command.AUTH, loginField.getText().trim(), passwordField.getText().trim());

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        if (nickname.equals("")) {
            Platform.runLater(() -> {
                stage.setTitle("NetworkStorage");
            });
        } else {
            Platform.runLater(() -> {
                stage.setTitle(String.format("NetworkStorage [ %s ]", nickname));
            });
        }
    }

    public void registration(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        regStage.show();
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 400, 350));
            regController = fxmlLoader.getController();
            regController.setController(this);
            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("%s %s %s %s", Command.REG, login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientListClicked(MouseEvent mouseEvent) {
        String selectedItem = clientList.getSelectionModel().getSelectedItem();
        dirClient.append("\\" + selectedItem);
        refreshList(clientList, dirClient);
    }

    private void refreshList(ListView list, StringBuilder dirList) {
        list.getItems().clear();
        textField.setText(dirList.toString());
        Path path = Paths.get(dirList.toString());
        File dir = new File(String.valueOf(path)); //path указывает на директорию
        List<File> lst = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                lst.add(file);
                list.getItems().add(file.getName());
            }
        }
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                lst.add(file);
                list.getItems().add(file.getName());
            }
        }
    }

    public void serverListClicked(MouseEvent mouseEvent) {
        String selectedItem = serverList.getSelectionModel().getSelectedItem();
        dirServer.append("\\" + selectedItem);
        refreshList(serverList, dirServer);
    }

    public void copyFileDir(ActionEvent actionEvent) {
    }

    public void createDir(ActionEvent actionEvent) {
    }

    public void backFileDir(ActionEvent actionEvent) {
        dirClient.delete(dirClient.lastIndexOf("\\"), dirClient.length());
        refreshList(clientList, dirClient);
    }

    private void run() {
        stage = (Stage) textField.getScene().getWindow();
        stage.setOnCloseRequest(event -> {
            System.out.println("bye");
            if (socket != null && !socket.isClosed()) {
                try {
                    out.writeUTF(Command.END);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
