#!/bin/bash

rm -rf out

javac \
  --module-path ./javafx-sdk-23.0.1/lib \
  --add-modules javafx.controls,javafx.fxml \
  -cp src/krsuppliers/resources/jfoenix-9.0.10.jar:src/krsuppliers/resources/mysql-connector-java-8.0.11.jar:src/krsuppliers/resources/itextpdf-5.5.13.1.jar \
  -d out $(find src/krsuppliers -name *.java)

cp src/krsuppliers/main.fxml out/krsuppliers/
cp src/krsuppliers/stocks/stocks.fxml out/krsuppliers/stocks/
cp src/krsuppliers/sales/sales.fxml out/krsuppliers/sales/
cp src/krsuppliers/purchases/purchases.fxml out/krsuppliers/purchases/
cp src/krsuppliers/particulars/particulars.fxml out/krsuppliers/particulars/
cp src/krsuppliers/accounts/accounts.fxml out/krsuppliers/accounts/
cp -r src/krsuppliers/resources out/krsuppliers/
