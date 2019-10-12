package krsuppliers.sales;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;
import krsuppliers.db.Database;
import krsuppliers.models.Sale;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

public class SaleController {
    @FXML
    Button save, delete, filter;
    @FXML
    TextField rate, qty, discount;
    @FXML
    Text total;
    @FXML
    TableView<Sale>table;
    @FXML
    TableColumn<Sale, Integer> _qty, _amount, _discount, _rate;
    @FXML
    TableColumn<Sale, String> _particular;
    @FXML
    TableColumn<Sale, Date> _date;

    private ObservableList<Sale> sales = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        setTableViewBinding();

        try {
            Statement query = Database.getConnection().createStatement();
            ResultSet results = query.executeQuery("SELECT * FROM sales LIMIT 30");
            int _total = 0;
            while (results.next()){
                sales.add(new Sale(results.getInt("_id"),
                        results.getDate("date"),
                        results.getInt("particular_id"),
                        results.getString("particular"),
                        results.getInt("qty"),
                        results.getInt("rate"),
                        results.getInt("discount"),
                        results.getInt("amount")));
                _total += results.getInt("amount");
            }

            total.setText(String.valueOf("Rs " + _total));
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void setTableViewBinding(){
        table.setItems(sales);
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
    }
}
