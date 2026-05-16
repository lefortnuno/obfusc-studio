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
        <p>
            <xsl:for-each select="unparsed-text-lines($text-input)">
                <xsl:if test="normalize-space(.) ne ''">
                    <xsl:call-template name="process-record">
                        <xsl:with-param name="record" select="."/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </p>
    </xsl:template>

    <xsl:template name="process-record">
        <xsl:param name="record"/>
<RequestRow>
    <RowId><xsl:value-of select="normalize-space(substring($record, 1, 10))"/></RowId> 
    <Branch><xsl:value-of select="normalize-space(substring($record, 11, 5))"/></Branch> 
    <Currency><xsl:value-of select="normalize-space(substring($record, 16, 3))"/></Currency> 
    <AccountId><xsl:value-of select="normalize-space(substring($record, 50, 11))"/></AccountId> 
    <AccountSuffix><xsl:value-of select="normalize-space(substring($record, 61, 2))"/></AccountSuffix> 
    <OperationCode><xsl:value-of select="normalize-space(substring($record, 63, 3))"/></OperationCode> 
    <MouvementNumber><xsl:value-of select="normalize-space(substring($record, 66, 6))"/></MouvementNumber> 
    <User><xsl:value-of select="normalize-space(substring($record, 75, 10))"/></User> 
    <LedgerDate><xsl:value-of select="normalize-space(substring($record, 93, 8))"/></LedgerDate> 
    <ValueDate><xsl:value-of select="normalize-space(substring($record, 105, 8))"/></ValueDate> 
    <Amount><xsl:value-of select="normalize-space(substring($record, 113, 20))"/></Amount> 
    <Side><xsl:value-of select="normalize-space(substring($record, 133, 1))"/></Side> 
    <Description><xsl:value-of select="normalize-space(substring($record, 134, 30))"/></Description> 
    <RecordNumber><xsl:value-of select="normalize-space(substring($record, 165, 11))"/></RecordNumber> 
    <MatchingReference><xsl:value-of select="normalize-space(substring($record, 176, 8))"/></MatchingReference> 
    <ReconciliationAccount><xsl:value-of select="normalize-space(substring($record, 261, 11))"/></ReconciliationAccount> 
    <ReconciliationAccountSuffix><xsl:value-of select="normalize-space(substring($record, 261, 11))"/>Suffix</ReconciliationAccountSuffix> 
    <InputBranch><xsl:value-of select="normalize-space(substring($record, 286, 5))"/></InputBranch> 
    <SendingBranch><xsl:value-of select="normalize-space(substring($record, 291, 5))"/></SendingBranch> 
    <ReceivingBranch><xsl:value-of select="normalize-space(substring($record, 296, 5))"/></ReceivingBranch> 
    <MovementLang><xsl:value-of select="normalize-space(substring($record, 351, 3))"/></MovementLang> 

    <Descriptions>
             <Description>
                      <ComplementaryDescription><xsl:value-of select="normalize-space(substring($record, 437, 60))"/></ComplementaryDescription>
                      <LangDescription><xsl:value-of select="normalize-space(substring($record, 497, 3))"/></LangDescription>
             </Description>
   </Descriptions>  

    <FileReferencec><xsl:value-of select="normalize-space(substring($record, 387, 50))"/>c</FileReferencec> 

</RequestRow> 
    </xsl:template>

</xsl:stylesheet>
