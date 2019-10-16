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

public class Pdf<T extends Transaction> extends Service<Boolean> {

    private ObservableList<T> list;
    private String total;
    private Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);
    private File file;
    private Category category;

    public Pdf(ObservableList<T> list, String total, File file, Category category){
        this.list = list;
        this.file = file;
        this.total = total;
        this.category = category;
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

                PdfPTable table;

                if(category == Category.SALES) {
                    table = new PdfPTable(8);
                    table.setWidths(new float[]{(float) 1.5, 2, 1, 4, 1, 2, 2, 2});
                }else{
                    table = new PdfPTable(9);
                    table.setWidths(new float[]{(float) 1.5, 2, 1, 4, 1, 2, 2, 2, 2});
                }
                addTableHeader(table);
                addTableRows(table);

                document.add(table);
                Paragraph _total = new Paragraph(new Chunk("Total : Rs " + total, font ));
                _total.setAlignment(Paragraph.ALIGN_RIGHT);
                document.add(_total);

                document.close();
                return true;
            }
        };
    }

    private void addTableHeader(PdfPTable table){
        if(category == Category.SALES) {
            Stream.of("#", "Date", "PID", "Particular", "Qty", "Rate", "Discount", "Amount").forEach(e -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setPhrase(new Phrase(e, font));
                table.addCell(header);
            });
        }else {
            Stream.of("#", "Date", "PID", "Particular", "Qty", "Rate", "Selling @", "Discount", "Amount").forEach(e -> {
                PdfPCell header = new PdfPCell();
                header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                header.setPhrase(new Phrase(e, font));
                table.addCell(header);
            });
        }
    }

    private void addTableRows(PdfPTable table){
        for (Transaction t:list) {
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.get_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getDate().toString(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getParticular_id()), font)));
            table.addCell(new PdfPCell(new Phrase(t.getParticular(), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getQty()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getRate()), font)));

            if(category == Category.PURCHASE)
                table.addCell(new PdfPCell(new Phrase(String.valueOf(((Purchase)t).getSelling_rate()), font)));

            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getDiscount()), font)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(t.getAmount()), font)));
        }
    }



}
