package krsuppliers.stocks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import krsuppliers.db.Database;
import krsuppliers.models.Particular;
import krsuppliers.models.Stock;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;

public class StockController {
    @FXML
    TableView<Stock> table;
    @FXML
    TableColumn<Stock, Integer> _id, _qty, _rate, _particular_id;
    @FXML
    TableColumn<Stock, String> _particular;
    @FXML
    TableColumn<Stock, Date> _date;
    @FXML
    DatePicker from, to;
    @FXML
    TextField pid;
    @FXML
    Text total;
    @FXML
    ComboBox<Particular> particular;
    @FXML
    Button cancel, filter;
    @FXML
    CheckBox _sales, _purchase;

    private static ObservableList<Particular> particulars = FXCollections.observableArrayList();
    private static ObservableList<Stock> sales = FXCollections.observableArrayList();
    private static ObservableList<Stock> purchases = FXCollections.observableArrayList();
    private static ObservableList<Stock> stocks = FXCollections.observableArrayList();

    private int balance_qty = 0;

    @FXML
    public void initialize(){
        setTableViewBinding();
        particulars.clear();
        stocks.clear();
        purchases.clear();
        sales.clear();
        clear();
        setParticulars();

        cancel.setOnAction(e->clear());
        filter.setOnAction(e->filter());

        from.setValue(LocalDate.now());
        to.setValue(LocalDate.now());
    }

    private void setParticulars(){
        try{
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery("SELECT * FROM particulars");

            while (resultSet.next()){
                particulars.add(new Particular(
                        resultSet.getInt("_id"),
                        resultSet.getString("particular"))
                );
            }
            particular.getItems().addAll(particulars);
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void filter(){
        balance_qty = 0;
        stocks.clear();
        purchases.clear();
        sales.clear();
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        if(start!=null && end!=null && !start.isAfter(end) && (particular.getValue() != null || !pid.getText().isEmpty() )){
            int _pid = 0;
            if (particular.getValue()!=null){
                _pid = particular.getValue().get_id();
            }else if(!pid.getText().isEmpty()){
                _pid = Integer.parseInt(pid.getText());
            }

            if (_sales.isSelected() && _purchase.isSelected()){
                getSales("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
                getPurchases("SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
                stocks.addAll(sales);
                stocks.addAll(purchases);
                Collections.sort(stocks);
            }else if(_sales.isSelected() && !_purchase.isSelected()){
                getSales("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
                stocks.addAll(sales);
            }else if(!_sales.isSelected() && _purchase.isSelected()){
                getPurchases("SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
                stocks.addAll(purchases);
            }
            total.setText(String.valueOf(balance_qty));
            balance_qty = 0;
        }
    }

    private void getPurchases(String queryString){
        try {
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery(queryString);
            purchases.clear();
            while (resultSet.next()){
                purchases.add(new Stock(resultSet.getInt("_id"),
                        resultSet.getDate("date"),
                        resultSet.getInt("particular_id"),
                        resultSet.getString("particular"),
                        resultSet.getInt("qty"),
                        resultSet.getInt("rate")));
                balance_qty += resultSet.getInt("qty");
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void getSales(String queryString){
        try {
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery(queryString);
            sales.clear();
            while (resultSet.next()){
                sales.add(new Stock(resultSet.getInt("_id"),
                        resultSet.getDate("date"),
                        resultSet.getInt("particular_id"),
                        resultSet.getString("particular"),
                        0-resultSet.getInt("qty"),
                        resultSet.getInt("rate")));
                balance_qty -= resultSet.getInt("qty");
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void setTableViewBinding(){
        table.setItems(stocks);
        _id.setCellValueFactory(new PropertyValueFactory<>("_id"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("particular_id"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    private void clear(){
        particular.setValue(null);
        _sales.setSelected(true);
        _purchase.setSelected(true);
        pid.setText("0");
        particular.setValue(null);
        balance_qty = 0;
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }



}
