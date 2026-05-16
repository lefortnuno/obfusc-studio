package ma.ac2i.converter.converter.config;

import ma.ac2i.converter.converter.entities.Structure;
import ma.ac2i.converter.converter.entities.StructureDetail;

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

public class FileTemplate {
    private String xml_to_txt;
    private String xml_to_csv;
    private String txt_to_xml;
    private String txt_to_csv;
    private String csv_to_xml;
    private String csv_to_txt;

    public FileTemplate() {
        this.xml_to_txt = "<?xml version='1.0' encoding='utf-8'?>\n" + "<xsl:stylesheet version=\"3.0\"\n"
                + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n"
                + "    xmlns:str=\"http://exslt.org/strings\"\n"
                + "    xmlns:math=\"http://www.w3.org/2005/xpath-functions/math\"\n"
                + "    xmlns:json=\"http://www.w3.org/2005/xpath-functions\"\n"
                + "    xmlns:my=\"http://example.com/my-functions\"\n" + "    \n"
                + "    extension-element-prefixes=\"str\"\n" + "    exclude-result-prefixes=\"xs json math my str\">\n"
                + "\n"
                + "    <!-- Boilerplate: do not change -->\n"
                + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                + "    <xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\" />\n"
                + "    <xsl:strip-space elements=\"*\" />\n" + "    <xsl:variable name=\"newline\">\n"
                + "        <xsl:text>&#10;</xsl:text>\n" + "    </xsl:variable>\n"
                + "    <xsl:variable name=\"some_spaces\" select=\"'                                                                                                                                                      '\" />\n"
                + "    <xsl:variable name=\"some_zeroes\" select=\"'000000000000000000000000000000000000000000000000000000000000'\" />\n"
                + "\n" + "    <xsl:function name=\"my:fixed_size\">\n" + "        <xsl:param name=\"sValue\"/>\n"
                + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:sequence select=\"substring(concat($sValue, $some_spaces), 1, $iWidth)\"/>\n"
                + "    </xsl:function>\n" + "\n" + "    <xsl:function name=\"my:fixed_size_amount\">\n"
                + "        <xsl:param name=\"sValue\"/>\n" + "        <xsl:param name=\"nDecimal\"/>\n"
                + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:variable name=\"amount\" select=\"number($sValue) * math:exp10($nDecimal)\"/>\n"
                + "        <xsl:choose>\n"
                + "            <xsl:when test=\"$nDecimal = 0 and string($amount) != 'NaN'\">\n"
                + "                <xsl:sequence\n"
                + "                    select=\"concat(substring($some_zeroes, 1, $iWidth -  string-length(string($sValue))), string($sValue))\"/>\n"
                + "            </xsl:when>\n" + "            <xsl:when test=\"string($amount) != 'NaN'\">\n"
                + "                <xsl:sequence\n"
                + "                    select=\"format-number($amount, concat(substring($some_zeroes, 1, $iWidth), ';-', substring($some_zeroes, 1, $iWidth - 1)))\"/>\n"
                + "            </xsl:when>\n" + "            <xsl:otherwise>\n"
                + "                <xsl:sequence select=\"substring($some_zeroes, 1, $iWidth)\"/>\n"
                + "            </xsl:otherwise>\n" + "        </xsl:choose>\n" + "    </xsl:function>\n" + "\n"
                + "    <xsl:function name=\"my:filler\">\n" + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:sequence select=\"substring($some_spaces, 1, $iWidth)\"/>\n" + "    </xsl:function>\n"
                + "\n" + "    <xsl:template match=\"text()\"/>\n" + "    \n"
                + "    <!-- Main template for processing XML -->\n" + "    <xsl:template name=\"main\" match=\"/\">\n"
                + "        <xsl:apply-templates select=\"$LAYOUT$\" />\n" + "    </xsl:template>\n" + "\n"
                + "    <!-- Template for each record in your XML -->\n" + "    <xsl:template match=\"$LAYOUT$\">\n"
                + "$FIELDS$" + "        <xsl:value-of select=\"$newline\"/>\n" + "    </xsl:template>\n" + "\n"
                + "</xsl:stylesheet>";
        //+ "        <$LAYOUT2$>\n"
        //+ "        </$LAYOUT2$>\n"
        this.txt_to_xml = """
                <?xml version="1.0" encoding="utf-8"?>
                <xsl:stylesheet version="3.0"
                    xmlns:xs="http://www.w3.org/2001/XMLSchema"
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="http://exslt.org/strings"
                                xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                                xmlns:json="http://www.w3.org/2005/xpath-functions"
                    xmlns:my="http://example.com/my-functions"
                    exclude-result-prefixes="xs json math my str">

                    <xsl:param name="text-input" as="xs:string" />
                    <xsl:output method="xml" indent="yes"/>
                <xsl:function name="my:reverse_fixed_size_amount">
                        <xsl:param name="sValue"/>
                        <xsl:param name="nDecimal"/>
                        <xsl:variable name="numericValue" select="number($sValue)"/>
                        <xsl:choose>
                            <xsl:when test="string($sValue) != ''">
                                <xsl:variable name="decimalFactor" select="math:exp10($nDecimal)"/>
                                <xsl:variable name="amount" select="$numericValue "/>
                                <xsl:choose>
                                    <xsl:when test="$nDecimal = 0">
                                        <xsl:sequence
                                                select="format-number($amount, '####')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence
                                                select="format-number($amount div $decimalFactor, concat('####.', substring('0000000000', 1, $nDecimal)))"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="0"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:function>
                    <xsl:template name="main" match="/">
                        <$LAYOUT1$>
                            <xsl:for-each select="unparsed-text-lines($text-input)">
                                <xsl:if test="normalize-space(.) ne ''">
                                    <xsl:call-template name="process-record">
                                        <xsl:with-param name="record" select="."/>
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:for-each>
                        </$LAYOUT1$>
                    </xsl:template>

                    <xsl:template name="process-record">
                        <xsl:param name="record"/>
                $FIELDS$\s
                    </xsl:template>

                </xsl:stylesheet>
                """;
        this.txt_to_csv = """
                <?xml version="1.0" encoding="utf-8"?>
                <xsl:stylesheet version="3.0"
                    xmlns:xs="http://www.w3.org/2001/XMLSchema"
                    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                  xmlns:str="http://exslt.org/strings"
                                xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                                xmlns:json="http://www.w3.org/2005/xpath-functions"
                    xmlns:my="http://example.com/my-functions"
                    exclude-result-prefixes="xs json math my str">
                <xsl:function name="my:reverse_fixed_size_amount">
                        <xsl:param name="sValue"/>
                        <xsl:param name="nDecimal"/>
                        <xsl:variable name="numericValue" select="number($sValue)"/>
                        <xsl:choose>
                            <xsl:when test="string($sValue) != ''">
                                <xsl:variable name="decimalFactor" select="math:exp10($nDecimal)"/>
                                <xsl:variable name="amount" select="$numericValue"/>
                                <xsl:choose>
                                    <xsl:when test="$nDecimal = 0">
                                        <xsl:sequence
                                                select="format-number($amount, '####')"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence
                                                select="format-number($amount div $decimalFactor, concat('####.', substring('0000000000', 1, $nDecimal)))"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="0"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:function>
                    <xsl:param name="text-input" as="xs:string" />
                    <xsl:param name="delimiter" as="xs:string" select="','"/>
                    <xsl:output method="text"/>

                    <xsl:template name="main" match="/">
                $HEADER$\s
                        <xsl:for-each select="unparsed-text-lines($text-input)">
                            <xsl:if test="normalize-space(.) ne ''">
                                <xsl:call-template name="process-record">
                                    <xsl:with-param name="record" select="."/>
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:template>

                    <xsl:template name="process-record">
                        <xsl:param name="record"/>
                $FIELDS$<xsl:text>&#10;</xsl:text>
                </xsl:template>

                </xsl:stylesheet>
                """;
        this.csv_to_xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n"
                + "    <xsl:output method=\"xml\" indent=\"yes\"/>\n" + "\n"
                + "    <!-- Customize the delimiter as needed -->\n"
                + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n" + "\n"
                + "    <xsl:template name=\"main\" match=\"/\">\n"
                + "<$LAYOUT1$>\n"
                + "        <xsl:for-each select=\"unparsed-text-lines($text-input)$HEADER$\">\n"
                + "            <xsl:variable name=\"fields\" select=\"tokenize(., $delimiter)\" as=\"xs:string*\"/>\n"
                + "$FIELDS$ \n"
                + "        </xsl:for-each>\n"
                + "</$LAYOUT1$>\n"
                + "    </xsl:template>\n"
                + "</xsl:stylesheet>\n";
        
        this.xml_to_csv = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n"
                + "    <xsl:output method=\"text\"/>\n"
                + "    \n"
                + "    <!-- Customize the delimiter as needed -->\n"
                + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n"
                + "\n"
                + "    <xsl:template match=\"/\">\n"
                + "        <!-- Output header row -->\n"
                + "$HEADER$\n"
                + "        <!-- Process each record -->\n"
                + "        <xsl:apply-templates select=\"$LAYOUT$\"/>\n"
                + "    </xsl:template>\n"
                + "\n"
                + "    <xsl:template match=\"$LAYOUT2$\">\n"
                + "$FIELDS$"
                + "    </xsl:template>\n"
                + "</xsl:stylesheet>\n";
        
        this.csv_to_txt = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<xsl:stylesheet version=\"3.0\"\n"
                + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n"
                + "    xmlns:str=\"http://exslt.org/strings\"\n"
                + "    xmlns:math=\"http://www.w3.org/2005/xpath-functions/math\"\n"
                + "    xmlns:json=\"http://www.w3.org/2005/xpath-functions\"\n"
                + "    xmlns:my=\"http://example.com/my-functions\"\n"
                + "    extension-element-prefixes=\"str\"\n"
                + "    exclude-result-prefixes=\"xs\">\n"
                + "\n"
                + "    <!-- Customize the delimiter as needed -->\n"
                + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n"
                + "\n"
                + "        <xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\" />\n"
                + "    <xsl:strip-space elements=\"*\" />\n"
                + "    <xsl:variable name=\"newline\">\n"
                + "        <xsl:text>&#10;</xsl:text>\n"
                + "    </xsl:variable>\n"
                + "    <xsl:variable name=\"some_spaces\" select=\"'                                                                                                                                                      '\" />\n"
                + "    <xsl:variable name=\"some_zeroes\" select=\"'000000000000000000000000000000000000000000000000000000000000'\" />\n"
                + "\n"
                + "    <xsl:function name=\"my:fixed_size\">\n"
                + "        <xsl:param name=\"sValue\"/>\n"
                + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:sequence select=\"substring(concat($sValue, $some_spaces), 1, $iWidth)\"/>\n"
                + "    </xsl:function>\n"
                + "\n"
                + "    <xsl:function name=\"my:fixed_size_amount\">\n"
                + "        <xsl:param name=\"sValue\"/>\n"
                + "        <xsl:param name=\"nDecimal\"/>\n"
                + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:variable name=\"amount\" select=\"number($sValue) * math:exp10($nDecimal)\"/>\n"
                + "        <xsl:choose>\n"
                + "            <xsl:when test=\"$nDecimal = 0 and string($amount) != 'NaN'\">\n"
                + "                <xsl:sequence\n"
                + "                    select=\"concat(substring($some_zeroes, 1, $iWidth -  string-length(string($sValue))), string($sValue))\"/>\n"
                + "            </xsl:when>\n"
                + "            <xsl:when test=\"string($amount) != 'NaN'\">\n"
                + "                <xsl:sequence\n"
                + "                    select=\"format-number($amount, concat(substring($some_zeroes, 1, $iWidth), ';-', substring($some_zeroes, 1, $iWidth - 1)))\"/>\n"
                + "            </xsl:when>\n"
                + "            <xsl:otherwise>\n"
                + "                <xsl:sequence select=\"substring($some_zeroes, 1, $iWidth)\"/>\n"
                + "            </xsl:otherwise>\n"
                + "        </xsl:choose>\n"
                + "    </xsl:function>\n"
                + "\n"
                + "    <xsl:function name=\"my:filler\">\n"
                + "        <xsl:param name=\"iWidth\"/>\n"
                + "        <xsl:sequence select=\"substring($some_spaces, 1, $iWidth)\"/>\n"
                + "    </xsl:function>\n"
                + "\n"
                + "    <xsl:template match=\"text()\"/>\n"
                + "\n"
                + "    <xsl:template name=\"main\" match=\"/\">\n"
                + "        <xsl:for-each select=\"unparsed-text-lines($text-input)$HEADER$\">\n"
                + "            <xsl:variable name=\"fields\" select=\"tokenize(., $delimiter)\" as=\"xs:string*\"/>\n"
                + "$FIELDS$"
                + "            <xsl:value-of select=\"$newline\"/>            \n"
                + "        </xsl:for-each>\n"
                + "    </xsl:template>\n"
                + "</xsl:stylesheet>\n";
    }

    public String makeFile(String basedir, String filename, String content) {

        try {
            FileWriter myWriter = new FileWriter(
                    basedir + File.separator + "ConfFile" + File.separator + filename + ".xslt");
            myWriter.write(content);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
            return basedir + File.separator + "ConfFile" + File.separator + filename + ".xslt";
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return "";
    }

    public String makeFields(String basedir, Structure structure, String from, String into, boolean with_decimal,
                             String layout, boolean with_header, boolean command) {
        String typetotype = from + "to" + into;
        String templateFile = "";
        String templatestr = "";
        String templateint = "";
        String header = "";
        String delimiter = "";
        switch (typetotype) {
            case "csvtotxt":
                templateFile = this.csv_to_txt;
                templatestr = "<xsl:value-of select=\"my:fixed_size($fields[$INDEX$], $LEN$)\" />\n";
                templateint = "<xsl:value-of select=\"my:fixed_size_amount($fields[$INDEX$], $DECIMAL$, $LEN$)\" />\n";
                header = "[position() > 1]";
                break;
            case "csvtoxml":
                templateFile = this.csv_to_xml;
                templatestr = "<xsl:value-of select=\"$fields[$INDEX$]\"/>"; // one col
                header = "[position() > 1]";
                // templateint = "<xsl:value-of select=\"concat('ID', $delimiter, 'Name',
                // $delimiter, 'Age', '&#10;')\"/>\n"; // header
                break;
            case "xmltotxt":
                templateFile = this.xml_to_txt;
                templatestr = "<xsl:value-of select=\"my:fixed_size(./$NAME$, $LEN$)\" />\n";
                templateint = "<xsl:value-of select=\"my:fixed_size_amount(./$NAME$, $DECIMAL$, $LEN$)\" />\n";
                break;
            case "xmltocsv":
                templateFile = this.xml_to_csv;
                templatestr = "<xsl:value-of select=\"concat( $FIELDS$ , '&#10;')\"/>\n"; // line
                header = "<xsl:value-of select=\"concat( $HEADER$, '&#10;')\"/>\n"; // header
                break;
            case "txttocsv":
                templateFile = this.txt_to_csv;
                templatestr = "<xsl:value-of select=\"normalize-space(substring($record, $POSI$, $LEN$))\"/>\n"; // one
                templateint = "<xsl:value-of select=\"my:reverse_fixed_size_amount(substring($record, $POSI$, $LEN$),$DECIMAL$)\"/>\n"; // one
                // col
                delimiter = "<xsl:value-of select=\"$delimiter\"/>\n"; // DELIMITER
                header = "<xsl:value-of select=\"concat( $HEADER$, '&#10;')\"/>\n";
                break;
            case "txttoxml":
                templateFile = this.txt_to_xml;
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
            file = makeFile(basedir, structure.getStrName() + "_" + typetotype+"_command", content);
        } else {
            file = makeFile(basedir, structure.getStrName() + "_" + typetotype, content);
        }
        return file;
    }
}
