import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;


public class SecureTransformService {

    private static final String SECRET_KEY = "A0x43x32x49$cwBJAQ==";  // clé XOR

    // --- Fonction d'encrypt ---
    public static String encrypt(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);  // XOR
        }

        return Base64.getEncoder().encodeToString(result);
    }

    // --- Fonction de decrypt_ ---
    public static String decrypt_(String encoded) {
        byte[] data = Base64.getDecoder().decode(encoded);
        byte[] key = "A0x43x32x49$cwBJAQ==".getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);  // XOR inverse
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    // Test
    public static void main(String[] args) {
        List<String> ox72 = new ArrayList<>();
        String[] originals1 = {
            // "admin",
            // "root",
            // "Admin@2015",
            // "ADMIN",
            // "USER",
            // "upload",
            // "download",
            // "ConfFile",

            // "not done",
            // "done",

            // "AES",
            // "AES/CBC/PKCS5Padding",
            // "ac2i@license2015",
            // "1999201520229999",
            // "dd-MM-yyyy",

            // "HOSTNAME",
            // "EXPDATE",
            // "MOD_BASE",

            // "true",
            // "True",

            // "LicenceDecryptee{",
            // "hostname='",
            // ", expDate='",
            // ", modBase='",
            // ", expirationDate=",
            // ", MOD_BASE=",
            // "}",
            
            // "Nom d'utilisateur ou mot de passe incorrect",
            // "Le compte utilisateur est verrouillé", 
            // "Le compte utilisateur est désactivé", 
            // "Le compte utilisateur a expiré", 
            // "Les informations d'identification de l'utilisateur ont expiré", 
            // "Échec de l'authentification",


        };

        String[] originals2 = {
            // "<?xml version='1.0' encoding='utf-8'?>\n" + "<xsl:stylesheet version=\"3.0\"\n"
                // + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
                // + "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n"
                // + "    xmlns:str=\"http://exslt.org/strings\"\n"
                // + "    xmlns:math=\"http://www.w3.org/2005/xpath-functions/math\"\n"
                // + "    xmlns:json=\"http://www.w3.org/2005/xpath-functions\"\n"
                // + "    xmlns:my=\"http://example.com/my-functions\"\n" + "    \n"
                // + "    extension-element-prefixes=\"str\"\n" + "    exclude-result-prefixes=\"xs json math my str\">\n"
                // + "\n"
                // + "    <!-- Boilerplate: do not change -->\n"
                // + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                // + "    <xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\" />\n"
                // + "    <xsl:strip-space elements=\"*\" />\n" + "    <xsl:variable name=\"newline\">\n"
                // + "        <xsl:text>&#10;</xsl:text>\n" + "    </xsl:variable>\n"
                // + "    <xsl:variable name=\"some_spaces\" select=\"'                                                                                                                                                      '\" />\n"
                // + "    <xsl:variable name=\"some_zeroes\" select=\"'000000000000000000000000000000000000000000000000000000000000'\" />\n"
                // + "\n" + "    <xsl:function name=\"my:fixed_size\">\n" + "        <xsl:param name=\"sValue\"/>\n"
                // + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:sequence select=\"substring(concat($sValue, $some_spaces), 1, $iWidth)\"/>\n"
                // + "    </xsl:function>\n" + "\n" + "    <xsl:function name=\"my:fixed_size_amount\">\n"
                // + "        <xsl:param name=\"sValue\"/>\n" + "        <xsl:param name=\"nDecimal\"/>\n"
                // + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:variable name=\"amount\" select=\"number($sValue) * math:exp10($nDecimal)\"/>\n"
                // + "        <xsl:choose>\n"
                // + "            <xsl:when test=\"$nDecimal = 0 and string($amount) != 'NaN'\">\n"
                // + "                <xsl:sequence\n"
                // + "                    select=\"concat(substring($some_zeroes, 1, $iWidth -  string-length(string($sValue))), string($sValue))\"/>\n"
                // + "            </xsl:when>\n" + "            <xsl:when test=\"string($amount) != 'NaN'\">\n"
                // + "                <xsl:sequence\n"
                // + "                    select=\"format-number($amount, concat(substring($some_zeroes, 1, $iWidth), ';-', substring($some_zeroes, 1, $iWidth - 1)))\"/>\n"
                // + "            </xsl:when>\n" + "            <xsl:otherwise>\n"
                // + "                <xsl:sequence select=\"substring($some_zeroes, 1, $iWidth)\"/>\n"
                // + "            </xsl:otherwise>\n" + "        </xsl:choose>\n" + "    </xsl:function>\n" + "\n"
                // + "    <xsl:function name=\"my:filler\">\n" + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:sequence select=\"substring($some_spaces, 1, $iWidth)\"/>\n" + "    </xsl:function>\n"
                // + "\n" + "    <xsl:template match=\"text()\"/>\n" + "    \n"
                // + "    <!-- Main template for processing XML -->\n" + "    <xsl:template name=\"main\" match=\"/\">\n"
                // + "        <xsl:apply-templates select=\"$LAYOUT$\" />\n" + "    </xsl:template>\n" + "\n"
                // + "    <!-- Template for each record in your XML -->\n" + "    <xsl:template match=\"$LAYOUT$\">\n"
                // + "$FIELDS$" + "        <xsl:value-of select=\"$newline\"/>\n" + "    </xsl:template>\n" + "\n"
                // + "</xsl:stylesheet>",

                // """
                // <?xml version="1.0" encoding="utf-8"?>
                // <xsl:stylesheet version="3.0"
                //     xmlns:xs="http://www.w3.org/2001/XMLSchema"
                //     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                // xmlns:str="http://exslt.org/strings"
                //                 xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                //                 xmlns:json="http://www.w3.org/2005/xpath-functions"
                //     xmlns:my="http://example.com/my-functions"
                //     exclude-result-prefixes="xs json math my str">

                //     <xsl:param name="text-input" as="xs:string" />
                //     <xsl:output method="xml" indent="yes"/>
                // <xsl:function name="my:reverse_fixed_size_amount">
                //         <xsl:param name="sValue"/>
                //         <xsl:param name="nDecimal"/>
                //         <xsl:variable name="numericValue" select="number($sValue)"/>
                //         <xsl:choose>
                //             <xsl:when test="string($sValue) != ''">
                //                 <xsl:variable name="decimalFactor" select="math:exp10($nDecimal)"/>
                //                 <xsl:variable name="amount" select="$numericValue "/>
                //                 <xsl:choose>
                //                     <xsl:when test="$nDecimal = 0">
                //                         <xsl:sequence
                //                                 select="format-number($amount, '####')"/>
                //                     </xsl:when>
                //                     <xsl:otherwise>
                //                         <xsl:sequence
                //                                 select="format-number($amount div $decimalFactor, concat('####.', substring('0000000000', 1, $nDecimal)))"/>
                //                     </xsl:otherwise>
                //                 </xsl:choose>
                //             </xsl:when>
                //             <xsl:otherwise>
                //                 <xsl:sequence select="0"/>
                //             </xsl:otherwise>
                //         </xsl:choose>
                //     </xsl:function>
                //     <xsl:template name="main" match="/">
                //         <$LAYOUT1$>
                //             <xsl:call-template name="parse-fixed-width">
                //                 <xsl:with-param name="data" select="replace(unparsed-text($text-input), '\\r\\n', '&#10;')"/>
                //             </xsl:call-template>
                //         </$LAYOUT1$>
                //     </xsl:template>

                //     <xsl:template name="parse-fixed-width">
                //         <xsl:param name="data"/>
                //         <xsl:variable name="linebreak" select="'&#10;'"/>
                //         <xsl:variable name="records" select="tokenize($data, $linebreak)"/>

                //         <xsl:for-each select="$records">
                //             <xsl:if test="normalize-space(.) ne ''">
                //                 <xsl:call-template name="process-record">
                //                     <xsl:with-param name="record" select="."/>
                //                 </xsl:call-template>
                //             </xsl:if>
                //         </xsl:for-each>
                //     </xsl:template>

                //     <xsl:template name="process-record">
                //         <xsl:param name="record"/>
                // $FIELDS$\s
                //     </xsl:template>

                // </xsl:stylesheet>
                // """,

                // """
                // <?xml version="1.0" encoding="utf-8"?>
                // <xsl:stylesheet version="3.0"
                //     xmlns:xs="http://www.w3.org/2001/XMLSchema"
                //     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                //   xmlns:str="http://exslt.org/strings"
                //                 xmlns:math="http://www.w3.org/2005/xpath-functions/math"
                //                 xmlns:json="http://www.w3.org/2005/xpath-functions"
                //     xmlns:my="http://example.com/my-functions"
                //     exclude-result-prefixes="xs json math my str">
                // <xsl:function name="my:reverse_fixed_size_amount">
                //         <xsl:param name="sValue"/>
                //         <xsl:param name="nDecimal"/>
                //         <xsl:variable name="numericValue" select="number($sValue)"/>
                //         <xsl:choose>
                //             <xsl:when test="string($sValue) != ''">
                //                 <xsl:variable name="decimalFactor" select="math:exp10($nDecimal)"/>
                //                 <xsl:variable name="amount" select="$numericValue"/>
                //                 <xsl:choose>
                //                     <xsl:when test="$nDecimal = 0">
                //                         <xsl:sequence
                //                                 select="format-number($amount, '####')"/>
                //                     </xsl:when>
                //                     <xsl:otherwise>
                //                         <xsl:sequence
                //                                 select="format-number($amount div $decimalFactor, concat('####.', substring('0000000000', 1, $nDecimal)))"/>
                //                     </xsl:otherwise>
                //                 </xsl:choose>
                //             </xsl:when>
                //             <xsl:otherwise>
                //                 <xsl:sequence select="0"/>
                //             </xsl:otherwise>
                //         </xsl:choose>
                //     </xsl:function>
                //     <xsl:param name="text-input" as="xs:string" />
                //     <xsl:param name="delimiter" as="xs:string" select="','"/>
                //     <xsl:output method="text"/>

                //     <xsl:template name="main" match="/">
                // $HEADER$\s
                //         <xsl:call-template name="parse-fixed-width">
                //             <xsl:with-param name="data" select="replace(unparsed-text($text-input), '\\r\\n', '&#10;')"/>
                //         </xsl:call-template>
                //     </xsl:template>

                //     <xsl:template name="parse-fixed-width">
                //         <xsl:param name="data"/>
                //         <xsl:variable name="linebreak" select="'&#10;'"/>
                //         <xsl:variable name="records" select="tokenize($data, $linebreak)"/>

                //         <xsl:for-each select="$records">
                //             <xsl:if test="normalize-space(.) ne ''">
                //                 <xsl:call-template name="process-record">
                //                     <xsl:with-param name="record" select="."/>
                //                 </xsl:call-template>
                //             </xsl:if>
                //         </xsl:for-each>
                //     </xsl:template>

                //     <xsl:template name="process-record">
                //         <xsl:param name="record"/>
                // $FIELDS$<xsl:text>&#10;</xsl:text>
                // </xsl:template>

                // </xsl:stylesheet>
                // """,

                // "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                // + "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n"
                // + "    <xsl:output method=\"xml\" indent=\"yes\"/>\n" + "\n"
                // + "    <!-- Customize the delimiter as needed -->\n"
                // + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                // + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n" + "\n"
                // + "    <xsl:template name=\"main\" match=\"/\">\n" + "            <!-- Process each CSV record -->\n"
                // + "<$LAYOUT1$>\n" + "            <xsl:call-template name=\"parse-csv\">\n"
                // + "                <xsl:with-param name=\"data\" select=\"replace(unparsed-text($text-input), '\\r\\n', '&#10;')\"/>\n"
                // + "            </xsl:call-template>       \n" + "</$LAYOUT1$>\n" + "    </xsl:template>\n" + "\n"
                // + "    <xsl:template name=\"parse-csv\">\n" + "        <xsl:param name=\"data\"/>\n"
                // + "        <xsl:variable name=\"lines\" select=\"tokenize($data, '&#10;')\" as=\"xs:string*\"/>\n"
                // + "        \n" + "        <!-- Skip header row -->\n"
                // + "        <xsl:variable name=\"records\" select=\"$lines$HEADER$\"/>\n" + "\n"
                // + "        <!-- Process each CSV record -->\n" + "        <xsl:for-each select=\"$records\">\n" 
                // + "                <!-- Split the record using the specified delimiter -->\n"
                // + "                <xsl:variable name=\"fields\" select=\"tokenize(., $delimiter)\" as=\"xs:string*\"/>\n"
                // + "\n" + "                <!-- Extract data and create XML elements -->\n" + "$FIELDS$ \n" 
                // + "        </xsl:for-each>\n"
                // + "    </xsl:template>\n"
                // + "</xsl:stylesheet>\n",

                // "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                // + "<xsl:stylesheet version=\"3.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" exclude-result-prefixes=\"xs\">\n"
                // + "    <xsl:output method=\"text\"/>\n"
                // + "    \n"
                // + "    <!-- Customize the delimiter as needed -->\n"
                // + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n"
                // + "\n"
                // + "    <xsl:template match=\"/\">\n"
                // + "        <!-- Output header row -->\n"
                // + "$HEADER$\n"
                // + "        <!-- Process each record -->\n"
                // + "        <xsl:apply-templates select=\"$LAYOUT$\"/>\n"
                // + "    </xsl:template>\n"
                // + "\n"
                // + "    <xsl:template match=\"$LAYOUT2$\">\n"
                // + "$FIELDS$"
                // + "    </xsl:template>\n"
                // + "</xsl:stylesheet>\n",

                // "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                // + "<xsl:stylesheet version=\"3.0\"\n"
                // + "    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n"
                // + "    xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\n"
                // + "    xmlns:str=\"http://exslt.org/strings\"\n"
                // + "    xmlns:math=\"http://www.w3.org/2005/xpath-functions/math\"\n"
                // + "    xmlns:json=\"http://www.w3.org/2005/xpath-functions\"\n"
                // + "    xmlns:my=\"http://example.com/my-functions\"\n"
                // + "    extension-element-prefixes=\"str\"\n"
                // + "    exclude-result-prefixes=\"xs\">\n"
                // + "\n"
                // + "    <!-- Customize the delimiter as needed -->\n"
                // + "    <xsl:param name=\"text-input\" as=\"xs:string\" />\n"
                // + "    <xsl:param name=\"delimiter\" as=\"xs:string\" select=\"','\"/>\n"
                // + "\n"
                // + "        <xsl:output method=\"text\" indent=\"no\" omit-xml-declaration=\"yes\" />\n"
                // + "    <xsl:strip-space elements=\"*\" />\n"
                // + "    <xsl:variable name=\"newline\">\n"
                // + "        <xsl:text>&#10;</xsl:text>\n"
                // + "    </xsl:variable>\n"
                // + "    <xsl:variable name=\"some_spaces\" select=\"'                                                                                                                                                      '\" />\n"
                // + "    <xsl:variable name=\"some_zeroes\" select=\"'000000000000000000000000000000000000000000000000000000000000'\" />\n"
                // + "\n"
                // + "    <xsl:function name=\"my:fixed_size\">\n"
                // + "        <xsl:param name=\"sValue\"/>\n"
                // + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:sequence select=\"substring(concat($sValue, $some_spaces), 1, $iWidth)\"/>\n"
                // + "    </xsl:function>\n"
                // + "\n"
                // + "    <xsl:function name=\"my:fixed_size_amount\">\n"
                // + "        <xsl:param name=\"sValue\"/>\n"
                // + "        <xsl:param name=\"nDecimal\"/>\n"
                // + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:variable name=\"amount\" select=\"number($sValue) * math:exp10($nDecimal)\"/>\n"
                // + "        <xsl:choose>\n"
                // + "            <xsl:when test=\"$nDecimal = 0 and string($amount) != 'NaN'\">\n"
                // + "                <xsl:sequence\n"
                // + "                    select=\"concat(substring($some_zeroes, 1, $iWidth -  string-length(string($sValue))), string($sValue))\"/>\n"
                // + "            </xsl:when>\n"
                // + "            <xsl:when test=\"string($amount) != 'NaN'\">\n"
                // + "                <xsl:sequence\n"
                // + "                    select=\"format-number($amount, concat(substring($some_zeroes, 1, $iWidth), ';-', substring($some_zeroes, 1, $iWidth - 1)))\"/>\n"
                // + "            </xsl:when>\n"
                // + "            <xsl:otherwise>\n"
                // + "                <xsl:sequence select=\"substring($some_zeroes, 1, $iWidth)\"/>\n"
                // + "            </xsl:otherwise>\n"
                // + "        </xsl:choose>\n"
                // + "    </xsl:function>\n"
                // + "\n"
                // + "    <xsl:function name=\"my:filler\">\n"
                // + "        <xsl:param name=\"iWidth\"/>\n"
                // + "        <xsl:sequence select=\"substring($some_spaces, 1, $iWidth)\"/>\n"
                // + "    </xsl:function>\n"
                // + "\n"
                // + "    <xsl:template match=\"text()\"/>\n"
                // + "\n"
                // + "    <xsl:template name=\"main\" match=\"/\">\n"
                // + "            <!-- Process each CSV record -->\n"
                // + "            <xsl:call-template name=\"parse-csv\">\n"
                // + "                <xsl:with-param name=\"data\" select=\"replace(unparsed-text($text-input), '\\r\\n', '&#10;')\"/>\n"
                // + "            </xsl:call-template>       \n"
                // + "    </xsl:template>\n"
                // + "\n"
                // + "    <xsl:template name=\"parse-csv\">\n"
                // + "        <xsl:param name=\"data\"/>\n"
                // + "        <xsl:variable name=\"lines\" select=\"tokenize($data, '&#10;')\" as=\"xs:string*\"/>\n"
                // + "        \n"
                // + "        <!-- Skip header row -->\n"
                // + "        <xsl:variable name=\"records\" select=\"$lines$HEADER$\"/>\n"
                // + "\n"
                // + "        <!-- Process each CSV record -->\n"
                // + "        <xsl:for-each select=\"$records\">            \n"
                // + "            <xsl:variable name=\"fields\" select=\"tokenize(., $delimiter)\" as=\"xs:string*\"/>\n"
                // + "$FIELDS$"
                // + "            <xsl:value-of select=\"$newline\"/>            \n"
                // + "        </xsl:for-each>\n"
                // + "    </xsl:template>\n"
                // + "</xsl:stylesheet>\n"
         
        };
        
        String[] originals = {
            """
             <?xml version='1.0' encoding='utf-8'?>\n" + "<xsl:stylesheet version=\"3.0\"\n"
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
                + "</xsl:stylesheet>""",

                """
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
                """,

                """
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
                """,

"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
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
                + "</xsl:stylesheet>\n",

                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
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
                + "</xsl:stylesheet>\n",

                 "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
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
                + "</xsl:stylesheet>\n"
         
        };

        String[] originals3 = {
            // "/assets/",
            // "/login",
            // "/logout",
            // "/licence",
            // "/error",
            // "/static/",
            // "/favicon.ico",
                
            // "/licence?error=no_licence",
            // "/licence?error=invalid_hostname",
            // "/licence?error=expired",
            // "/licence?error=module_not_authorized",
            // ".*/(users|download|upload|structurs|complex-structures/.+)?$"
            // ".*(/|/users|/download|/upload|/structurs|/complex-structures/main|/complex-structures/sub).*"
            // "A0x43x32x49$cwBJAQ=="
        };

        
        String[] originals4 = {
            // "Usage: -l <cleLicence>",
            // "Usage: -c <config> <inputFile> <outputFile> <delimiteur>",
            // "Usage: -c <config> <inputFile> <outputFile> <delimiteur>",
            // // "Option inconnue : ",
            // " Utiliation :",
            // "  -d                                         -> demarre l application web",
            // "  -l <cleLicence>                            -> enregistre une nouvelle licence",
            // "  -c <config> <input> <output> <delim>       -> execute une conversion",
            // "  -h                                         -> Aide a l application web",
            // "== Verification de la licence ==",
            // "[INFO] Licence existante",
            // "Erreur : licence invalide ou corrompue",
            // "Erreur : licence non valide pour ce serveur",
            // "Attendu : ",
            // "Trouvé  : ",
            // "Erreur : licence expirée depuis le ",
            // "Erreur : Licence sans Module activer! ",
            // "[SUCCESS] Licence OK"
            // "Expiration : "

            // "Aucune licence trouvée.",
            // "Clé licence invalide.",
            // "Hostname non valide pour cette licence.",
            // "Licence expirée.",
 
            // "Licence absente",
            // "Licence invalide.",
            // "Hostname invalide.",
            // "Licence expirée.",
            // "Licence active mais aucun module activé.",
            // "Format d'entrée incorrect : ",
            // "Impossible de séparer 'from' et 'into' dans : ",
            "[SUCCESS] Conversion reussie.",
            "[FAILED] Echec de la conversion."
            
                // "La conversion fichier en cours avec la configuration=" ,
                // ", fichier d'entree=" ,
                // ", fichier de sortie=" ,
                // ", format source=" ,
                // ", format cible=" ,
                // ", delimiteur=",
                // "/licence?error=no_licence",
                // "/licence?error=module_not_authorized",
                // "/licence?error=invalid_key"
        };

        for (String original : originals) {
            String encrypted = encrypt(original);
            System.out.println("\"" + encrypted + "\",");
            ox72.add(encrypted);
        }

        int i = 0;
        for (String e : ox72) {
            String decrypted = decrypt_(e);
            System.out.println("[" + i + "] " + e + " : " + decrypted);
            i++;
        }

    }
}
