package krsuppliers.purchases;

import com.jfoenix.controls.JFXButton;
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
import krsuppliers.models.Category;
import krsuppliers.models.Particular;
import krsuppliers.pdf.Pdf;
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
    JFXButton save, cancel, filter, print;
    @FXML
    TextField rate, qty, discount, selling_rate, bill;
    @FXML
    DatePicker from, to, date;
    @FXML
    Text total;
    @FXML
    JFXSpinner printing, busy;
    @FXML
    ComboBox<Particular> particular;
    @FXML
    TableView<Purchase>table;
    @FXML
    TableColumn<Purchase, Integer> _qty, _particular_id, _purchase_id, _bill;
    @FXML
    TableColumn<Purchase, Float> _amount, _discount, _rate, _selling_rate;
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
        busy.setVisible(false);

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
        Task task = new Task<Void>() {
            @Override
                    protected Void call() throws Exception {
                try {
                    Statement query = Database.getConnection().createStatement();
                    ResultSet resultSet = query.executeQuery(queryString);
                    purchases.clear();
                    float _total = 0;
                    while (resultSet.next()){
                        purchases.add(new Purchase(resultSet.getInt("_id"),
                                resultSet.getDate("date"),
                                resultSet.getInt("bill"),
                                resultSet.getInt("particular_id"),
                                resultSet.getString("particular"),
                                resultSet.getInt("qty"),
                                resultSet.getFloat("rate"),
                                resultSet.getFloat("selling_rate"),
                                resultSet.getFloat("discount"),
                                resultSet.getFloat("amount")));
                        _total += resultSet.getFloat("amount");
                    }

                    total.setText(String.valueOf(_total));
                }catch (SQLException e){
                    showErrorDialog(e.getMessage());
                }
                return null;
            }
        };

        busy.visibleProperty().bind(task.runningProperty());
        new Thread(task).start();
    }

    private void deletePurchases(Purchase sale){
        if(showAlertDialog("#" + sale.get_id() + " will be deleted!"))
            try {
                Statement query = Database.getConnection().createStatement();
                int num = query.executeUpdate("DELETE FROM purchases WHERE _id = " + sale.get_id());
                if(num == 1){
                    purchases.remove(sale);
                    float _total = Float.parseFloat(total.getText());
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
                float _total = Float.parseFloat(total.getText());
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
        selling_rate.setText(String.valueOf(purchase.getSelling_rate()));
        particulars.forEach(t->{
            if (t.get_id() == purchase.getParticular_id()) {
                particular.setValue(t);
            }
        });
    }

    private void savePurchases(){
        LocalDate date_ = date.getValue();
        int qty_ = Integer.parseInt(qty.getText());
        int bill_ = Integer.parseInt(bill.getText());
        float rate_ = Float.parseFloat(rate.getText());
        float discount_ = Float.parseFloat(discount.getText());
        float selling_rate_ = Float.parseFloat(selling_rate.getText());
        Particular p = particular.getValue();
        float amt = qty_ * rate_ - discount_;

        try {
            if (STATE == actions.SAVE) {
                PreparedStatement query = Database.getConnection().prepareStatement("INSERT INTO purchases (date, bill, particular_id, particular, qty, rate, selling_rate, amount, discount) VALUES(?,?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                query.setDate(1, java.sql.Date.valueOf(date_));
                query.setInt(2, bill_);
                query.setInt(3, p.get_id());
                query.setString(4, p.getParticular());
                query.setInt(5, qty_);
                query.setFloat(6, rate_);
                query.setFloat(7, selling_rate_);
                query.setFloat(8, amt);
                query.setFloat(9, discount_);
                query.execute();
                ResultSet resultSet = query.getGeneratedKeys();
                if(resultSet.next()) {
                    getPurchases("SELECT * FROM purchases WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                }else{
                    showErrorDialog("Can't insert the purchase record!");
                }
            }else if(STATE == actions.UPDATE && purchase_id !=0){
                PreparedStatement query = Database.getConnection().prepareStatement("UPDATE purchases SET date = ?, bill = ?, particular_id = ?, particular = ?, qty = ?, rate = ?, selling_rate = ?, amount = ?, discount = ? WHERE _id = ?" );
                query.setDate(1, java.sql.Date.valueOf(date_));
                query.setInt(2, bill_);
                query.setInt(3, p.get_id());
                query.setString(4, p.getParticular());
                query.setInt(5, qty_);
                query.setFloat(6, rate_);
                query.setFloat(7, selling_rate_);
                query.setFloat(8, amt);
                query.setFloat(9, discount_);
                query.setInt(10, purchase_id);
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
        _bill.setCellValueFactory(new PropertyValueFactory<>("bill"));
        _rate.setCellValueFactory(new PropertyValueFactory<>("rate"));
        _selling_rate.setCellValueFactory(new PropertyValueFactory<>("selling_rate"));
        _date.setCellValueFactory(new PropertyValueFactory<>("date"));
        _amount.setCellValueFactory(new PropertyValueFactory<>("amount"));
    }

    private void printPdf(){
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(LocalDateTime.now().toString() + ".pdf");
        File file = chooser.showSaveDialog(print.getScene().getWindow());
        if(file != null) {
            Pdf<Purchase> pdf = new Pdf<>(purchases, total.getText(), file, Category.PURCHASE);
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
        selling_rate.setText("0");
        discount.setText("0");
        bill.setText("0");
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
