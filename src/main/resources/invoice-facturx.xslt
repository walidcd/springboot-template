<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
                xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
                xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
                xmlns:f="urn:local-func"
                exclude-result-prefixes="rsm ram udt f">

    <xsl:output method="xml" indent="yes"/>
    <xsl:strip-space elements="*"/>

    <xsl:variable name="clr-navy"   select="'#002060'"/>
    <xsl:variable name="clr-steel"  select="'#dbe6f5'"/>
    <xsl:variable name="clr-pastel" select="'#c5d9f1'"/>
    <xsl:variable name="clr-red"    select="'#ff3300'"/>
    <xsl:variable name="base-font"  select="'Calibri, Helvetica, Arial, sans-serif'"/>

    <xsl:function name="f:fmt-date">
        <xsl:param name="raw"/>
        <xsl:sequence select="
      if (string-length($raw)=8)
         then concat(substring($raw,7,2),'/',substring($raw,5,2),'/',substring($raw,1,4))
         else $raw"/>
    </xsl:function>
    <xsl:variable name="hdrPeriod"
                  select="/rsm:CrossIndustryInvoice
                      /rsm:SupplyChainTradeTransaction
                      /ram:ApplicableHeaderTradeSettlement
                      /ram:BillingSpecifiedPeriod"/>
    <xsl:template match="/">
        <fo:root font-family="{$base-font}" font-size="9pt" line-height="13pt">
            <fo:layout-master-set>
                <fo:simple-page-master master-name="A4"
                                       page-height="297mm" page-width="210mm"
                                       margin-top="12mm" margin-bottom="15mm"
                                       margin-left="12mm" margin-right="12mm">
                    <fo:region-body  margin-top="85mm"/>
                    <fo:region-before extent="85mm"/>
                    <fo:region-after extent="10mm"/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">

                <fo:static-content flow-name="xsl-region-before">
                    <fo:table table-layout="fixed" width="100%">
                        <fo:table-column column-width="60%"/>
                        <fo:table-column column-width="40%"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block>
                                        <fo:external-graphic src="url('logo.png')" content-width="125mm" height="12mm"/>
                                        <xsl:call-template name="seller-info"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block border="0.5pt solid {$clr-navy}" padding="3pt" font-weight="bold" font-size="10pt">
                                        <fo:block>
                                            <fo:inline color="{$clr-navy}">FACTURE N° :</fo:inline>
                                            <fo:inline color="{$clr-red}">
                                                <xsl:value-of select="rsm:ExchangedDocument/ram:ID"/>
                                            </fo:inline>
                                        </fo:block>
                                        <fo:block>
                                            <fo:inline color="{$clr-navy}">Date :</fo:inline>
                                            <xsl:value-of select="f:fmt-date(rsm:ExchangedDocument/ram:IssueDateTime/udt:DateTimeString)"/>
                                        </fo:block>
                                    </fo:block>
                                    <fo:block text-align="right" margin-top="2pt">
                                        <fo:external-graphic src="url('fx-badge.png')" content-width="14mm"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>
                    <xsl:call-template name="top-reference-blocks"/>
                </fo:static-content>

                <fo:static-content flow-name="xsl-region-after">
                    <fo:block text-align="center" font-size="8pt" color="gray">
                        <xsl:value-of select="//ram:IncludedNote[ram:SubjectCode='REG']/ram:Content"/>
                        <fo:leader/>
                        Page <fo:page-number/>
                    </fo:block>
                </fo:static-content>

                <fo:flow flow-name="xsl-region-body">
                    <fo:block space-after="4pt">
                        DATE DE DÉBUT DE PRESTATION :
                        <xsl:value-of select="f:fmt-date($hdrPeriod/ram:StartDateTime/udt:DateTimeString)"/>
                    </fo:block>
                    <fo:block space-after="6pt">
                        DATE DE FIN DE PRESTATION :
                        <xsl:value-of select="f:fmt-date($hdrPeriod/ram:EndDateTime/udt:DateTimeString)"/>
                    </fo:block>

                    <xsl:call-template name="lines-table"/>
                    <xsl:call-template name="header-allowances"/>
                    <xsl:call-template name="tax-summary"/>
                    <xsl:call-template name="totals-bar"/>
                    <xsl:call-template name="payment-block"/>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template name="seller-info">
        <xsl:variable name="s" select="//ram:SellerTradeParty"/>
        <fo:block font-weight="bold"><xsl:value-of select="$s/ram:Name"/></fo:block>
        <fo:block><xsl:value-of select="$s/ram:SpecifiedLegalOrganization/ram:TradingBusinessName"/></fo:block>
        <fo:block>
            <xsl:value-of select="normalize-space($s/ram:PostalTradeAddress/ram:LineOne)"/>,
            <xsl:value-of select="$s/ram:PostalTradeAddress/ram:CityName"/>
        </fo:block>
        <fo:block>SIREN : <xsl:value-of select="$s/ram:SpecifiedLegalOrganization/ram:ID"/></fo:block>
        <fo:block>N° TVA : <xsl:value-of select="$s/ram:SpecifiedTaxRegistration/ram:ID"/></fo:block>
    </xsl:template>

    <xsl:template name="sub-block">
        <xsl:param name="title"/>
        <xsl:param name="content"/>
        <fo:block border="0.5pt solid {$clr-navy}" padding="3pt" margin-bottom="3pt">
            <fo:block background-color="{$clr-navy}" color="white" font-weight="bold" padding="1pt">
                <xsl:value-of select="$title"/>
            </fo:block>
            <xsl:copy-of select="$content"/>
        </fo:block>
    </xsl:template>

    <xsl:template name="top-reference-blocks">
        <fo:table table-layout="fixed" width="100%" font-size="9pt" space-before="4pt">
            <fo:table-column column-width="60%"/>
            <fo:table-column column-width="40%"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <xsl:call-template name="sub-block">
                            <xsl:with-param name="title" select="'Vos références'"/>
                            <xsl:with-param name="content">
                                <xsl:for-each select="//ram:BuyerReference">
                                    <fo:block><xsl:value-of select="."/></fo:block>
                                </xsl:for-each>
                            </xsl:with-param>
                        </xsl:call-template>
                    </fo:table-cell>
                    <fo:table-cell>
                        <xsl:variable name="b" select="//ram:BuyerTradeParty"/>
                        <xsl:call-template name="sub-block">
                            <xsl:with-param name="title" select="'Adresse du Client'"/>
                            <xsl:with-param name="content">
                                <fo:block font-weight="bold"><xsl:value-of select="$b/ram:Name"/></fo:block>
                                <fo:block><xsl:value-of select="$b/ram:PostalTradeAddress/ram:LineOne"/></fo:block>
                                <fo:block><xsl:value-of select="$b/ram:PostalTradeAddress/ram:CityName"/></fo:block>
                                <fo:block><xsl:value-of select="$b/ram:PostalTradeAddress/ram:CountryID"/></fo:block>
                            </xsl:with-param>
                        </xsl:call-template>
                    </fo:table-cell>
                </fo:table-row>
                <fo:table-row>
                    <fo:table-cell>
                        <xsl:call-template name="sub-block">
                            <xsl:with-param name="title" select="'Références sur la facture'"/>
                            <xsl:with-param name="content">
                                <fo:block>CADRE DE FACTURATION : S1</fo:block>
                            </xsl:with-param>
                        </xsl:call-template>
                    </fo:table-cell>
                    <fo:table-cell background-color="{$clr-steel}">
                        <xsl:variable name="b" select="//ram:BuyerTradeParty"/>
                        <xsl:call-template name="sub-block">
                            <xsl:with-param name="title" select="'Vos identifiants'"/>
                            <xsl:with-param name="content">
                                <fo:block>FR-SIRET : <xsl:value-of select="$b/ram:GlobalID[@schemeID='0009']"/></fo:block>
                                <fo:block>SIREN : <xsl:value-of select="$b/ram:SpecifiedLegalOrganization/ram:ID"/></fo:block>
                                <fo:block>N° TVA : <xsl:value-of select="$b/ram:SpecifiedTaxRegistration/ram:ID"/></fo:block>
                            </xsl:with-param>
                        </xsl:call-template>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="lines-table">
        <fo:table table-layout="auto" width="100%" margin-top="6pt">
            <fo:table-column column-width="10mm"/>
            <fo:table-column column-width="20mm"/>
            <fo:table-column column-width="proportional-column-width(4)"/>
            <fo:table-column column-width="20mm"/>
            <fo:table-column column-width="15mm"/>
            <fo:table-column column-width="15mm"/>
            <fo:table-column column-width="25mm"/>
            <fo:table-column column-width="25mm"/>
            <fo:table-column column-width="15mm"/>
            <fo:table-header background-color="{$clr-navy}" color="white" font-weight="bold" font-size="8.5pt">
                <fo:table-row>
                    <fo:table-cell><fo:block>#</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block># BC</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>NOM &amp; DESIGNATION</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>P.U. HT</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>UNITÉ</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>Qté</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>Remise/Charge</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>TOTAL HT</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>TVA %</fo:block></fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body background-color="{$clr-steel}">
                <xsl:for-each select="//ram:IncludedSupplyChainTradeLineItem">
                    <fo:table-row keep-together.within-page="always">
                        <fo:table-cell><fo:block><xsl:value-of select="ram:AssociatedDocumentLineDocument/ram:LineID"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block><xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:BuyerOrderReferencedDocument/ram:LineID"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block wrap-option="wrap"><xsl:value-of select="ram:SpecifiedTradeProduct/ram:Name"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number(ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:ChargeAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="center"><fo:block><xsl:value-of select="ram:SpecifiedLineTradeAgreement/ram:NetPriceProductTradePrice/ram:BasisQuantity/@unitCode"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="center"><fo:block><xsl:value-of select="format-number(ram:SpecifiedLineTradeDelivery/ram:BilledQuantity,'#,##0')"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="right"><fo:block>
                            <xsl:variable name="ac" select="ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge"/>
                            <xsl:value-of select="format-number(sum($ac[ram:ChargeIndicator/udt:Indicator='false']/ram:ActualAmount) - sum($ac[ram:ChargeIndicator/udt:Indicator='true']/ram:ActualAmount),'#,##0.00')"/>
                        </fo:block></fo:table-cell>
                        <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number(ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="center"><fo:block><xsl:value-of select="ram:SpecifiedLineTradeSettlement/ram:ApplicableTradeTax/ram:RateApplicablePercent"/> %</fo:block></fo:table-cell>
                    </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="header-allowances">
        <xsl:variable name="ac" select="//ram:SpecifiedTradeAllowanceCharge"/>
        <xsl:if test="$ac">
            <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-top="6pt">
                <fo:table-column column-width="80%"/>
                <fo:table-column column-width="20%"/>
                <fo:table-body>
                    <xsl:for-each select="$ac">
                        <fo:table-row>
                            <fo:table-cell>
                                <fo:block>
                                    <xsl:value-of select="ram:ReasonCode"/> <xsl:value-of select="ram:Reason"/> :
                                    <xsl:value-of select="format-number(ram:CalculationPercent,'#,##0.00')"/> % sur
                                    <xsl:value-of select="format-number(ram:BasisAmount,'#,##0.00')"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell text-align="right" color="{if (ram:ChargeIndicator/udt:Indicator='false') then $clr-red else 'black'}">
                                <fo:block><xsl:value-of select="format-number(ram:ActualAmount,'#,##0.00')"/></fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </xsl:for-each>
                </fo:table-body>
            </fo:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="tax-summary">
        <fo:table table-layout="fixed" width="100%" margin-top="6pt">
            <fo:table-column column-width="25%"/>
            <fo:table-column column-width="25%"/>
            <fo:table-column column-width="25%"/>
            <fo:table-column column-width="25%"/>
            <fo:table-header background-color="{$clr-navy}" color="white" font-weight="bold">
                <fo:table-row>
                    <fo:table-cell><fo:block>Code</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>Taux</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>Base TVA</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>Montant TVA</fo:block></fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <xsl:for-each select="//ram:ApplicableTradeTax">
                    <fo:table-row>
                        <fo:table-cell><fo:block><xsl:value-of select="ram:CategoryCode"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block><xsl:value-of select="format-number(ram:RateApplicablePercent,'#,##0.00')"/> %</fo:block></fo:table-cell>
                        <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number(ram:BasisAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                        <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number(ram:CalculatedAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                    </fo:table-row>
                </xsl:for-each>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="totals-bar">
        <xsl:variable name="s" select="//ram:SpecifiedTradeSettlementHeaderMonetarySummation"/>
        <fo:table table-layout="fixed" width="100%" margin-top="6pt">
            <fo:table-column column-width="33%"/>
            <fo:table-column column-width="33%"/>
            <fo:table-column column-width="34%"/>
            <fo:table-header background-color="{$clr-pastel}" font-weight="bold">
                <fo:table-row>
                    <fo:table-cell><fo:block>TOTAL HT</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>TOTAL TVA</fo:block></fo:table-cell>
                    <fo:table-cell><fo:block>TOTAL TTC</fo:block></fo:table-cell>
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number($s/ram:TaxBasisTotalAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                    <fo:table-cell text-align="right"><fo:block><xsl:value-of select="format-number($s/ram:TaxTotalAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                    <fo:table-cell text-align="right" font-weight="bold"><fo:block><xsl:value-of select="format-number($s/ram:GrandTotalAmount,'#,##0.00')"/></fo:block></fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template name="payment-block">
        <xsl:variable name="due" select="//ram:SpecifiedTradePaymentTerms/ram:DueDateDateTime/udt:DateTimeString"/>
        <xsl:variable name="s"   select="//ram:SpecifiedTradeSettlementHeaderMonetarySummation"/>
        <xsl:variable name="pm"  select="//ram:SpecifiedTradeSettlementPaymentMeans"/>
        <xsl:variable name="payee" select="//ram:PayeeTradeParty"/>

        <fo:table table-layout="fixed" width="100%" font-size="9pt" margin-top="8pt">
            <fo:table-column column-width="50%"/>
            <fo:table-column column-width="50%"/>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell border="0.5pt solid {$clr-navy}" padding="3pt">
                        <fo:block font-weight="bold">Date d'échéance :</fo:block>
                        <fo:block><xsl:value-of select="f:fmt-date($due)"/></fo:block>
                    </fo:table-cell>
                    <fo:table-cell border="0.5pt solid {$clr-navy}" padding="3pt">
                        <fo:block font-weight="bold">NET A PAYER</fo:block>
                        <fo:block font-weight="bold"><xsl:value-of select="format-number($s/ram:DuePayableAmount,'#,##0.00')"/> <xsl:value-of select="//ram:InvoiceCurrencyCode"/></fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>

        <fo:block space-before="4pt">Bénéficiaire – NOM : <xsl:value-of select="$payee/ram:Name"/></fo:block>
        <fo:block>MOYEN DE PAIEMENT : <xsl:value-of select="$pm/ram:Information"/></fo:block>
        <fo:block>IBAN : <xsl:value-of select="$pm/ram:PayeePartyCreditorFinancialAccount/ram:IBANID"/> – BIC : <xsl:value-of select="$pm/ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID"/></fo:block>
        <fo:block>REFERENCE AVIS DE PAIEMENT : <xsl:value-of select="//ram:PaymentReference"/></fo:block>
    </xsl:template>

</xsl:stylesheet>
