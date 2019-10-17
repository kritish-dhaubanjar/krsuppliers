package krsuppliers.stocks;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import krsuppliers.db.Database;
import krsuppliers.models.*;
import krsuppliers.pdf.Pdf;
import krsuppliers.pdf.PdfStock;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.ListIterator;

public class StockController {
    @FXML
    TableView<Stock> table;
    @FXML
    TableColumn<Stock, Integer> _id, _qty, _particular_id, _bill;
    @FXML
    TableColumn<Stock, Float>  _rate, _discount, _amount, _balance;
    @FXML
    TableColumn<Stock, String> _particular;
    @FXML
    TableColumn<Stock, Date> _date;
    @FXML
    DatePicker from, to;
    @FXML
    TextField pid;
    @FXML
    ComboBox<Particular> particular;
    @FXML
    JFXButton cancel, filter, print;
    @FXML
    JFXCheckBox _sales, _purchase, all_particulars;
    @FXML
    JFXSpinner busy, printing;

    private static ObservableList<Particular> particulars = FXCollections.observableArrayList();
    private static ObservableList<Transaction> transactions =  FXCollections.observableArrayList();
    private static ObservableList<Stock> stocks = FXCollections.observableArrayList();


    @FXML
    public void initialize(){
        setTableViewBinding();
        particulars.clear();
        stocks.clear();
        transactions.clear();
        clear();
        setParticulars();

        particular.setOnKeyReleased(e->{
            for(Particular p: particulars){
                if(p.getParticular().toLowerCase().startsWith(e.getText())){
                    particular.getSelectionModel().select(p);
                    break;
                }
            }
        });

        cancel.setOnAction(e->clear());
        filter.setOnAction(e->filter());
        print.setOnAction(e->printPdf());
        busy.setVisible(false);
        printing.setVisible(false);
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
        stocks.clear();
        transactions.clear();
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();

        if(all_particulars.isSelected()){
            getAllTransactions();
        }
        else if(start!=null && end!=null && !start.isAfter(end) && (particular.getValue() != null || (!pid.getText().isEmpty() && !pid.getText().equals("0")) )){
            int _pid = 0;
            if (particular.getValue()!=null){
                _pid = particular.getValue().get_id();
            }else if(!pid.getText().isEmpty()){
                _pid = Integer.parseInt(pid.getText());
            }

            if (_sales.isSelected() && _purchase.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC", "SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0  ORDER BY DATE ASC");
            }else if(_sales.isSelected() && !_purchase.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC", "");
            }else if(!_sales.isSelected() && _purchase.isSelected()){
                getTransactions("","SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC");
            }
        }
    }

    private void getTransactions(String salesQuery, String purchaseQuery){

        Task<ObservableList<Stock>> task = new Task<ObservableList<Stock>>() {
            @Override
            protected ObservableList<Stock> call() throws Exception {
                transactions.clear();
                try {
                    if(!purchaseQuery.isEmpty()) {
                        Statement query = Database.getConnection().createStatement();
                        ResultSet resultSet = query.executeQuery(purchaseQuery);
                        while (resultSet.next()) {
                            Purchase purchase = new Purchase(resultSet.getInt("_id"),
                                    resultSet.getDate("date"),
                                    resultSet.getInt("bill"),
                                    resultSet.getInt("particular_id"),
                                    resultSet.getString("particular"),
                                    resultSet.getInt("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(purchase);
                        }
                    }
                    if(!salesQuery.isEmpty()) {
                        Statement query = Database.getConnection().createStatement();
                        ResultSet resultSet = query.executeQuery(salesQuery);
                        while (resultSet.next()) {
                            Sale sale = new Sale(resultSet.getInt("_id"),
                                    resultSet.getDate("date"),
                                    resultSet.getInt("bill"),
                                    resultSet.getInt("particular_id"),
                                    resultSet.getString("particular"),
                                    0 - resultSet.getInt("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(sale);
                        }
                    }
                } catch (SQLException e) {
                    showErrorDialog(e.getMessage());
                }

                Collections.sort(transactions);

                int balance = 0;

                for(Transaction transaction: transactions){
                    Stock stock = new Stock(transaction.get_id(),
                            transaction.getDate(),
                            transaction.getBill(),
                            transaction.getParticular_id(),
                            transaction.getParticular(),
                            transaction.getQty(),
                            transaction.getRate(),
                            transaction.getDiscount(),
                            transaction.getAmount());

                    balance += transaction.getQty();
                    stock.setBalance(balance);
                    stocks.add(stock);
                }
                return stocks;
            }
        };

        busy.visibleProperty().bind(task.runningProperty());
        table.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    private void printPdf(){
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(LocalDateTime.now().toString() + ".pdf");
        File file = chooser.showSaveDialog(print.getScene().getWindow());
        if(file != null) {
            PdfStock pdf = new PdfStock(stocks, file);
            printing.visibleProperty().bind(pdf.runningProperty());
            pdf.start();
            pdf.setOnSucceeded(e -> {
                showInfoDialog("Completed","Pdf File Created.");
            });
            pdf.setOnFailed(e -> {
                showInfoDialog("Failed", "Failed to create Pdf File.");
            });
        }
    }

    private void setTableViewBinding(){
        table.setItems(stocks);
        _id.setCellValueFactory(new PropertyValueFactory<>("_id"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("particular_id"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        _balance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _bill.setCellValueFactory(new PropertyValueFactory<>("bill"));
    }

    private void getAllTransactions(){
        stocks.clear();
        transactions.clear();
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();

        if(start!=null && end!=null && !start.isAfter(end)){
            String queryString = "";
            if (_sales.isSelected() && _purchase.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount,cancel FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION SELECT _id,date, bill, particular_id,particular,-1 * qty AS qty,rate,discount,amount,cancel FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }else if(_sales.isSelected() && !_purchase.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular, qty * -1 AS qty,rate,discount,amount,cancel FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }else if(!_sales.isSelected() && _purchase.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount,cancel FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }

            final String _queryString = queryString;
            Task<ObservableList<Stock>> task = new Task<ObservableList<Stock>>() {
                @Override
                protected ObservableList<Stock> call() throws Exception {
                    try {
                        Statement query = Database.getConnection().createStatement();
                        ResultSet resultSet = query.executeQuery(_queryString);
                        while (resultSet.next()) {
                            Transaction t = new Transaction(resultSet.getInt("_id"),
                                    resultSet.getDate("date"),
                                    resultSet.getInt("bill"),
                                    resultSet.getInt("particular_id"),
                                    resultSet.getString("particular"),
                                    resultSet.getInt("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(t);
                        }
                    }catch (SQLException e){
                        showErrorDialog(e.getMessage());
                    }

                    int balance = 0;

                    for(int i=0; i<transactions.size(); i++){
                        Stock stock = new Stock(transactions.get(i).get_id(),
                                transactions.get(i).getDate(),
                                transactions.get(i).getBill(),
                                transactions.get(i).getParticular_id(),
                                transactions.get(i).getParticular(),
                                transactions.get(i).getQty(),
                                transactions.get(i).getRate(),
                                transactions.get(i).getDiscount(),
                                transactions.get(i).getAmount());
                        balance += transactions.get(i).getQty();
                        stock.setBalance(balance);
                        stocks.add(stock);
                        if(i< transactions.size() - 1)
                            if(transactions.get(i).getParticular_id() != transactions.get(i+1).getParticular_id())
                                balance = 0;
                    }
                    return stocks;
                }
            };

            busy.visibleProperty().bind(task.runningProperty());
            table.itemsProperty().bind(task.valueProperty());

            new Thread(task).start();
        }
    }

    private void clear(){
        particular.setValue(null);
        _sales.setSelected(true);
        _purchase.setSelected(true);
        pid.setText("0");
        particular.setValue(null);
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }

    private void showInfoDialog(String title, String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(info);
        alert.showAndWait();
    }

}
