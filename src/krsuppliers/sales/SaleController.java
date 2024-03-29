package krsuppliers.sales;

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
import krsuppliers.models.DateConverter;
import krsuppliers.models.Particular;
import krsuppliers.pdf.Pdf;
import krsuppliers.models.Sale;

import java.io.File;
import java.sql.Date;
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
    TextField rate, qty, discount, bill;
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
    TableColumn<Sale, Integer> _qty, _amount, _discount, _rate, _particular_id, _sale_id, _bill;
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

        particular.setOnAction((e)->getParticularSellingRate());


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

        from.setConverter(new DateConverter(from));
        to.setConverter(new DateConverter(to));
        date.setConverter(new DateConverter(date));

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
            ResultSet resultSet = query.executeQuery("SELECT _id, particular FROM particulars");

            while (resultSet.next()){
                particulars.add(new Particular(
                        resultSet.getInt("_id"),
                        resultSet.getString("particular"))
                );
            }
            Collections.sort(particulars);
            particular.getItems().addAll(particulars);
        }catch (SQLException e){
            showErrorDialog(e.getMessage());
        }
    }

    private void getParticularSellingRate(){
        int _id = particular.getSelectionModel().getSelectedItem().get_id();
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement("SELECT MAX(rate) AS rate FROM balance WHERE particular_id = ?");
            statement.setInt(1, _id);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()){
                float _rate = resultSet.getFloat("rate");
                double selling_rate = _rate + 0.2 * _rate;
                rate.setText(String.format("%.2f", selling_rate));
                break;
            }
        }catch (SQLException err){
            showErrorDialog(err.getMessage());
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
                    float _total = 0;
                    while (resultSet.next()) {
                        sales.add(new Sale(resultSet.getInt("_id"),
                                resultSet.getDate("date"),
                                resultSet.getInt("bill"),
                                resultSet.getInt("particular_id"),
                                resultSet.getString("particular"),
                                resultSet.getFloat("qty"),
                                resultSet.getFloat("rate"),
                                resultSet.getFloat("discount"),
                                resultSet.getFloat("amount")));
                        _total += resultSet.getFloat("amount");
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

    private void cancelSales(Sale sale){
        try {
            Statement query = Database.getConnection().createStatement();
            int num = query.executeUpdate("UPDATE sales SET cancel = 1 WHERE _id = " + sale.get_id());
            if(num == 1){
                sales.remove(sale);
                float _total = Float.parseFloat(total.getText());
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
        bill.setText(String.valueOf(sale.getBill()));
        particulars.forEach(t->{
            if (t.get_id() == sale.getParticular_id()) {
                particular.setValue(t);
            }
        });
    }

    private void saveSales(){
        Date date_ = Date.valueOf(date.getValue());
        float qty_ = Float.parseFloat(qty.getText());
        int bill_ = Integer.parseInt(bill.getText());
        float rate_ = Float.parseFloat(rate.getText());
        float discount_ = Float.parseFloat(discount.getText());
        Particular p = particular.getValue();
        float amt = qty_ * rate_ - discount_;

        try {
            if (STATE == actions.SAVE) {
                PreparedStatement query = Database.getConnection().prepareStatement("INSERT INTO sales (date, bill, particular_id, particular, qty, rate, amount, discount) VALUES(?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                query.setDate(1, date_);
                query.setInt(2, bill_);
                query.setInt(3, p.get_id());
                query.setString(4, p.getParticular());
                query.setFloat(5, qty_);
                query.setFloat(6, rate_);
                query.setFloat(7, amt);
                query.setFloat(8, discount_);
                query.execute();
                ResultSet resultSet = query.getGeneratedKeys();
                if(resultSet.next()) {
                    getSales("SELECT * FROM sales WHERE cancel = 0 ORDER BY _id DESC LIMIT 20");
                    clear();
                }else{
                    showErrorDialog("Can't insert the sales record!");
                }
            }else if(STATE == actions.UPDATE && sales_id !=0){
                PreparedStatement query = Database.getConnection().prepareStatement("UPDATE sales SET date = ?, bill = ? ,particular_id = ?, particular = ?, qty = ?, rate = ?, amount = ?, discount = ? WHERE _id = ?" );
                query.setDate(1, date_);
                query.setInt(2, bill_);
                query.setInt(3, p.get_id());
                query.setString(4, p.getParticular());
                query.setFloat(5, qty_);
                query.setFloat(6, rate_);
                query.setFloat(7, amt);
                query.setFloat(8, discount_);
                query.setInt(9, sales_id);
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
            Pdf<Sale> pdf = new Pdf<>(sales, total.getText(), file, Category.SALES);
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
        _bill.setCellValueFactory(new PropertyValueFactory<>("bill"));
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

    enum actions{
        SAVE,
        UPDATE
    }



}
