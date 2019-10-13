package krsuppliers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URL;

public class Controller {
    private static FXMLLoader fxmlLoader = new FXMLLoader();
    @FXML
    Pane sales, purchases, stocks, particulars, account;
    @FXML
    Pane pane;

    private void loadFxml(URL url){
        pane.getChildren().removeAll(pane.getChildren());
        try {
            pane.getChildren().add(fxmlLoader.load(url));
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    @FXML
    public void initialize(){
        URL initUrl = getClass().getResource("./sales/sales.fxml");
        loadFxml(initUrl);

        sales.setOnMouseClicked((event)->{
            final URL url = getClass().getResource("./sales/sales.fxml");
            loadFxml(url);
            setBackground(sales);
        });

        purchases.setOnMouseClicked((event)->{
            final URL url = getClass().getResource("./purchases/purchases.fxml");
            loadFxml(url);
            setBackground(purchases);
        });

        stocks.setOnMouseClicked((event)->{
            final URL url = getClass().getResource("./stocks/stocks.fxml");
            loadFxml(url);
            setBackground(stocks);
        });

        particulars.setOnMouseClicked((event)->{
            final URL url = getClass().getResource("./particulars/particulars.fxml");
            loadFxml(url);
            setBackground(particulars);
        });

        account.setOnMouseClicked((event)->{
            final URL url = getClass().getResource("./accounts/accounts.fxml");
            loadFxml(url);
            setBackground(account);
        });
    }

    private void setBackground(Pane pane){
        sales.styleProperty().set("-fx-background-color: #3e3e3e;");
        purchases.styleProperty().set("-fx-background-color: #3e3e3e;");
        stocks.styleProperty().set("-fx-background-color: #3e3e3e;");
        particulars.styleProperty().set("-fx-background-color: #3e3e3e;");
        account.styleProperty().set("-fx-background-color: #3e3e3e;");
        pane.styleProperty().set("-fx-background-color: #2e2e2e;");
    }
}
