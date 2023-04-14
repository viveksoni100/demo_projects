package com.itext.jsontopdf;

import com.google.gson.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.web.bind.annotation.*;

import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;

@RestController
public class PDFController {

    @ResponseBody
    @PostMapping("/convertToPdf")
    public String convertJsonToPdf(@RequestBody String jsonData) {
        String msg = "";
        try {
            //2. converting json string to jsonObject
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            JsonArray array = parser.parse(jsonData).getAsJsonArray();
            JsonObject object = new JsonObject();
            object.add("item", array);
            JsonObject jsonObject = gson.fromJson(object, JsonObject.class);

            //3. create iText pdf document
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream("/home/viveksoni/Documents" + "/jsontopdf.pdf"));
            document.open();

            /**
             * we are taking the numOfColumn so we can use this ahead while declaring pdf table
             */
            int numColumns = getNumOfColumns(jsonObject);

            //4.
            PdfPTable table = new PdfPTable(numColumns);

            Paragraph heading = new Paragraph("Student Data");
            heading.setAlignment(Element.ALIGN_CENTER);
            document.add(heading);
            document.add(new Paragraph("\n"));

            //5. populate data in to the table
            // filling the header
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    Iterator<Map.Entry<String, JsonElement>> iterator = ((JsonObject) element).entrySet().iterator();
                    while (iterator.hasNext()) {
                        Font boldFont = new Font(Font.FontFamily.COURIER, 10, Font.BOLD);
                        table.addCell(new PdfPCell(new Phrase(iterator.next().getKey(), boldFont)));
                    }
                    break;
                }
            }

            // filling the data
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                for (JsonElement element : entry.getValue().getAsJsonArray()) {
                    Iterator<Map.Entry<String, JsonElement>> iterator = ((JsonObject) element).entrySet().iterator();
                    while (iterator.hasNext()) {
                        Font normalFont = new Font(Font.FontFamily.COURIER, 9, Font.NORMAL);
                        JsonElement jsonElement = iterator.next().getValue();
                        String value = "";
                        if (jsonElement != null && !jsonElement.isJsonNull()) {
                            value = jsonElement.getAsString();
                        }
                        Phrase phrase = new Phrase(value + " ", normalFont);
                        PdfPCell cell = new PdfPCell(phrase);
                        table.addCell(cell);
                    }
                }
            }

            //6. add the table to the document
            document.add(table);
            //7. close the document
            document.close();

            msg = "PDF conversion is successful";
        } catch (Exception e) {
            e.printStackTrace();
            msg = "PDF conversion failed due to : " + e.getMessage();
        }
        return msg;
    }

    private int getNumOfColumns(JsonObject jsonObject) {
        int numColumns = 0;
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            for (JsonElement element : entry.getValue().getAsJsonArray()) {
                numColumns = ((JsonObject) element).entrySet().size();
                break;
            }
        }
        return numColumns;
    }

}
