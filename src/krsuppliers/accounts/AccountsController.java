package krsuppliers.accounts;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import krsuppliers.Main;
import krsuppliers.db.Configuration;
import krsuppliers.db.Database;

import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.Optional;

public class AccountsController {
    @FXML
    TextField database, user, password, host, port, newDatabase;
    @FXML
    CheckBox confirm;
    @FXML
    Button save, create;
    @FXML
    Text confirm_msg;

    private Map<String, String> config;

    @FXML
    public void initialize(){
        save.setOnAction(e->update());
        create.setDisable(true);
        create.setOnAction(e->create());
        confirm.setSelected(false);
        confirm.setOnAction(e->{
            if (confirm.isSelected())
                create.setDisable(false);
            else
                create.setDisable(true);
        });
        try {
            this.config = Configuration.getConfiguration().getConfig();
            this.database.setText(config.get("database"));
            this.user.setText(config.get("username"));
            this.password.setText(config.get("password"));
            this.host.setText(config.get("host"));
            this.port.setText(config.get("port"));
        }catch (IOException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void update(){
        try {
            Configuration.getConfiguration().updateConfiguration(user.getText(), password.getText(), database.getText(), host.getText(), port.getText());

                Main.stage.close();

                try {
                    Main main = new Main();
                    main.start(new Stage());
                }catch (Exception e) {
                    System.out.println(e.getMessage());
                }


        }catch (IOException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void create(){
        if(confirm.isSelected()) {
            try {
                Statement statement = Database.getConnection().createStatement();
                int num = statement.executeUpdate("CREATE DATABASE " + newDatabase.getText().trim());
                if(num == 1){
                    statement.executeUpdate("USE " + newDatabase.getText().trim());
                    statement.execute("CREATE TABLE particulars (_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, particular TEXT NOT NULL)");
                    statement.execute("CREATE TABLE purchases(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate INTEGER NOT NULL, discount INTEGER NOT NULL, amount INTEGER NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                    statement.execute("CREATE TABLE sales(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate INTEGER NOT NULL, discount INTEGER NOT NULL, amount INTEGER NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                    showAlertDialog("Database created successfully!");
                }else{
                    showErrorDialog("Can't create database!");
                }
            } catch (SQLException e) {
                showErrorDialog(e.getMessage());
            }
        }else{

        }
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }

    private void showAlertDialog(String info){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Database created");
        alert.setHeaderText(info);
        Optional<ButtonType> button = alert.showAndWait();
    }
}
