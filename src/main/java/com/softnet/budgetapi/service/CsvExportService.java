package com.softnet.budgetapi.service;

import com.softnet.budgetapi.dto.response.TransactionResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CsvExportService {

    public String exportToCsv(List<TransactionResponse> transactions) {
        StringBuilder sb = new StringBuilder();

        sb.append("ID,Amount,Type,Category,Description,Date\n");

        for(TransactionResponse t : transactions){
            sb.append(t.id()).append(",")
                    .append(t.amount()).append(",")
                    .append(t.type()).append(",")
                    .append(escapeCsvField(t.category())).append(",")
                    .append(escapeCsvField(t.description())).append(",")
                    .append(t.date()).append("\n");
        }

        return sb.toString();
    }

    private String escapeCsvField(String field){
        if(field == null){
            return "";
        }
        if(field.contains(",") || field.contains("\"") || field.contains("\n")){
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
