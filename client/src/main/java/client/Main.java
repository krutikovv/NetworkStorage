package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sun.font.TrueTypeFont;

import java.awt.*;

public class Main extends Application {
//    Dimension sSize = Toolkit.getDefaultToolkit ().getScreenSize ();
//    Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
//
//    int taskBarHeight = sSize.height - winSize.height;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/sample.fxml"));
        primaryStage.setTitle("GeekChat");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
