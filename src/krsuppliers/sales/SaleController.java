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
import java.time.LocalDate;
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
    @FXML
    DatePicker from, to;

    private ObservableList<Sale> sales = FXCollections.observableArrayList();

    @FXML
    public void initialize(){
        setTableViewBinding();
        filter.setOnAction((e)-> filterRecords());
        retrieve("SELECT * FROM sales ORDER BY _id DESC LIMIT 20");
    }

    private void setTableViewBinding(){
        table.setItems(sales);                                                  /* Observable List */
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));            /* Property Values */
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    private void filterRecords(){
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        if(start!=null && end!=null && start.isBefore(end)){
            retrieve("SELECT * FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "'");
        }
    }

    private void retrieve(String queryString){
        try {
            Statement query = Database.getConnection().createStatement();
            ResultSet results = query.executeQuery(queryString);
            sales.clear();
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
}
