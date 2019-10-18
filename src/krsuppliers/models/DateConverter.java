package krsuppliers.models;

import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateConverter  extends StringConverter<LocalDate> {

    private DatePicker date;
    private String pattern = "yyyy-MM-dd";
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(pattern);

    public DateConverter(DatePicker date){
        this.date = date;
        date.setPromptText(pattern.toLowerCase());

    }

    @Override public String toString(LocalDate date) {
        if (date != null) {
            return dateFormatter.format(date);
        } else {
            return "";
        }
    }

    @Override public LocalDate fromString(String string) {
        if (string != null && !string.isEmpty()) {
            return LocalDate.parse(string, dateFormatter);
        } else {
            return null;
        }
    }
}
