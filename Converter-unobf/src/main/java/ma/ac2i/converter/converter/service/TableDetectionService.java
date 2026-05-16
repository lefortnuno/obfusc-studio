package ma.ac2i.converter.converter.service;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ac2i.converter.converter.entities.*;
import ma.ac2i.converter.converter.repository.ComplexStructureRepository;
import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Service
@NoArgsConstructor
@Data
public class TableDetectionService {

    private String appDirectory;
    private ComplexStructureRepository complexStructureRepository;

    public TableDetectionService(ComplexStructureRepository complexStructureRepository) {
        this.complexStructureRepository = complexStructureRepository;
    }

    public void test(String infilepath, ComplexStructure complexStructure) {
        IOUtils.setByteArrayMaxOverride(500000000);
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(infilepath))) {
            String filePath = appDirectory + File.separator + "download" + File.separator + complexStructure.getName() + ".xml";
            try {
                String content = convert(workbook, complexStructure);
                try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
                    bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    bw.write(content);
                }

                System.out.println("String has been written to the file successfully.");
            } catch (Exception e) {
                e.printStackTrace();
            }


            /*for (CellRangeAddress table : tableBounds) {
                System.out.println("Table detected from row " + (table.getFirstRow() + 1) +
                        " to row " + (table.getLastRow() + 1) +
                        ", column " + (table.getFirstColumn() + 1) +
                        " to column " + (table.getLastColumn() + 1));
                // You can process or store each table here
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String convert(Workbook workbook, ComplexStructure complexStructure) throws Exception {
        Sheet sheet = workbook.getSheetAt(0);
        List<CellRangeAddress> tables = detectTables(sheet);
        System.out.println(tables.size());
        List<SubStructure> subStructures = complexStructure.getSubStructures();
        if (tables.size() != subStructures.size())
            throw new RuntimeException("number of tables don't match the number of sub-structure");
        int index = 0;
        Map<String, String> SubStructuresXml = new HashMap<>();
        for (CellRangeAddress table : tables) {
            String SubStructureXml = "";
            int firstRow = table.getFirstRow();
            int lastRow = table.getLastRow();
            int firstCol = table.getFirstColumn();
            int lastCol = table.getLastColumn();
            List<SubStructureDetail> subStructureDetails = subStructures.get(index).getSubStructureDetails();
            String pattern = subStructures.get(index).getExpression();
            if (subStructures.get(index).getType().equals("vertical")) {
                Map<String, Integer> fieldNames = getHeadersv(sheet.getRow(firstRow), subStructures.get(index));
                for (int row = firstRow + 1; row <= lastRow; row++) {
                    String output = pattern;
                    Row dataRow = sheet.getRow(row);
                    for (SubStructureDetail ssd : subStructureDetails) {
                        Integer index_cell = fieldNames.get(ssd.getName());
                        if (index_cell == null) {
                            output = output.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", "");
                            continue;
                        }
                        Cell cell = dataRow.getCell(index_cell);
                        if (cell.getCellType() == CellType.NUMERIC) {
                            double numericValue = cell.getNumericCellValue();
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                            symbols.setDecimalSeparator('.');
                            DecimalFormat df = new DecimalFormat("0.##", symbols);
                            String formattedNumber = df.format(numericValue);
                            output = output.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", Matcher.quoteReplacement(formattedNumber));
                        } else if (cell.getCellType() == CellType.STRING) {
                            // Handle string value
                            output = output.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", Matcher.quoteReplacement(cell.getStringCellValue()));
                        } else if (cell.getCellType() == CellType.BLANK) {
                            output = output.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", "");
                        }
                    }
                    if (!output.equals(pattern)) {
                        SubStructureXml = SubStructureXml + output;
                    }
                }
            } else {
                Map<Integer, String> fieldNames = getHeadersh(sheet, firstRow, lastRow, firstCol, subStructures.get(index));
                for (int col = firstCol + 1; col <= lastCol; col++) {
                    String output = pattern;
                    for (int row = firstRow; row <= lastRow; row++) {
                        Row headerRow = sheet.getRow(row);
                        Cell cell = headerRow.getCell(col);
                        String value_mapp = fieldNames.get(row);
                        if (value_mapp == null) {
                            continue;
                        }
                        if (cell.getCellType() == CellType.NUMERIC) {
                            double numericValue = cell.getNumericCellValue();
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                            symbols.setDecimalSeparator('.');
                            DecimalFormat df = new DecimalFormat("0.##", symbols);
                            String formattedNumber = df.format(numericValue);
                            output = output.replaceAll("@" + Pattern.quote(value_mapp) + "(?![\\w])", Matcher.quoteReplacement(formattedNumber));
                        } else if (cell.getCellType() == CellType.STRING) {
                            output = output.replaceAll("@" + Pattern.quote(value_mapp) + "(?![\\w])", Matcher.quoteReplacement(cell.getStringCellValue()));
                        } else if (cell.getCellType() == CellType.BLANK) {
                            output = output.replaceAll("@" + Pattern.quote(value_mapp) + "(?![\\w])", "");
                        }
                    }
                    if (!output.equals(pattern)) {
                        SubStructureXml = SubStructureXml + output;
                    }
                }
            }
            SubStructuresXml.put(subStructures.get(index).getName(), SubStructureXml);
            index++;
        }
        String comPattern = complexStructure.getExpression();
        String output = comPattern;
        for (String key : SubStructuresXml.keySet()) {
            String value = SubStructuresXml.get(key);
            output = output.replaceAll("@" + Pattern.quote(key) + "(?![\\w])", Matcher.quoteReplacement(value));
        }
        return output;
    }


    public String convertTablesToCsv(String infilepath, Structure structure, String output_type, String delimiter, boolean header,boolean header_out) {
        IOUtils.setByteArrayMaxOverride(500000000);
        try (Workbook workbook = WorkbookFactory.create(new FileInputStream(infilepath))) {
            String filePath = appDirectory + File.separator + "upload" + File.separator + structure.getStrName() + ".csv";
            if (output_type.equals("csv")) {
                filePath = appDirectory + File.separator + "download" + File.separator + structure.getStrName() + ".csv";
            }
            Sheet sheet = workbook.getSheetAt(0);
            List<CellRangeAddress> tables = detectTables(sheet);
            for (CellRangeAddress tableRange : tables) {
                System.out.println(tableRange.toString());
                try {
                    String content = "";
                    if (output_type.equals("csv")) {
                        content = convertTableToCsv(sheet, tableRange, delimiter, header);
                        String header_txt="";
                        int headerindex= 1;
                        for(StructureDetail structureDetail : structure.getStructureDetails()){
                            header_txt = header_txt + structureDetail.getName();
                            if (headerindex != structure.getStructureDetails().size()){
                                header_txt = header_txt + delimiter;
                            }
                        }
                        if (header_out ) {
                            content = header_txt + "\n"+content;
                        }
                    } else {
                        content = convertTableToCsv(sheet, tableRange, header);
                    }
                    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(filePath), StandardCharsets.UTF_8)) {
                        bw.write(content);
                    }
                    if (output_type.equals("csv")) {
                        return structure.getStrName() + ".csv";
                    }
                    return filePath;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertTableToCsv(Sheet sheet, CellRangeAddress tableRange, String delimiter, boolean header) {
        StringBuilder csvData = new StringBuilder();
        int first_row = tableRange.getFirstRow();
        if (header ) {
            first_row++;
        }
        for (int i = first_row; i <= tableRange.getLastRow(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = tableRange.getFirstColumn(); j <= tableRange.getLastColumn(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        csvData.append(cellToString(cell)).append(delimiter);
                    }
                }
                csvData.append("\n");
            }
        }
        // Now you can write `csvData` to a CSV file or do further processing as needed
        return csvData.toString();
    }

    private String convertTableToCsv(Sheet sheet, CellRangeAddress tableRange, boolean header) {
        StringBuilder csvData = new StringBuilder();
        int first_row = tableRange.getFirstRow();
        if (header ) {
            first_row++;
        }
        for (int i = first_row; i <= tableRange.getLastRow(); i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                for (int j = tableRange.getFirstColumn(); j <= tableRange.getLastColumn(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        csvData.append(cellToString(cell)).append(",");
                    }
                }
                csvData.append("\n");
            }
        }
        // Now you can write `csvData` to a CSV file or do further processing as needed
        return csvData.toString();
    }

    private String cellToString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setDecimalSeparator('.');
                    DecimalFormat df = new DecimalFormat("0.##", symbols);
                    String formattedNumber = df.format(numericValue);
                    return formattedNumber;
                }
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private Map<String, Integer> getHeadersv(Row row, SubStructure subStructure) {
        Map<String, Integer> headers = new HashMap<>();
        List<SubStructureDetail> subStructureDetails = subStructure.getSubStructureDetails();
        for (int col = 0; col < row.getLastCellNum(); col++) {
            Cell cell = row.getCell(col);
            if (cell != null) {
                for (int i = 0; i < subStructureDetails.size(); i++) {
                    if (cell.getStringCellValue().equals(subStructureDetails.get(i).getName())) {
                        headers.put(subStructureDetails.get(i).getName(), col);
                    }
                }
            }
        }
        return headers;
    }

    private Map<Integer, String> getHeadersh(Sheet sheet, int firstRow, int lastRow, int firstCol, SubStructure subStructure) {
        Map<Integer, String> headers = new HashMap<>();
        List<SubStructureDetail> subStructureDetails = subStructure.getSubStructureDetails();
        for (int row = firstRow; row <= lastRow; row++) {
            Row headerRow = sheet.getRow(row);
            Cell cell = headerRow.getCell(firstCol);
            if (cell != null) {
                for (SubStructureDetail detail : subStructureDetails) {
                    if (cell.getStringCellValue().equals(detail.getName())) {
                        headers.put(row, detail.getName());
                        break;
                    }
                }
            }
        }

        return headers;
    }


    public List<CellRangeAddress> detectTables(Sheet sheet) {
        List<CellRangeAddress> tables = new ArrayList<>();
        int numRows = sheet.getPhysicalNumberOfRows();
        for (int i = 0; i < numRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                boolean isPotentialHeader = isPotentialHeader(row);
                if (isPotentialHeader) {
                    int endRow = findEndOfTable(sheet, i);
                    tables.add(new CellRangeAddress(i, endRow, row.getFirstCellNum(), row.getLastCellNum() - 1));
                    i = endRow; // Jump to the next row after the table
                }
            }
        }
        return tables;
    }

    private boolean isPotentialHeader(Row row) {
        // Add your logic to determine if a row could be a table header
        // For example, you can check if the row contains a mix of data types
        // Or if it contains certain keywords indicating it's a header
        // Return true if the row is a potential header, false otherwise
        return true; // Placeholder, replace with your logic
    }

    private int findEndOfTable(Sheet sheet, int startRow) {
        int numRows = sheet.getPhysicalNumberOfRows();
        for (int i = startRow + 1; i < numRows; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) {
                return i - 1;
            }
        }
        return numRows - 1; // If no empty row found, assume the table extends to the end of the sheet
    }

    private boolean isEmptyRow(Row row) {
        int numCells = row.getPhysicalNumberOfCells();
        for (int i = 0; i < numCells; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
