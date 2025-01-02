package com.temis.app.utils;

import org.apache.poi.xwpf.usermodel.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

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

            // Estilo del texto
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

    public byte[] createPDFDocument(String draftText) throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText();
                contentStream.setLeading(14.5f);
                contentStream.newLineAtOffset(50, 750);

                String[] paragraphs = draftText.split("\\n\\n");

                for (String paragraphText : paragraphs) {
                    if (paragraphText.startsWith("Título:")) {
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                    } else {
                        contentStream.setFont(PDType1Font.HELVETICA, 12);
                    }

                    contentStream.showText(paragraphText);
                    contentStream.newLine();
                    contentStream.newLine();
                }

                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}