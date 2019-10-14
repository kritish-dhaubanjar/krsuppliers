package krsuppliers.sales;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import krsuppliers.db.Database;
import krsuppliers.models.Particular;
import krsuppliers.models.Pdf;
import krsuppliers.models.Sale;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SaleController {
    @FXML
    JFXButton save, cancel, filter, print;
    @FXML
    TextField rate, qty, discount;
    @FXML
    DatePicker from, to, date;
    @FXML
    Text total;
    @FXML
    JFXSpinner busy, printing;
    @FXML
    ComboBox<Particular> particular;
    @FXML
    TableView<Sale>table;
    @FXML
    TableColumn<Sale, Integer> _qty, _amount, _discount, _rate, _particular_id, _sale_id;
    @FXML
    TableColumn<Sale, String> _particular;
    @FXML
    TableColumn<Sale, Date> _date;

    private int sales_id = 0;
    private ObservableList<Sale> sales = FXCollections.observableArrayList();
    private List<Particular> particulars = new ArrayList<>();
    private actions STATE = actions.SAVE;

    @FXML
    public void initialize(){
        particulars.clear();
        sales.clear();
        particular.getItems().clear();
        clear();
        printing.setVisible(false);
        busy.setVisible(false);
        /*
        particular.setOnAction((e)->{
            System.out.println(particular.getSelectionModel().getSelectedItem());
        });
        */

        setTableViewBinding();
        setParticulars();
        filter.setOnAction(e-> filterRecords());
        save.setOnAction(e->saveSales());
        cancel.setOnAction(e->clear());
        print.setOnAction(e->printPdf());

        particular.setOnKeyReleased(e->{
            for(Particular p: particulars){
                if(p.getParticular().toLowerCase().startsWith(e.getText())){
                    particular.getSelectionModel().select(p);
                    break;
                }
            }
        });

        from.setValue(LocalDate.now());
        to.setValue(LocalDate.now());

        getSales("SELECT * FROM sales WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");

    }

    private void filterRecords(){
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        if(start!=null && end!=null && !start.isAfter(end)){
            getSales("SELECT * FROM sales WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
        }
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

    private void getSales(String queryString) {
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    Statement query = Database.getConnection().createStatement();
                    ResultSet resultSet = query.executeQuery(queryString);
                    sales.clear();
                    int _total = 0;
                    while (resultSet.next()) {
                        sales.add(new Sale(resultSet.getInt("_id"),
                                resultSet.getDate("date"),
                                resultSet.getInt("particular_id"),
                                resultSet.getString("particular"),
                                resultSet.getInt("qty"),
                                resultSet.getInt("rate"),
                                resultSet.getInt("discount"),
                                resultSet.getInt("amount")));
                        _total += resultSet.getInt("amount");
                    }

                    total.setText(String.valueOf(_total));
                } catch (SQLException e) {
                    showErrorDialog(e.getMessage());
                }
                return null;
            }
        };

        busy.visibleProperty().bind(task.runningProperty());
        new Thread(task).start();
    }

    private void deleteSales(Sale sale){
        try {
            Statement query = Database.getConnection().createStatement();
            int num = query.executeUpdate("DELETE FROM sales WHERE _id = " + sale.get_id());
            if(num == 1){
                sales.remove(sale);
                int _total = Integer.parseInt(total.getText());
                _total -= sale.getAmount();
                total.setText(String.valueOf(_total));
            }else{
                showErrorDialog("Can't delete the selected record!");
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }

    }

    private void cancelSales(Sale sale){
        try {
            Statement query = Database.getConnection().createStatement();
            int num = query.executeUpdate("UPDATE sales SET cancel = 1 WHERE _id = " + sale.get_id());
            if(num == 1){
                sales.remove(sale);
                int _total = Integer.parseInt(total.getText());
                _total -= sale.getAmount();
                total.setText(String.valueOf(_total));
            }else{
                showErrorDialog("Can't restore the selected record!");
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void updateSales(Sale sale){
        STATE = actions.UPDATE;
        sales_id = sale.get_id();
        date.setValue(sale.getDate().toLocalDate());
        qty.setText(String.valueOf(sale.getQty()));
        rate.setText(String.valueOf(sale.getRate()));
        discount.setText(String.valueOf(sale.getDiscount()));
        particulars.forEach(t->{
            if (t.get_id() == sale.getParticular_id()) {
                particular.setValue(t);
            }
        });
    }

    private void saveSales(){
        LocalDate date_ = date.getValue();
        int qty_ = Integer.parseInt(qty.getText());
        int rate_ = Integer.parseInt(rate.getText());
        int discount_ = Integer.parseInt(discount.getText());
        Particular p = particular.getValue();
        int amt = qty_ * rate_ - discount_;

        try {
            if (STATE == actions.SAVE) {
                PreparedStatement query = Database.getConnection().prepareStatement("INSERT INTO sales (date, particular_id, particular, qty, rate, amount, discount) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                query.setDate(1, java.sql.Date.valueOf(date_));
                query.setInt(2, p.get_id());
                query.setString(3, p.getParticular());
                query.setInt(4, qty_);
                query.setInt(5, rate_);
                query.setInt(6, amt);
                query.setInt(7, discount_);
                query.execute();
                ResultSet resultSet = query.getGeneratedKeys();
                if(resultSet.next()) {
                    getSales("SELECT * FROM sales WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                }else{
                    showErrorDialog("Can't insert the sales record!");
                }
            }else if(STATE == actions.UPDATE && sales_id !=0){
                PreparedStatement query = Database.getConnection().prepareStatement("UPDATE sales SET date = ?, particular_id = ?, particular = ?, qty = ?, rate = ?, amount = ?, discount = ? WHERE _id = ?" );
                query.setDate(1, java.sql.Date.valueOf(date_));
                query.setInt(2, p.get_id());
                query.setString(3, p.getParticular());
                query.setInt(4, qty_);
                query.setInt(5, rate_);
                query.setInt(6, amt);
                query.setInt(7, discount_);
                query.setInt(8, sales_id);
                int num = query.executeUpdate();
                if(num == 1){
                    getSales("SELECT * FROM sales WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                    sales_id = 0;
                }else{
                    showErrorDialog("Can't update the selected record!");
                }
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void printPdf(){
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(LocalDateTime.now().toString() + ".pdf");
        File file = chooser.showSaveDialog(print.getScene().getWindow());
        if(file != null) {
            Pdf<Sale> pdf = new Pdf<>(sales, total.getText(), file);
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
        table.setItems(sales);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        MenuItem cancel = new MenuItem("Cancel");
        MenuItem update = new MenuItem("Update");

        update.setOnAction(e->updateSales(table.getSelectionModel().getSelectedItem()));
        delete.setOnAction(e->deleteSales(table.getSelectionModel().getSelectedItem()));
        cancel.setOnAction(e->cancelSales(table.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(update);
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().addAll(cancel);


        table.setRowFactory((e)-> {
                TableRow<Sale> row = new TableRow<>();
                row.emptyProperty().addListener((observable, wasEmpty, isEmpty)->{
                    row.setContextMenu(contextMenu);
                });
                return row;
        });

        _sale_id.setCellValueFactory(new PropertyValueFactory<>("_id"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("particular_id"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void clear(){
        particular.setValue(null);
        qty.setText("0");
        date.setValue(LocalDate.now());
        rate.setText("0");
        discount.setText("0");
        STATE = actions.SAVE;
    }

    private void showErrorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Oops! Something went wrong!");
        alert.setHeaderText(error);
        alert.showAndWait();
    }

    private boolean showAlertDialog(String info){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Are you sure?");
        alert.setHeaderText(info);
        Optional<ButtonType> button = alert.showAndWait();
        if (button.isPresent() && button.get() == ButtonType.OK ){
            return true;
        }else{
            return false;
        }
    }

    private void showInfoDialog(String title, String info){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(info);
        alert.showAndWait();
    }

    enum actions{
        SAVE,
        UPDATE
    }



}
