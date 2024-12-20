package com.temis.app.utils;

import org.apache.poi.xwpf.usermodel.*;

import java.io.ByteArrayOutputStream;

public class WordDocumentFormatter {

    public byte[] createFormattedDocument(String draftText) throws Exception {

        XWPFDocument document = new XWPFDocument();

        
        String[] paragraphs = draftText.split("\\n\\n");

        for (String paragraphText : paragraphs) {
            XWPFParagraph paragraph = document.createParagraph();

            
            if (paragraphText.startsWith("Título:")) {
                paragraph.setStyle("Heading1");
                paragraph.setSpacingAfter(200);
            } else {
                paragraph.setStyle("Normal");
                paragraph.setSpacingAfter(100);
            }

            
            XWPFRun run = paragraph.createRun();
            run.setText(paragraphText);

            
            if (paragraphText.startsWith("Título:")) {
                run.setBold(true);
                run.setFontSize(14);
            } else {
                run.setFontSize(12);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.write(outputStream);
        document.close();
        return outputStream.toByteArray();
    }
}