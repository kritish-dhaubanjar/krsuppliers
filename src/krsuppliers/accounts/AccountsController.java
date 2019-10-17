package krsuppliers.accounts;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import javafx.concurrent.Task;
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
import java.util.concurrent.ExecutionException;

public class AccountsController {
    @FXML
    TextField database, user, password, host, port, newDatabase;
    @FXML
    JFXCheckBox confirm, populate_confirm;
    @FXML
    JFXButton save, create, populate;
    @FXML
    Text confirm_msg;
    @FXML
    JFXSpinner busy;

    private Map<String, String> config;

    @FXML
    public void initialize(){
        save.setOnAction(e->update());
        populate.setOnAction(e->populate_db());

        busy.setVisible(false);

        create.setDisable(true);
        create.setOnAction(e->create());
        confirm.setSelected(false);
        confirm.setOnAction(e->{
            if (confirm.isSelected())
                create.setDisable(false);
            else
                create.setDisable(true);
        });

        populate_confirm.setOnAction(e->{
            if (populate_confirm.isSelected())
                populate.setDisable(false);
            else
                populate.setDisable(true);
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

    private void populate_db(){
        if(populate_confirm.isSelected()){

            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    try {
                        Statement statement = Database.getConnection().createStatement();
                        statement.executeUpdate("USE " + database.getText().trim());
                        statement.execute("CREATE TABLE particulars (_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, particular TEXT NOT NULL)");
                        statement.execute("CREATE TABLE purchases(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, bill INTEGER NOT NULL DEFAULT 0, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate FLOAT NOT NULL, selling_rate FLOAT NOT NULL, discount FLOAT NOT NULL, amount FLOAT NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                        statement.execute("CREATE TABLE sales(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, bill INTEGER NOT NULL DEFAULT 0, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate FLOAT NOT NULL, discount FLOAT NOT NULL, amount FLOAT NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                        return "Tables created successfully!";
                    } catch (SQLException e) {
                        return e.getMessage();
                    }
                }
            };

            task.setOnSucceeded(e->{
                try {
                    String result = task.get();
                    if(result.equals("Database created successfully!")) {
                        showAlertDialog(result);
                    }else {
                        showErrorDialog(result);
                    }
                }catch (InterruptedException | ExecutionException err){
                    showErrorDialog(err.getMessage());
                }
            });


            busy.visibleProperty().bind(task.runningProperty());
            new  Thread(task).start();
        }
    }


    private void create(){

        if(confirm.isSelected()) {
            Task<String> task = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    try {
                        Statement statement = Database.getConnection().createStatement();
                        int num = statement.executeUpdate("CREATE DATABASE " + newDatabase.getText().trim());
                        if(num == 1) {
                            statement.executeUpdate("USE " + newDatabase.getText().trim());
                            statement.execute("CREATE TABLE particulars (_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, particular TEXT NOT NULL)");
                            statement.execute("CREATE TABLE purchases(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, bill INTEGER NOT NULL DEFAULT 0, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate FLOAT NOT NULL, selling_rate FLOAT NOT NULL, discount FLOAT NOT NULL, amount FLOAT NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                            statement.execute("CREATE TABLE sales(_id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT, date DATE NOT NULL, bill INTEGER NOT NULL DEFAULT 0, particular_id INTEGER NOT NULL, particular TEXT NOT NULL, qty INTEGER NOT NULL, rate FLOAT NOT NULL, discount FLOAT NOT NULL, amount FLOAT NOT NULL, cancel TINYINT NOT NULL DEFAULT 0)");
                            return "Database created successfully!";
                        }
                    } catch (SQLException e) {
                        return e.getMessage();
                    }
                    return "Can't create database!";
                }
            };

            task.setOnSucceeded(e->{
                try {
                    String result = task.get();
                    if(result.equals("Database created successfully!")) {
                        showAlertDialog(result);
                    }else {
                        showErrorDialog(result);
                    }
                }catch (InterruptedException | ExecutionException err){
                    showErrorDialog(err.getMessage());
                }
            });

            busy.visibleProperty().bind(task.runningProperty());
            new  Thread(task).start();
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
