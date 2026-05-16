package ma.ac2i.y1r0.z1r0.s0o;

import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ac2i.y1r0.z1r0.e0o.*;
import ma.ac2i.y1r0.z1r0.r0o.W03o;
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
public class RO6o0o {

    private String appDirectory;
    private W03o c0x53x52$;

    public RO6o0o(W03o c0x53x52$) {
        this.c0x53x52$ = c0x53x52$;
    }

    public void test(String i0x104, ComplexStructure c0x53) {
        IOUtils.setByteArrayMaxOverride(500000000);
        try (Workbook w0x107 = WorkbookFactory.create(new FileInputStream(i0x104))) {
            String f0x50 = appDirectory + File.separator + "download" + File.separator + c0x53.getName() + ".xml";
            try {
                String c0x116 = c0x114$(w0x107, c0x53);
                try (BufferedWriter b0x119 = Files.newBufferedWriter(Paths.get(f0x50), StandardCharsets.UTF_8)) {
                    b0x119.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    b0x119.write(c0x116);
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

    public String c0x114$(Workbook w0x107, ComplexStructure c0x53) throws Exception {
        Sheet s0x116 = w0x107.getSheetAt(0);
        List<CellRangeAddress> t0x115 = detectTables(s0x116);
        System.out.println(t0x115.size());
        List<SubStructure> s0x53 = c0x53.getSubStructures();
        if (t0x115.size() != s0x53.size())
            throw new RuntimeException("number of tables don't match the number of sub-structure");
        int i0x120 = 0;
        Map<String, String> S0x53x58$ = new HashMap<>();
        for (CellRangeAddress t0x101 : t0x115) {
            String s0x53x58 = "";
            int f0x52 = t0x101.getFirstRow();
            int l0x52 = t0x101.getLastRow();
            int f0x43 = t0x101.getFirstColumn();
            int l0x43 = t0x101.getLastColumn();
            List<SubStructureDetail> s0x53x44 = s0x53.get(i0x120).getSubStructureDetails();
            String p0x110 = s0x53.get(i0x120).getExpression();
            if (s0x53.get(i0x120).getType().equals("vertical")) {
                Map<String, Integer> f0x4E$ = getHeadersv(s0x116.getRow(f0x52), s0x53.get(i0x120));
                for (int r0x119 = f0x52 + 1; r0x119 <= l0x52; r0x119++) {
                    String o0x116 = p0x110;
                    Row d0x52 = s0x116.getRow(r0x119);
                    for (SubStructureDetail ssd : s0x53x44) {
                        Integer i0x5Fx108 = f0x4E$.get(ssd.getName());
                        if (i0x5Fx108 == null) {
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", "");
                            continue;
                        }
                        Cell c0x108 = d0x52.getCell(i0x5Fx108);
                        if (c0x108.getCellType() == CellType.NUMERIC) {
                            double n0x56 = c0x108.getNumericCellValue();
                            DecimalFormatSymbols s0x115 = new DecimalFormatSymbols();
                            s0x115.setDecimalSeparator('.');
                            DecimalFormat df = new DecimalFormat("0.##", s0x115);
                            String f0x4E = df.format(n0x56);
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", Matcher.quoteReplacement(f0x4E));
                        } else if (c0x108.getCellType() == CellType.STRING) {
                            // Handle string value
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", Matcher.quoteReplacement(c0x108.getStringCellValue()));
                        } else if (c0x108.getCellType() == CellType.BLANK) {
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(ssd.getName()) + "(?![\\w])", "");
                        }
                    }
                    if (!o0x116.equals(p0x110)) {
                        s0x53x58 = s0x53x58 + o0x116;
                    }
                }
            } else {
                Map<Integer, String> f0x4E$ = getHeadersh(s0x116, f0x52, l0x52, f0x43, s0x53.get(i0x120));
                for (int c0x1081 = f0x43 + 1; c0x1081 <= l0x43; c0x1081++) {
                    String o0x116 = p0x110;
                    for (int r0x119 = f0x52; r0x119 <= l0x52; r0x119++) {
                        Row h0x52 = s0x116.getRow(r0x119);
                        Cell c0x108 = h0x52.getCell(c0x1081);
                        String v0x5Fx102 = f0x4E$.get(r0x119);
                        if (v0x5Fx102 == null) {
                            continue;
                        }
                        if (c0x108.getCellType() == CellType.NUMERIC) {
                            double n0x56 = c0x108.getNumericCellValue();
                            DecimalFormatSymbols s0x115 = new DecimalFormatSymbols();
                            s0x115.setDecimalSeparator('.');
                            DecimalFormat df = new DecimalFormat("0.##", s0x115);
                            String f0x4E = df.format(n0x56);
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(v0x5Fx102) + "(?![\\w])", Matcher.quoteReplacement(f0x4E));
                        } else if (c0x108.getCellType() == CellType.STRING) {
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(v0x5Fx102) + "(?![\\w])", Matcher.quoteReplacement(c0x108.getStringCellValue()));
                        } else if (c0x108.getCellType() == CellType.BLANK) {
                            o0x116 = o0x116.replaceAll("@" + Pattern.quote(v0x5Fx102) + "(?![\\w])", "");
                        }
                    }
                    if (!o0x116.equals(p0x110)) {
                        s0x53x58 = s0x53x58 + o0x116;
                    }
                }
            }
            S0x53x58$.put(s0x53.get(i0x120).getName(), s0x53x58);
            i0x120++;
        }
        String c0x50 = c0x53.getExpression();
        String o0x116 = c0x50;
        for (String k0x121 : S0x53x58$.keySet()) {
            String v0x101 = S0x53x58$.get(k0x121);
            o0x116 = o0x116.replaceAll("@" + Pattern.quote(k0x121) + "(?![\\w])", Matcher.quoteReplacement(v0x101));
        }
        return o0x116;
    }


    public String convertTablesToCsv(String i0x104, Structure s0x101, String o0x5Fx101, String d0x114, boolean h0x114,boolean h0x5Fx1161) {
        IOUtils.setByteArrayMaxOverride(500000000);
        try (Workbook w0x107 = WorkbookFactory.create(new FileInputStream(i0x104))) {
            String f0x50 = appDirectory + File.separator + "upload" + File.separator + s0x101.getStrName() + ".csv";
            if (o0x5Fx101.equals("csv")) {
                f0x50 = appDirectory + File.separator + "download" + File.separator + s0x101.getStrName() + ".csv";
            }
            Sheet s0x116 = w0x107.getSheetAt(0);
            List<CellRangeAddress> t0x115 = detectTables(s0x116);
            for (CellRangeAddress t0x52 : t0x115) {
                System.out.println(t0x52.toString());
                try {
                    String c0x116 = "";
                    if (o0x5Fx101.equals("csv")) {
                        c0x116 = convertTableToCsv(s0x116, t0x52, d0x114, h0x114);
                        String h0x5Fx116="";
                        int h0x105x120i = 1;
                        for(StructureDetail structureDetail : s0x101.getStructureDetails()){
                            h0x5Fx116 = h0x5Fx116 + structureDetail.getName();
                            if (h0x105x120i != s0x101.getStructureDetails().size()){
                                h0x5Fx116 = h0x5Fx116 + d0x114;
                            }
                        }
                        if (h0x5Fx1161 ) {
                            c0x116 = h0x5Fx116 + "\n"+c0x116;
                        }
                    } else {
                        c0x116 = convertTableToCsv(s0x116, t0x52, h0x114);
                    }
                    try (BufferedWriter b0x119 = Files.newBufferedWriter(Paths.get(f0x50), StandardCharsets.UTF_8)) {
                        b0x119.write(c0x116);
                    }
                    if (o0x5Fx101.equals("csv")) {
                        return s0x101.getStrName() + ".csv";
                    }
                    return f0x50;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String convertTableToCsv(Sheet s0x116, CellRangeAddress t0x52, String d0x114, boolean h0x114) {
        StringBuilder c0x44 = new StringBuilder();
        int f0x5Fx119 = t0x52.getFirstRow();
        if (h0x114 ) {
            f0x5Fx119++;
        }
        for (int i = f0x5Fx119; i <= t0x52.getLastRow(); i++) {
            Row r0x119 = s0x116.getRow(i);
            if (r0x119 != null) {
                for (int j = t0x52.getFirstColumn(); j <= t0x52.getLastColumn(); j++) {
                    Cell c0x108 = r0x119.getCell(j);
                    if (c0x108 != null) {
                        c0x44.append(cellToString(c0x108)).append(d0x114);
                    }
                }
                c0x44.append("\n");
            }
        } 
        return c0x44.toString();
    }

    private String convertTableToCsv(Sheet s0x116, CellRangeAddress t0x52, boolean h0x114) {
        StringBuilder c0x44 = new StringBuilder();
        int f0x5Fx119 = t0x52.getFirstRow();
        if (h0x114 ) {
            f0x5Fx119++;
        }
        for (int i = f0x5Fx119; i <= t0x52.getLastRow(); i++) {
            Row r0x119 = s0x116.getRow(i);
            if (r0x119 != null) {
                for (int j = t0x52.getFirstColumn(); j <= t0x52.getLastColumn(); j++) {
                    Cell c0x108 = r0x119.getCell(j);
                    if (c0x108 != null) {
                        c0x44.append(cellToString(c0x108)).append(",");
                    }
                }
                c0x44.append("\n");
            }
        } 
        return c0x44.toString();
    }

    private String cellToString(Cell c0x108) {
        switch (c0x108.getCellType()) {
            case STRING:
                return c0x108.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(c0x108)) {
                    return c0x108.getDateCellValue().toString();
                } else {
                    double n0x56 = c0x108.getNumericCellValue();
                    DecimalFormatSymbols s0x115 = new DecimalFormatSymbols();
                    s0x115.setDecimalSeparator('.');
                    DecimalFormat df = new DecimalFormat("0.##", s0x115);
                    String f0x4E = df.format(n0x56);
                    return f0x4E;
                }
            case BOOLEAN:
                return Boolean.toString(c0x108.getBooleanCellValue());
            case FORMULA:
                return c0x108.getCellFormula();
            default:
                return "";
        }
    }

    private Map<String, Integer> getHeadersv(Row r0x119, SubStructure s0x531) {
        Map<String, Integer> h0x1141 = new HashMap<>();
        List<SubStructureDetail> s0x53x44 = s0x531.getSubStructureDetails();
        for (int c0x1081 = 0; c0x1081 < r0x119.getLastCellNum(); c0x1081++) {
            Cell c0x108 = r0x119.getCell(c0x1081);
            if (c0x108 != null) {
                for (int i = 0; i < s0x53x44.size(); i++) {
                    if (c0x108.getStringCellValue().equals(s0x53x44.get(i).getName())) {
                        h0x1141.put(s0x53x44.get(i).getName(), c0x1081);
                    }
                }
            }
        }
        return h0x1141;
    }

    private Map<Integer, String> getHeadersh(Sheet s0x116, int f0x52, int l0x52, int f0x43, SubStructure s0x531) {
        Map<Integer, String> h0x1141 = new HashMap<>();
        List<SubStructureDetail> s0x53x44 = s0x531.getSubStructureDetails();
        for (int r0x119 = f0x52; r0x119 <= l0x52; r0x119++) {
            Row h0x52 = s0x116.getRow(r0x119);
            Cell c0x108 = h0x52.getCell(f0x43);
            if (c0x108 != null) {
                for (SubStructureDetail d0x108 : s0x53x44) {
                    if (c0x108.getStringCellValue().equals(d0x108.getName())) {
                        h0x1141.put(r0x119, d0x108.getName());
                        break;
                    }
                }
            }
        }

        return h0x1141;
    }


    public List<CellRangeAddress> detectTables(Sheet s0x116) {
        List<CellRangeAddress> t0x115 = new ArrayList<>();
        int n0x52 = s0x116.getPhysicalNumberOfRows();
        for (int i = 0; i < n0x52; i++) {
            Row r0x119 = s0x116.getRow(i);
            if (r0x119 != null) {
                boolean i0x50x48 = i0x50x48(r0x119);
                if (i0x50x48) {
                    int endRow = findEndOfTable(s0x116, i);
                    t0x115.add(new CellRangeAddress(i, endRow, r0x119.getFirstCellNum(), r0x119.getLastCellNum() - 1));
                    i = endRow; // Jump to the next row after the table
                }
            }
        }
        return t0x115;
    }

    private boolean i0x50x48(Row r0x119) {
        // Add your logic to determine if a row could be a table header
        // For example, you can check if the row contains a mix of data types
        // Or if it contains certain keywords indicating it's a header
        // Return true if the row is a potential header, false otherwise
        return true; // Placeholder, replace with your logic
    }

    private int findEndOfTable(Sheet s0x116, int s0x52) {
        int n0x52 = s0x116.getPhysicalNumberOfRows();
        for (int i = s0x52 + 1; i < n0x52; i++) {
            Row r0x119 = s0x116.getRow(i);
            if (r0x119 == null || isEmptyRow(r0x119)) {
                return i - 1;
            }
        }
        return n0x52 - 1; // If no empty row found, assume the table extends to the end of the sheet
    }

    private boolean isEmptyRow(Row r0x119) {
        int n0x43 = r0x119.getPhysicalNumberOfCells();
        for (int i = 0; i < n0x43; i++) {
            Cell c0x108 = r0x119.getCell(i);
            if (c0x108 != null && c0x108.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
