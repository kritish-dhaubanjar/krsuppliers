package krsuppliers.purchases;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import krsuppliers.db.Database;
import krsuppliers.models.Particular;
import krsuppliers.models.Pdf;
import krsuppliers.models.Purchase;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class PurchaseController {
    @FXML
    Button save, cancel, filter, print;
    @FXML
    TextField rate, qty, discount;
    @FXML
    DatePicker from, to, date;
    @FXML
    Text total;
    @FXML
    ProgressBar printing;
    @FXML
    ChoiceBox<Particular> particular;
    @FXML
    TableView<Purchase>table;
    @FXML
    TableColumn<Purchase, Integer> _qty, _amount, _discount, _rate, _particular_id, _purchase_id;
    @FXML
    TableColumn<Purchase, String> _particular;
    @FXML
    TableColumn<Purchase, Date> _date;

    private int purchase_id = 0;

    enum actions{
        SAVE,
        UPDATE
    }


    private ObservableList<Purchase> purchases = FXCollections.observableArrayList();
    private List<Particular> particulars = new ArrayList<>();
    private actions STATE = actions.SAVE;

    @FXML
    public void initialize(){
        particulars.clear();
        purchases.clear();
        particular.getItems().clear();
        clear();
        printing.setVisible(false);

        /*
        particular.setOnAction((e)->{
            System.out.println(particular.getSelectionModel().getSelectedItem());
        });
        */

        setTableViewBinding();
        setParticulars();
        filter.setOnAction(e-> filterRecords());
        save.setOnAction(e->savePurchases());
        cancel.setOnAction(e->clear());
        print.setOnAction(e->printPdf());

        from.setValue(LocalDate.now());
        to.setValue(LocalDate.now());
        getPurchases("SELECT * FROM purchases WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
    }


    private void filterRecords(){
        LocalDate start = from.getValue();
        LocalDate end = to.getValue();
        if(start!=null && end!=null && !start.isAfter(end)){
            getPurchases("SELECT * FROM purchases WHERE date >= '" + start + "' AND date <= '" + end + "' AND cancel = 0");
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

    private void getPurchases(String queryString){
        try {
            Statement query = Database.getConnection().createStatement();
            ResultSet resultSet = query.executeQuery(queryString);
            purchases.clear();
            int _total = 0;
            while (resultSet.next()){
                purchases.add(new Purchase(resultSet.getInt("_id"),
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
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void deletePurchases(Purchase sale){
        if(showAlertDialog("#" + sale.get_id() + " will be deleted!"))
            try {
                Statement query = Database.getConnection().createStatement();
                int num = query.executeUpdate("DELETE FROM purchases WHERE _id = " + sale.get_id());
                if(num == 1){
                    purchases.remove(sale);
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

    private void cancelPurchases(Purchase purchase){
        try {
            Statement query = Database.getConnection().createStatement();
            int num = query.executeUpdate("UPDATE purchases SET cancel = 1 WHERE _id = " + purchase.get_id());
            if(num == 1){
                purchases.remove(purchase);
                int _total = Integer.parseInt(total.getText());
                _total -= purchase.getAmount();
                total.setText(String.valueOf(_total));
            }else{
                showErrorDialog("Can't restore the selected record!");
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void updatePurchases(Purchase purchase){
        STATE = actions.UPDATE;
        purchase_id  = purchase.get_id();
        date.setValue(purchase.getDate().toLocalDate());
        qty.setText(String.valueOf(purchase.getQty()));
        rate.setText(String.valueOf(purchase.getRate()));
        discount.setText(String.valueOf(purchase.getDiscount()));
        particulars.forEach(t->{
            if (t.get_id() == purchase.getParticular_id()) {
                particular.setValue(t);
            }
        });
    }

    private void savePurchases(){
        LocalDate date_ = date.getValue();
        int qty_ = Integer.parseInt(qty.getText());
        int rate_ = Integer.parseInt(rate.getText());
        int discount_ = Integer.parseInt(discount.getText());
        Particular p = particular.getValue();
        int amt = qty_ * rate_ - discount_;

        try {
            if (STATE == actions.SAVE) {
                PreparedStatement query = Database.getConnection().prepareStatement("INSERT INTO purchases (date, particular_id, particular, qty, rate, amount, discount) VALUES(?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
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
                    getPurchases("SELECT * FROM purchases WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                }else{
                    showErrorDialog("Can't insert the purchase record!");
                }
            }else if(STATE == actions.UPDATE && purchase_id !=0){
                PreparedStatement query = Database.getConnection().prepareStatement("UPDATE purchases SET date = ?, particular_id = ?, particular = ?, qty = ?, rate = ?, amount = ?, discount = ? WHERE _id = ?" );
                query.setDate(1, java.sql.Date.valueOf(date_));
                query.setInt(2, p.get_id());
                query.setString(3, p.getParticular());
                query.setInt(4, qty_);
                query.setInt(5, rate_);
                query.setInt(6, amt);
                query.setInt(7, discount_);
                query.setInt(8, purchase_id);
                int num = query.executeUpdate();
                if(num == 1){
                    getPurchases("SELECT * FROM purchases WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                    purchase_id = 0;
                }else{
                    showErrorDialog("Can't update the selected record!");
                }
            }
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }


    private void setTableViewBinding(){
        table.setItems(purchases);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        MenuItem cancel = new MenuItem("Cancel");
        MenuItem update = new MenuItem("Update");

        update.setOnAction(e->updatePurchases(table.getSelectionModel().getSelectedItem()));
        delete.setOnAction(e->deletePurchases(table.getSelectionModel().getSelectedItem()));
        cancel.setOnAction(e->cancelPurchases(table.getSelectionModel().getSelectedItem()));

        contextMenu.getItems().addAll(update);
        contextMenu.getItems().addAll(delete);
        contextMenu.getItems().addAll(cancel);


        table.setRowFactory((e)-> {
            TableRow<Purchase> row = new TableRow<>();
            row.emptyProperty().addListener((observable, wasEmpty, isEmpty)->{row.setContextMenu(contextMenu);});
            return row;
        });

        _purchase_id.setCellValueFactory(new PropertyValueFactory<>("_id"));
        _qty.setCellValueFactory(new PropertyValueFactory<>("qty"));
        _discount.setCellValueFactory(new PropertyValueFactory<>("discount"));
        _particular.setCellValueFactory(new PropertyValueFactory<>("particular"));
        _particular_id.setCellValueFactory(new PropertyValueFactory<>("particular_id"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void printPdf(){
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(LocalDateTime.now().toString() + ".pdf");
        File file = chooser.showSaveDialog(print.getScene().getWindow());
        if(file != null) {
            Pdf<Purchase> pdf = new Pdf<>(purchases, file);
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


}
