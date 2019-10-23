package krsuppliers.particulars;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import krsuppliers.db.Database;
import krsuppliers.models.Balance;
import krsuppliers.models.DateConverter;
import krsuppliers.models.Particular;

import javax.xml.crypto.Data;
import java.sql.*;
import java.time.LocalDate;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class ParticularsController {
    @FXML
    TableView<Balance> table;
    @FXML
    TableColumn<Balance, Integer> _id, _particular_id, _bill;
    @FXML
    TableColumn<Balance, String> _particular;
    @FXML
    TableColumn<Balance, Float> _qty, _rate, _discount, _amount, _selling_rate;
    @FXML
    TableColumn<Balance, Date> _date;
    @FXML
    JFXButton save, cancel;
    @FXML
    TextField particular, bill, qty, rate, discount;
    @FXML
    DatePicker date;
    @FXML
    JFXSpinner busy;

    private int pid = 0;

    enum actions {SAVE, UPDATE}

    private static ObservableList<Balance> particulars = FXCollections.observableArrayList();
    private actions action = actions.SAVE;

    @FXML
    public void initialize(){
        setTableViewBinding();
        setParticulars();
        particulars.clear();
        clear();
        cancel.setOnAction(e->clear());
        action = actions.SAVE;
        save.setOnAction(e->save());
    }

    private void save(){
        String _particular = particular.getText();
        Date date_ = Date.valueOf(date.getValue());
        float qty_ = Float.parseFloat(qty.getText());
        int bill_ = Integer.parseInt(bill.getText());
        float rate_ = Float.parseFloat(rate.getText());
        float discount_ = Float.parseFloat(discount.getText());
        float amt = qty_ * rate_ - discount_;

        try {
            if(action == actions.UPDATE && pid!=0) {
                PreparedStatement statement = Database.getConnection().prepareStatement("UPDATE particulars SET particular = ? WHERE _id = ?");

                statement.setString(1, _particular);
                statement.setInt(2, pid);

                int num = statement.executeUpdate();
                if(num == 1){
                    statement = Database.getConnection().prepareStatement("UPDATE balance SET date = ?, bill = ?, particular = ?, qty = ?, rate = ?, discount = ?, amount = ? WHERE particular_id = ?");
                    statement.setDate(1, date_);
                    statement.setInt(2, bill_);
                    statement.setString(3, _particular);
                    statement.setFloat(4, qty_);
                    statement.setFloat(5, rate_);
                    statement.setFloat(6, discount_);
                    statement.setFloat(7, amt);
                    statement.setInt(8, pid);
                    num = statement.executeUpdate();
                }
                if(num == 1){
                    statement.executeUpdate("UPDATE sales SET particular = '" + _particular +"' WHERE particular_id = " + pid);
                    statement.executeUpdate("UPDATE purchases SET particular = '" + _particular +"' WHERE particular_id = " + pid);
                    clear();
                }else{
                    showErrorDialog("Oops! Something went wrong!");
                }
                pid = 0;
                action = actions.SAVE;
                setParticulars();
            }else if(action == actions.SAVE){
                PreparedStatement statement = Database.getConnection().prepareStatement("INSERT INTO particulars(particular) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, _particular);
                statement.execute();
                ResultSet resultSet = statement.getGeneratedKeys();
                if(resultSet.next()){
                    statement = Database.getConnection().prepareStatement("INSERT INTO balance(date, bill,  particular_id, particular, qty, rate, discount, amount) VALUES(?,?,?,?,?,?,?,?)");
                    statement.setDate(1, date_);
                    statement.setInt(2, bill_);
                    statement.setInt(3, resultSet.getInt(1));
                    statement.setString(4, _particular);
                    statement.setFloat(5, qty_);
                    statement.setFloat(6, rate_);
                    statement.setFloat(7, discount_);
                    statement.setFloat(8, amt);
                }

                int num = statement.executeUpdate();
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

    private void save(Balance p){
        particular.setText(p.getParticular());
        date.setValue(p.getDate().toLocalDate());
        bill.setText(String.valueOf(p.getBill()));
        rate.setText(String.valueOf(p.getRate()));
        discount.setText(String.valueOf(p.getDiscount()));
        qty.setText(String.valueOf(p.getQty()));
        pid = p.get_id();
        action = actions.UPDATE;
    }



    private void setTableViewBinding(){
        ContextMenu contextMenu = new ContextMenu();

        MenuItem edit = new MenuItem("Edit");

        edit.setOnAction(e->save(table.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(edit);

        table.setRowFactory(e->{
            TableRow<Balance> p = new TableRow<>();
            p.emptyProperty().addListener((observable, oldValue, newValue) -> {
                p.setContextMenu(contextMenu);
            });
            return p;
        });

        table.setItems(particulars);
        _id.setCellValueFactory(new PropertyValueFactory<>("_id"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("particular_id"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _bill.setCellValueFactory(new PropertyValueFactory<>("bill"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _selling_rate.setCellValueFactory(new PropertyValueFactory<>("selling_rate"));
    }

    private void setParticulars(){
        particulars.clear();

        Task<ObservableList<Balance>> task = new Task<ObservableList<Balance>>() {
            @Override
            protected ObservableList<Balance> call() throws Exception {
                try{
                    Statement query = Database.getConnection().createStatement();
                    ResultSet resultSet = query.executeQuery("SELECT * FROM balance");

                    while (resultSet.next()){
                        particulars.add(new Balance(
                                        resultSet.getInt("_id"),
                                        resultSet.getDate("date"),
                                        resultSet.getInt("bill"),
                                        resultSet.getInt("particular_id"),
                                        resultSet.getString("particular"),
                                        resultSet.getFloat("qty"),
                                        resultSet.getFloat("rate"),
                                        resultSet.getFloat("discount"),
                                        resultSet.getFloat("amount")
                                )
                        );
                    }
                    return particulars;
                }catch (SQLException e){
                    showErrorDialog(e.getMessage());
                }
                return null;
            }
        };

        busy.visibleProperty().bind(task.runningProperty());
        table.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    private void clear(){
        particular.setText("");
        bill.setText("0");
        qty.setText("0");
        rate.setText("0");
        discount.setText("0");

        date.setConverter(new DateConverter(date));
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }


}
