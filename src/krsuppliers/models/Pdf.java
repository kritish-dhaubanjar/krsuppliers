package krsuppliers.models;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class Pdf<T extends Transaction> extends Service<Boolean> {

    private ObservableList<T> list;
    private Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
    private File file;

    public Pdf(ObservableList<T> list, File file){
        this.list = list;
        this.file = file;
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Document document = new Document();
                FileOutputStream fstream = new FileOutputStream(file);
                PdfWriter.getInstance(document, fstream);
                document.open();
                document.setPageSize(new Rectangle(210, 297));
                document.add(new Chunk(LocalDateTime.now().toString(), font));
                PdfPTable table = new PdfPTable(8);
                table.setWidths(new int[]{2,2,1,4,1,2,2,2});
                addTableHeader(table);
                addTableRows(table);
                document.add(table);
                document.close();
                return true;
            }
        };
    }

    private void addTableHeader(PdfPTable table){
        Stream.of("#", "Date", "PID", "Particular", "Qty", "Rate", "Discount", "Amount").forEach(e->{
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setPhrase(new Phrase(e, font));
            table.addCell(header);
        });
    }

    private void addTableRows(PdfPTable table){
        for (Transaction t:list) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.get_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getDate().toString(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getParticular_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getParticular(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQty()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getRate()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getDiscount()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getAmount()), font)));
        }
    }



}
