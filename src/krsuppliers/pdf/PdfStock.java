package krsuppliers.pdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import krsuppliers.models.Category;
import krsuppliers.models.Purchase;
import krsuppliers.models.Stock;
import krsuppliers.models.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.stream.Stream;

public class PdfStock extends Service<Boolean> {

    private ObservableList<Stock> list;
    private Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
    private File file;

    public PdfStock(ObservableList<Stock> list, File file){
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
                document.setPageSize(PageSize.A4);

                Paragraph _date = new Paragraph(new Chunk(LocalDateTime.now().toString(), font));
                _date.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(_date);

                PdfPTable table = new PdfPTable(10);
                table.setWidths(new float[]{(float) 1.5, 2,1, 1, 4, 1, 2, 2, 2, 2});

                addTableHeader(table);
                addTableRows(table);

                document.add(table);
                document.close();
                return true;
            }
        };
    }

    private void addTableHeader(PdfPTable table){
        Stream.of("#", "Date", "Bill","PID", "Particular", "Qty", "Rate", "Discount", "Amount", "Balance Qty").forEach(e -> {
            PdfPCell header = new PdfPCell();
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setPhrase(new Phrase(e, font));
            table.addCell(header);
        });
    }

    private void addTableRows(PdfPTable table){
        for (Stock t:list) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.get_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getDate().toString(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getBill()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getParticular_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getParticular(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQty()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getRate()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getDiscount()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getAmount()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getBalance()), font)));
        }
    }



}
