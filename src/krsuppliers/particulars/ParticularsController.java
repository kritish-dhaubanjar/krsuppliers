package krsuppliers.particulars;

import com.jfoenix.controls.JFXButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import krsuppliers.db.Database;
import krsuppliers.models.Particular;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ParticularsController {
    @FXML
    TableView<Particular> table;
    @FXML
    TableColumn<Particular, Integer> _particular_id;
    @FXML
    TableColumn<Particular, String> _particular;
    @FXML
    JFXButton save, cancel;
    @FXML
    TextField name;

    int pid = 0;

    enum actions {SAVE, UPDATE}

    private final static ObservableList<Particular> particulars = FXCollections.observableArrayList();
    private actions action = actions.SAVE;

    @FXML
    public void initialize(){
        setTableViewBinding();
        particulars.clear();
        setParticulars();
        clear();
        cancel.setOnAction(e->clear());
        action = actions.SAVE;
        save.setOnAction(e->save());
    }

    private void save(){
        String _name = name.getText();
        try {
            Statement statement = Database.getConnection().createStatement();
            if(action == actions.UPDATE && pid!=0) {
                int num = statement.executeUpdate("UPDATE particulars SET particular = '" + _name + "' WHERE _id = " + pid);
                if(num == 1){
                    statement.executeUpdate("UPDATE sales SET particular = '" + _name +"' WHERE particular_id = " + pid);
                    statement.executeUpdate("UPDATE purchases SET particular = '" + _name +"' WHERE particular_id = " + pid);
                    clear();
                }else{
                    showErrorDialog("Oops! Something went wrong!");
                }
                pid = 0;
                action = actions.SAVE;
                setParticulars();
            }else if(action == actions.SAVE){
                int num = statement.executeUpdate("INSERT INTO particulars(particular) VALUES('" + _name + "')");
                if (num == 1) {
                    setParticulars();
                    clear();
                }else{
                    showErrorDialog("Oops! Something went wrong!");
                }
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void save(Particular p){
        name.setText(p.getParticular());
        pid = p.get_id();
        action = actions.UPDATE;
    }



    private void setTableViewBinding(){
        ContextMenu contextMenu = new ContextMenu();

        MenuItem edit = new MenuItem("Edit");

        edit.setOnAction(e->save(table.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(edit);

        table.setRowFactory(e->{
            TableRow<Particular> p = new TableRow<>();
            p.emptyProperty().addListener((observable, oldValue, newValue) -> {
                p.setContextMenu(contextMenu);
            });
            return p;
        });

        table.setItems(particulars);
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("_id"));
    }

    private void setParticulars(){
        try{
            particulars.clear();
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery("SELECT * FROM particulars");

            while (resultSet.next()){
                particulars.add(new Particular(
                        resultSet.getInt("_id"),
                        resultSet.getString("particular"))
                );
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void clear(){
        name.setText("");
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }


}
