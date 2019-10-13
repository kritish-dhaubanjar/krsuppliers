package krsuppliers;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("resources/icons/theGimmickBox.png")));
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("KR Suppliers");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 1024, 640));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }

}
