package krsuppliers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import krsuppliers.db.Database;

public class AuthController {
    @FXML
    Button login;
    @FXML
    TextField username;
    @FXML
    PasswordField password;

    @FXML
    public void inititalize(){
        login.setOnAction(e->{
            String _username = username.getText();
            String _password = password.getText();
        });
    }
}
