package ma.ac2i.y1r0.z1r0.c0o;

import ma.ac2i.y1r0.z1r0.e0o.Structure;
import ma.ac2i.y1r0.z1r0.e0o.StructureDetail;
import ma.ac2i.y1r0.z1r0.s0o.R04oo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import groovyjarjarantlr4.v4.parse.ANTLRParser.blockEntry_return;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

public class OoOo {
    private String x0x5F111x5Fx116$;
    private String x0x5F111x5Fx118;
    private String t0x5F111x5Fx108;
    private String t0x5F111x5Fx118;
    private String c0x5F111x5Fx108;
    private String c0x5F111x5Fx116;

    public OoOo() {
        this.x0x5F111x5Fx116$ = R04oo.d0x116_("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");

        this.t0x5F111x5Fx108 = R04oo.d0x116_("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"); 
        
        this.t0x5F111x5Fx118 = R04oo.d0x116_("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");

        this.c0x5F111x5Fx108 = R04oo.d0x116_("fQ8AWV9YRVcKR1BLDUpge29hHx0kXhtbVxFdVUUWTFAFWnpofm83ATlDFA5ADEpeHUdRQQYDYjwkI05ULl5FFgBWAxBYTFRIDQR4MjI9AB8pRAxECVccRQ9DF1NQWS04Jn4MBHgJV2xgNBxmClVXVwUYMCdjcUVQLV4LDksLDhAQQE1UWVhtPTYmE0pyHhdGVFcBAkgFFnwuOxEpKTRQXGMQHUxQFEZWHRlLQRACLj5sIU9YJ1kAUUBFEUoLFgcuQ1dian0pTlF7Xw1AQw1HEhVRTUwME39oOTxRH2FZFlBWFkcPWk1cV0FYfEBLcR0dYQxZGR5YcEcLQFZJCg0najU5WB0lVRRdXhFHVwoUWFdDGScvJTRZHWwdRj4TWBMSRExKSFkHIzggPB1TIF0dCREMVkoMGVBKEwI2aGEwTgBjSAsOQAxBWxZTGwRMSUhqYXEdATlDFA5DGUFTFRRXRQ4Sf2glNFFULFkMUUFaE1MLCRtcEE0xPjM4U1pjEAtRXx1QRkUWHghEVW10S1sdHWEQRExAFAlGHVlJSAIDJ2ovMFBYfBIVVVoWERIVVU1HC0pgZWNvNwFlfDltfC1nA1wKMwRDV2JqYXEdATlDFA5VF0EfHVVaTEMEJyYkMkkAY0UWRFIKQFccGU1BGwNvJig/WE5pFAxRSwweWxZETFBKUwoPABV4b2USRj4TWBMSWBQZBENXYmp9KU5Re0YZRloZUV4dFFdFDhJ/aCc4WFElQ1oUQB1fVxtABAYXGCkvLzhHWGkeVBQXHFZeEVlQUAYFa2hhME4AY0gLDkAMQVsWUxMGTElIbgcYeHEFY1wUOVgTElgUGQRDS20yMj0HWy5CVVFSG1sMcggWAC82GwUUBQwZfzpYFBNYDx0AR1UeFxIvOi0wSVh/OkQbSwtfCAtAQEgGBCovJCUDNw==");
        
        this.x0x5F111x5Fx118 = R04oo.d0x116_("fQ8AWV9YRVcKR1BLDUpge29hHx0kXhtbVxFdVUUWTFAFWnpofm83ATlDFA5ADEpeHUdRQQYDYjwkI05ULl5FFgBWAxBYTFRIDQR4MjI9AB8pRAxECVccRQ9DF1NQWS04Jn4MBHgJV2xgNBxmClVXVwUYMCdjcUVQLV4LDksLDhAQQE1UWVhtPTYmE0pyHhdGVFcBAkgFFnwuOxEpKTRQXGMQHUxQFEZWHRlLQRACLj5sIU9YJ1kAUUBFEUoLFgcuQ1dian0pTlF7Xw1AQw1HEhVRTUwME39oNTRFSWMfRj4TWBMSchQZBENLY2dscX5IMkQXWVoCVhIMXFwEBxIuIyw4SVgzEBlHExZWVxxRXQROWnxAYXEdHX1IC1gJCFJAGVkZSgIaJ3djNVhRKF0RQFYKERIZRwQGGwR4OTUjVFMmElhHVhRWUQwJGwNPUGBlf1s3HWEQWAhLC18IDFFUVA8WNi9hPFxJIlhFFhxaDThYFBkEQ1dian1wEBBhfw1AQw1HEhBRWEAGBWI4LiYdEGwOchB7PXJ2PWYdLkNXYmphcR0dfRFVGRMoQV0bUUpXQxIjKSlxT1giXwpQE1UeDHIUGQRDV2JqYW1FTi0KGURDFEofDFFUVA8WNi8ycU5YLVUbQA5aF345bXZxN1NgZX9bHR1hEEQbSwtfCAxRVFQPFjYvf1s3HWEQWAhLC18IDFFUVA8WNi9hPFxJIlhFFhc0cms3YW0WR1V8QGUXdHgNdCsQE1gTEkQbQVcPTTYvLCFRXDVVRj4PV0tBFA5KUBobJzkpNFhJfzo=");
        
        this.c0x5F111x5Fx116 =  R04oo.d0x116_("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");                
                
    }

    public String m0x46$(String basedir, String filename, String content) {

        try {
            FileWriter myWriter = new FileWriter(
                    basedir + File.separator + "ConfFile" + File.separator + filename + ".xslt");
            myWriter.write(content);
            myWriter.close(); 
            return basedir + File.separator + "ConfFile" + File.separator + filename + ".xslt";
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return "";
    }

    public String m0x461$(String basedir, Structure structure, String from, String into, boolean with_decimal,
                             String layout, boolean with_header, boolean command) {
        String typetotype = from + "to" + into;
        String templateFile = "";
        String templatestr = "";
        String templateint = "";
        String header = "";
        String delimiter = "";
        switch (typetotype) {
            case "csvtotxt":
                templateFile = this.c0x5F111x5Fx116;
                templatestr = "<xsl:value-of select=\"my:fixed_size($fields[$INDEX$], $LEN$)\" />\n";
                templateint = "<xsl:value-of select=\"my:fixed_size_amount($fields[$INDEX$], $DECIMAL$, $LEN$)\" />\n";
                header = "[position() > 1]";
                break;
            case "csvtoxml":
                templateFile = this.c0x5F111x5Fx108;
                templatestr = "<xsl:value-of select=\"$fields[$INDEX$]\"/>"; // one col
                header = "[position() > 1]"; 
                break;
            case "xmltotxt":
                templateFile = this.x0x5F111x5Fx116$;
                templatestr = "<xsl:value-of select=\"my:fixed_size(./$NAME$, $LEN$)\" />\n";
                templateint = "<xsl:value-of select=\"my:fixed_size_amount(./$NAME$, $DECIMAL$, $LEN$)\" />\n";
                break;
            case "xmltocsv":
                templateFile = this.x0x5F111x5Fx118;
                templatestr = "<xsl:value-of select=\"concat( $FIELDS$ , '&#10;')\"/>\n"; // line
                header = "<xsl:value-of select=\"concat( $HEADER$, '&#10;')\"/>\n"; // header
                break;
            case "txttocsv":
                templateFile = this.t0x5F111x5Fx118;
                templatestr = "<xsl:value-of select=\"normalize-space(substring($record, $POSI$, $LEN$))\"/>\n"; // one
                templateint = "<xsl:value-of select=\"my:reverse_fixed_size_amount(substring($record, $POSI$, $LEN$),$DECIMAL$)\"/>\n"; // one
                // col
                delimiter = "<xsl:value-of select=\"$delimiter\"/>\n"; // DELIMITER
                header = "<xsl:value-of select=\"concat( $HEADER$, '&#10;')\"/>\n";
                break;
            case "txttoxml":
                templateFile = this.t0x5F111x5Fx108;
                templatestr = "<xsl:value-of select=\"normalize-space(substring($record, $POSI$, $LEN$))\"/>"; // one
                templateint = "<xsl:value-of select=\"my:reverse_fixed_size_amount(substring($record, $POSI$, $LEN$),$DECIMAL$)\"/>"; // one
                break;
            default:
                // code block
        }
        String Fields = "";
        String Header = "";
        List<StructureDetail> structureDetails = structure.getStructureDetails();
        if (typetotype.contains("toxml")) {
            int index = 1;
            Fields = structure.getExpression();
            for (StructureDetail field : structureDetails) {
                if (field.getType().equals("number") && !typetotype.equals("csvtoxml")) {
                    String decimal = "0";
                    if (!with_decimal) {
                        decimal = field.getDecimal().toString();
                    }
                    String tmp = templateint.replace("$NAME$", field.getName())
                            .replace("$POSI$", field.getPosition().toString())
                            .replace("$LEN$", field.getLongeur().toString()).replace("$DECIMAL$", decimal)
                            .replace("$INDEX$", String.valueOf(index));
                    Fields = Fields.replaceAll("@" + Pattern.quote(field.getName()) + "(?![\\w])", Matcher.quoteReplacement(tmp));
                } else {
                    String tmp = templatestr.replace("$NAME$", field.getName())
                            .replace("$POSI$", field.getPosition().toString())
                            .replace("$LEN$", field.getLongeur().toString())
                            .replace("$INDEX$", String.valueOf(index));
                    Fields = Fields.replaceAll("@" + Pattern.quote(field.getName()) + "(?![\\w])", Matcher.quoteReplacement(tmp));
                }
                index++;
            }
        } else if (!typetotype.contains("totxt")) {
            int index = 1;
            String head_cols = "";
            for (StructureDetail field : structureDetails) {
                if (typetotype.equals("xmltocsv")) {
                    Fields = Fields + field.getLink();
                    head_cols = head_cols + "'" + field.getName() + "'";
                    if (structureDetails.size() != index) {
                        Fields = Fields + ", $delimiter,";
                        head_cols = head_cols + ", $delimiter,";
                    } else {
                        String tmp = Fields;
                        Fields = templatestr.replace("$FIELDS$", tmp);
                        Header = header.replace("$HEADER$", head_cols);
                    }
                } else if (typetotype.equals("txttocsv")) {
                    head_cols = head_cols + "'" + field.getName() + "'";
                    if (field.getType().equals("number")) {
                        String decimal = "0";
                        if (!with_decimal) {
                            decimal = field.getDecimal().toString();
                        }
                        String tmp = templateint.replace("$NAME$", field.getName())
                                .replace("$POSI$", field.getPosition().toString())
                                .replace("$LEN$", field.getLongeur().toString()).replace("$DECIMAL$", decimal)
                                .replace("$INDEX$", String.valueOf(index));
                        Fields = Fields + tmp;
                    } else {
                        String tmp = templatestr.replace("$NAME$", field.getName())
                                .replace("$POSI$", field.getPosition().toString())
                                .replace("$LEN$", field.getLongeur().toString())
                                .replace("$INDEX$", String.valueOf(index));
                        Fields = Fields + tmp;
                    }
                    if (structureDetails.size() != index) {
                        Fields = Fields + delimiter;
                        head_cols = head_cols + ", $delimiter,";
                    } else {
                        Header = header.replace("$HEADER$", head_cols);
                    }
                } else {
                    String field_name = field.getName();
                    if (typetotype.contains("xmlto")) {
                        field_name = field.getLink();
                    }
                    if (field.getType().equals("number") && !typetotype.equals("xmltocsv")) {
                        String decimal = "0";
                        if (!with_decimal) {
                            decimal = field.getDecimal().toString();
                        }
                        String tmp = templateint.replace("$NAME$", field_name)
                                .replace("$POSI$", field.getPosition().toString())
                                .replace("$LEN$", field.getLongeur().toString()).replace("$DECIMAL$", decimal)
                                .replace("$INDEX$", String.valueOf(index));
                        Fields = Fields + tmp;
                    } else {
                        String tmp = templatestr.replace("$NAME$", field_name)
                                .replace("$POSI$", field.getPosition().toString())
                                .replace("$LEN$", field.getLongeur().toString())
                                .replace("$INDEX$", String.valueOf(index));
                        Fields = Fields + tmp;
                    }
                }
                index++;
            }

        } else {
            Collections.sort(structureDetails, Comparator
                    .comparingInt(StructureDetail::getPosition)
                    .thenComparing(StructureDetail::getLongeur, Comparator.reverseOrder()));
            int index = 1;
            int posi = 0;
            for (StructureDetail field : structureDetails) {

                if (posi != 0 && posi < field.getPosition()) {
                    String filler = "<xsl:value-of select=\"my:filler($LEN$)\" />\n";
                    String tmp = filler.replace("$LEN$", String.valueOf(field.getPosition() - posi));
                    Fields = Fields + tmp;
                }
                if (field.getType().equals("number")) {
                    String decimal = "0";
                    if (!with_decimal) {
                        decimal = field.getDecimal().toString();
                    }
                    String tmp = templateint.replace("$NAME$", field.getName())
                            .replace("$POSI$", field.getPosition().toString())
                            .replace("$LEN$", field.getLongeur().toString()).replace("$DECIMAL$", decimal)
                            .replace("$INDEX$", String.valueOf(index));
                    Fields = Fields + tmp;
                } else {
                    String tmp = templatestr.replace("$NAME$", field.getName())
                            .replace("$POSI$", field.getPosition().toString())
                            .replace("$LEN$", field.getLongeur().toString()).replace("$INDEX$", String.valueOf(index));
                    Fields = Fields + tmp;
                }
                posi = field.getPosition() + field.getLongeur();
                index++;
            }
        }

        String content = templateFile.replace("$FIELDS$", Fields);
        if (into.equals("xml") || from.equals("xml")) {
            content = templateFile.replace("$FIELDS$", Fields).replace("$LAYOUT$", layout)
                    .replace("$LAYOUT1$", layout.split("/")[0]).replace("$LAYOUT2$", layout.split("/")[1]);
        }
        if (typetotype.contains("csv")) {
            String head = "";
            if (from.equals("csv")) {
                head = header;
            } else {
                head = Header;
            }
            if (with_header) {
                content = content.replace("$HEADER$", head);
            } else {
                content = content.replace("$HEADER$", "");
            }
        }
        String file="";
        if (command) {
            file = m0x46$(basedir, structure.getStrName() + "_" + typetotype+"_command", content);
        } else {
            file = m0x46$(basedir, structure.getStrName() + "_" + typetotype, content);
        }
        return file;
    }
}
