package com.dabel.app;

import com.dabel.dto.TransactionDto;
import com.dabel.exception.ResourceNotFoundException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class DocumentExporter {

    public static ByteArrayResource transactionReceipt(String resourcePath, TransactionDto transactionDto) {

        ClassPathResource resource = new ClassPathResource(resourcePath);

        try(FileInputStream fis = new FileInputStream(resource.getFile())) {

            XWPFDocument xwpfDocument = new XWPFDocument(fis);
            for(XWPFParagraph paragraph: xwpfDocument.getParagraphs()) {
                for(XWPFRun run: paragraph.getRuns()) {

                    String text = run.getText(0);

                    if(text != null && text.contains("pType")) {
                        text = text.replace("pType", transactionDto.getTransactionType());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("pID")) {
                        text = text.replace("pID", String.valueOf(transactionDto.getTransactionId()));
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("pDate")) {
                        text = text.replace("pDate", transactionDto.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("accountNumber")) {
                        text = text.replace("accountNumber", transactionDto.getInitiatorAccount().getAccountNumber());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("customerIdentity")) {
                        text = text.replace("customerIdentity", transactionDto.getCustomerIdentity());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("customerFullName")) {
                        text = text.replace("customerFullName", transactionDto.getCustomerFullName());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("sourceType")) {
                        text = text.replace("sourceType", transactionDto.getSourceType());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("sourceValue")) {
                        text = text.replace("sourceValue", transactionDto.getSourceValue());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("pAmount")) {
                        text = text.replace("pAmount", transactionDto.getAmount() + " " + transactionDto.getCurrency());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("currency")) {
                        text = text.replace("currency", transactionDto.getCurrency());
                        run.setText(text, 0);
                    }

                    if(text != null && text.contains("totalAmount")) {
                        text = text.replace("totalAmount", transactionDto.getAmount() + " " + transactionDto.getCurrency());
                        run.setText(text, 0);
                    }
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            xwpfDocument.write(outputStream);
            xwpfDocument.close();

            return new ByteArrayResource(outputStream.toByteArray());

        } catch (IOException ex) {
            throw new ResourceNotFoundException("Sorry, a system error is occurred. Please try again!");
        }
    }
}
