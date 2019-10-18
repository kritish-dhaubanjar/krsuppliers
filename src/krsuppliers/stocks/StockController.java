package krsuppliers.stocks;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXToggleButton;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import krsuppliers.db.Database;
import krsuppliers.models.*;
import krsuppliers.pdf.PdfStock;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    DatePicker from, to, close_date;
    @FXML
    TextField pid;
    @FXML
    ComboBox<Particular> particular;
    @FXML
    JFXButton cancel, filter, print, close;
    @FXML
    JFXToggleButton close_preview;
    @FXML
    JFXCheckBox _sales, _purchase, all_particulars, _stock, confirm_close;
    @FXML
    JFXSpinner busy, printing;

    private static ObservableList<Particular> particulars = FXCollections.observableArrayList();
    private static ObservableList<Transaction> transactions =  FXCollections.observableArrayList();
    private static ObservableList<Stock> stocks = FXCollections.observableArrayList();
    private static ObservableList<Stock> closingStock = FXCollections.observableArrayList();


    @FXML
    public void initialize(){
        setTableViewBinding();
        particulars.clear();
        stocks.clear();
        transactions.clear();
        {
            confirm_close.setDisable(true);
            close_date.setConverter(new DateConverter(close_date));
            close_date.setDisable(true);
            close.setDisable(true);
            confirm_close.setOnAction(e->{
                if(confirm_close.isSelected()) {
                    close.setDisable(false);
                    close_date.setValue(LocalDate.now());
                }
                else
                    close.setDisable(true);
            });
            all_particulars.setOnAction(e->{
                if(!all_particulars.isSelected()){
                    confirm_close.setDisable(true);
                    close_date.setDisable(true);
                    close_preview.setDisable(true);
                }else{
                    close_preview.setDisable(false);
                }
            });
        }
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
        close.setOnAction(e->closestock());

        from.setConverter(new DateConverter(from));
        to.setConverter(new DateConverter(to));

        from.setValue(LocalDate.now());
        to.setValue(LocalDate.now());
    }

    private void setParticulars(){
        try{
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery("SELECT _id, particular FROM particulars");

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

            if (_sales.isSelected() && _purchase.isSelected() && !_stock.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC",
                        "SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0  ORDER BY DATE ASC",
                        "");
            }else if(_sales.isSelected() && !_purchase.isSelected() && !_stock.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC",
                        "",
                        "");
            }else if(!_sales.isSelected() && _purchase.isSelected() && !_stock.isSelected()){
                getTransactions("",
                        "SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC",
                        "");
            }else if(_sales.isSelected() && _purchase.isSelected() && _stock.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC",
                        "SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0  ORDER BY DATE ASC",
                        "SELECT * FROM balance WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' ORDER BY DATE ASC");
            }else if(_sales.isSelected() && !_purchase.isSelected() && _stock.isSelected()){
                getTransactions("SELECT * FROM sales WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY DATE ASC",
                        "",
                        "SELECT * FROM balance WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' ORDER BY DATE ASC");
            }else if(!_sales.isSelected() && _purchase.isSelected() && _stock.isSelected()){
                getTransactions("",
                        "SELECT * FROM purchases WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0  ORDER BY DATE ASC",
                        "SELECT * FROM balance WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' ORDER BY DATE ASC");
            }else if(!_sales.isSelected() && !_purchase.isSelected() && _stock.isSelected()){
                getTransactions("",
                        "",
                        "SELECT * FROM balance WHERE particular_id = "+ _pid +" AND date >= '" + start + "' AND date <= '" + end + "' ORDER BY DATE ASC");
            }
        }
    }

    private void getTransactions(String salesQuery, String purchaseQuery, String stocksQuery){

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
                                    resultSet.getFloat("qty"),
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
                                    0 - resultSet.getFloat("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(sale);
                        }
                    }
                    if(!stocksQuery.isEmpty()){
                        Statement query = Database.getConnection().createStatement();
                        ResultSet resultSet = query.executeQuery(stocksQuery);
                        while (resultSet.next()) {
                            Balance balance = new Balance(0,
                                    resultSet.getDate("date"),
                                    resultSet.getInt("bill"),
                                    resultSet.getInt("_id"),
                                    resultSet.getString("particular"),
                                    resultSet.getFloat("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(balance);
                        }
                    }

                } catch (SQLException e) {
                    showErrorDialog(e.getMessage());
                }

                Collections.sort(transactions);

                float balance = 0;

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
            PdfStock pdf = new PdfStock(close_preview.isSelected() ? closingStock : stocks, file);
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
        _id.setCellFactory(new Callback<TableColumn<Stock, Integer>, TableCell<Stock, Integer>>() {
            @Override
            public TableCell<Stock, Integer> call(TableColumn<Stock, Integer> param) {
                return new TableCell<Stock, Integer>(){
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if(!empty){
                            if(item.equals(0)){
                                setText("-");
                            }else {
                                setText(String.valueOf(item));
                            }
                        }else {
                            setText("");
                        }
                    }
                };
            }
        });

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
        closingStock.clear();
        transactions.clear();
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();

        if(start!=null && end!=null && !start.isAfter(end)){
            String queryString = "";
            if (_sales.isSelected() && _purchase.isSelected() && !_stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION SELECT _id,date, bill, particular_id,particular,-1 * qty AS qty,rate,discount,amount FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }else if(_sales.isSelected() && !_purchase.isSelected() && !_stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular, qty * -1 AS qty,rate,discount,amount FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }else if(!_sales.isSelected() && _purchase.isSelected() && !_stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 ORDER BY particular_id ASC, date ASC";
            }else if(_sales.isSelected() && _purchase.isSelected() && _stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION SELECT _id,date, bill, particular_id,particular,-1 * qty AS qty,rate,discount,amount FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION  SELECT _id * 0 AS _id ,date, bill, particular_id,particular,qty,rate,discount,amount FROM balance WHERE  date >= '" + start + "' AND date <= '" + end + "' ORDER BY particular_id ASC, date ASC";
            }else if(_sales.isSelected() && !_purchase.isSelected() && _stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION SELECT _id * 0 AS _id ,date, bill, particular_id,particular,qty,rate,discount,amount FROM balance WHERE  date >= '" + start + "' AND date <= '" + end + "' ORDER BY particular_id ASC, date ASC";
            }else if(!_sales.isSelected() && _purchase.isSelected() && _stock.isSelected()){
                queryString = "SELECT _id,date, bill, particular_id,particular,-1 * qty AS qty,rate,discount,amount FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0 UNION  SELECT _id * 0  AS _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM balance WHERE  date >= '" + start + "' AND date <= '" + end + "' ORDER BY particular_id ASC, date ASC";
            }else if(!_sales.isSelected() && !_purchase.isSelected() && _stock.isSelected()){
                queryString = "SELECT _id * 0 AS _id,date, bill, particular_id,particular,qty,rate,discount,amount FROM balance WHERE  date >= '" + start + "' AND date <= '" + end + "' ORDER BY _id ASC, date ASC";
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
                                    resultSet.getFloat("qty"),
                                    resultSet.getFloat("rate"),
                                    resultSet.getFloat("discount"),
                                    resultSet.getFloat("amount"));
                            transactions.add(t);
                        }
                    }catch (SQLException e){
                        showErrorDialog(e.getMessage());
                    }

                    float balance = 0;

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

                        if(i< transactions.size() - 1) {
                            if (transactions.get(i).getParticular_id() != transactions.get(i + 1).getParticular_id()) {
                                /* Closing Stock Balance Particular*/

                                    Statement rateQuery = Database.getConnection().createStatement();

                                    ResultSet resultSet = rateQuery.executeQuery("SELECT MAX(rate) FROM purchases WHERE particular_id = " + transactions.get(i).getParticular_id() + " UNION SELECT MAX(rate) FROM balance WHERE particular_id = " + transactions.get(i).getParticular_id() );
                                    float max_rate = 0;
                                    while (resultSet.next()){
                                        if(max_rate < resultSet.getFloat(1))
                                            max_rate = resultSet.getFloat(1);
                                    }

                                    Stock endStock = new Stock(
                                            transactions.get(i).get_id(),
                                            transactions.get(i).getDate(),
                                            transactions.get(i).getBill(),
                                            transactions.get(i).getParticular_id(),
                                            transactions.get(i).getParticular(),
                                            balance,
                                            max_rate,
                                            transactions.get(i).getDiscount(),
                                            balance * max_rate
                                    );
                                    endStock.setBalance(balance);
                                    closingStock.add(endStock);
                                /**/
                                balance = 0;
                            }
                        }else{
                            /* Closing Stock Balance of Nth Particular*/
                            Statement rateQuery = Database.getConnection().createStatement();
                            ResultSet resultSet = rateQuery.executeQuery("SELECT MAX(rate) FROM purchases WHERE particular_id = " + transactions.get(i).getParticular_id() + " UNION SELECT MAX(rate) FROM balance WHERE particular_id = " + transactions.get(i).getParticular_id() );
                            float max_rate = 0;
                            while (resultSet.next()){
                                if(max_rate < resultSet.getFloat(1))
                                    max_rate = resultSet.getFloat(1);
                            }

                            Stock endStock = new Stock(
                                    transactions.get(i).get_id(),
                                    transactions.get(i).getDate(),
                                    transactions.get(i).getBill(),
                                    transactions.get(i).getParticular_id(),
                                    transactions.get(i).getParticular(),
                                    balance,
                                    max_rate,
                                    transactions.get(i).getDiscount(),
                                    balance * max_rate
                            );
                            endStock.setBalance(balance);
                            closingStock.add(endStock);
                            /**/
                        }
                    }
                    return  close_preview.isSelected() ? closingStock : stocks;
                }
            };

            busy.visibleProperty().bind(task.runningProperty());
            table.itemsProperty().bind(task.valueProperty());

            new Thread(task).start();

            task.setOnSucceeded(e->{
                if(all_particulars.isSelected()) {
                    close_date.setDisable(false);
                    confirm_close.setDisable(false);
                }
            });
        }
    }

    private void clear(){
        particular.setValue(null);
        _sales.setSelected(true);
        _purchase.setSelected(true);
        _stock.setSelected(true);
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

    private void closestock(){

        if(close_date.getValue() != null){
        Task<String> close = new Task<String>() {
            @Override
            protected String call() throws Exception {
                try {
                    PreparedStatement statement = Database.getConnection().prepareStatement("INSERT INTO balance(date, bill,  particular_id, particular, qty, rate, discount, amount) VALUES(?,?,?,?,?,?,?,?)");

                    for (Stock s:closingStock ) {
                        statement.setDate(1, Date.valueOf(close_date.getValue()));
                        statement.setInt(2, s.getBill());
                        statement.setInt(3, s.getParticular_id());
                        statement.setString(4, s.getParticular());
                        statement.setFloat(5, s.getQty());
                        statement.setFloat(6, s.getRate());
                        statement.setFloat(7, s.getDiscount());
                        statement.setFloat(8, s.getAmount());
                        int num = statement.executeUpdate();
                    }
                    return "Stock Closed Successfully!";
                }catch (SQLException e){
                    return e.getMessage();
                }
            }
        };

        busy.visibleProperty().bind(close.runningProperty());
        close.setOnSucceeded(e->{
            try {
                String msg = close.get();
                if (msg.equals("Stock Closed Successfully!"))
                    showInfoDialog("Stock Closed", msg);
                else
                    showErrorDialog(msg);
            }catch (InterruptedException | ExecutionException er){
                showErrorDialog(er.getMessage());
            }
        });

        new Thread(close).start();
        }

    }
}
