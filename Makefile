run:
	cd out && java \
		--add-opens javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED \
		--module-path ../javafx-sdk-23.0.1/lib \
		--add-modules javafx.controls,javafx.fxml,javafx.graphics \
		-cp "krsuppliers/resources/jfoenix-9.0.10.jar:krsuppliers/resources/mysql-connector-java-8.0.11.jar:krsuppliers/resources/itextpdf-5.5.13.1.jar:." \
		krsuppliers.Main
