package com.aliagri.demo.service.report;

import com.aliagri.demo.model.Purchase;
import com.aliagri.demo.model.PurchaseItem;
import com.aliagri.demo.model.Sale;
import com.aliagri.demo.model.SaleItem;
import com.aliagri.demo.repository.PurchaseRepository;
import com.aliagri.demo.repository.SaleRepository;
import com.openhtmltopdf.bidi.support.ICUBidiReorderer;
import com.openhtmltopdf.bidi.support.ICUBidiSplitter;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.TextDirection;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service("pdfExporter")
@RequiredArgsConstructor
public class RegistrePdfExporter implements RegistreExporter {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());

    /** The table is padded with blank rows up to this many rows. */
    private static final int MIN_ROWS = 8;

    /** Blank rows always left at the end of the table for handwritten entries. */
    private static final int TRAILING_BLANK_ROWS = 2;

    @Override
    public ByteArrayInputStream generate() throws IOException {

        String html = buildHtml();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.useFastMode();

        // Arabic RTL and character shaping support
        builder.useUnicodeBidiSplitter(new ICUBidiSplitter.ICUBidiSplitterFactory());
        builder.useUnicodeBidiReorderer(new ICUBidiReorderer());

        // Keep the document LTR; Arabic elements have their own RTL direction.
        builder.defaultTextDirection(TextDirection.LTR);

        // Same family, two weights: regular (400) and bold (700)
        registerFont(builder, "/fonts/NotoSansArabic-Regular.ttf", "NotoSansArabic", 400);
        registerFont(builder, "/fonts/NotoSansArabic-Bold.ttf", "NotoSansArabic", 700);

        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void registerFont(PdfRendererBuilder builder, String path, String familyName, int weight)
            throws IOException {

        if (getClass().getResource(path) == null) {
            throw new IOException("Font not found on classpath: " + path
                    + " (expected in src/main/resources" + path + ")");
        }

        builder.useFont(
                () -> getClass().getResourceAsStream(path),
                familyName,
                weight,
                FontStyle.NORMAL,
                true);
    }

    private String buildHtml() {

        List<Sale> sales = new ArrayList<>(saleRepository.findAllWithDetails());
        List<Purchase> purchases = new ArrayList<>(purchaseRepository.findAllWithDetails());

        // Oldest first
        sales.sort(Comparator.comparing(Sale::getSaleDate, Comparator.nullsLast(Comparator.naturalOrder())));
        purchases.sort(Comparator.comparing(Purchase::getPurchaseDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        // Flat, independent lists: row i = sale i + purchase i
        List<SaleItem> saleItems = new ArrayList<>();
        for (Sale sale : sales) {
            saleItems.addAll(sale.getItems());
        }

        List<PurchaseItem> purchaseItems = new ArrayList<>();
        for (Purchase purchase : purchases) {
            purchaseItems.addAll(purchase.getItems());
        }

        StringBuilder html = new StringBuilder();

        html.append(
                """
                        <!DOCTYPE html>
                        <html lang="fr" xmlns="http://www.w3.org/1999/xhtml">
                        <head>
                            <meta charset="UTF-8"/>
                            <title>Registre</title>

                            <style>

                                /* Bottom margin leaves room for the observations panel on every page */
                                @page {
                                    size: A4 landscape;
                                    margin: 10mm 10mm 36mm 10mm;

                                    @bottom-center {
                                        content: element(observations);
                                        vertical-align: top;
                                        padding-top: 3mm;
                                    }
                                }

                                * {
                                    box-sizing: border-box;
                                }

                                body {
                                    font-family: Helvetica, 'NotoSansArabic', sans-serif;
                                    font-size: 10px;
                                    color: #000000;
                                    margin: 0;
                                    padding: 0;
                                }

                                /* Running element: repeated at the bottom of every page */
                                #observations {
                                    position: running(observations);
                                    width: 277mm;
                                }

                                /* TITLE (one line: French + Arabic) */

                                .document-title {
                                    text-align: center;
                                    font-size: 13px;
                                    font-weight: bold;
                                    margin: 2mm 0 4mm 0;
                                }

                                .title-fr {
                                    text-decoration: underline;
                                }

                                .title-ar {
                                    font-family: 'NotoSansArabic', sans-serif;
                                    direction: rtl;
                                    unicode-bidi: embed;
                                    text-decoration: underline;
                                }

                                /* TABLES (shared) */

                                .register-table,
                                .bottom-panel {
                                    width: 100%;
                                    table-layout: fixed;
                                    border-collapse: collapse;
                                    direction: ltr;
                                }

                                .register-table th,
                                .register-table td,
                                .bottom-panel th,
                                .bottom-panel td {
                                    border: 0.75px solid #000000;
                                    text-align: center;
                                    vertical-align: middle;
                                    overflow-wrap: break-word;
                                }

                                /* No bold in header cells */
                                th {
                                    font-weight: normal;
                                    background-color: #D9E1F2;
                                    color: #000000;
                                }

                                /* MAIN REGISTER */

                                .group-heading th {
                                    font-size: 9px;
                                    padding: 3px;
                                    height: 6mm;
                                }

                                .column-heading th {
                                    font-family: 'NotoSansArabic', sans-serif;
                                    font-size: 9px;
                                    line-height: 1.4;
                                    padding: 3px;
                                    height: 11mm;
                                }

                                .arabic-heading {
                                    direction: rtl;
                                    unicode-bidi: embed;
                                    text-align: center;
                                }

                                .register-table td {
                                    font-size: 9px;
                                    padding: 1px 2px;
                                    height: 8mm;
                                    background-color: #FFFFFF;
                                }

                                .register-table td.small {
                                    font-size: 8px;
                                }

                                .register-table tbody tr {
                                    page-break-inside: avoid;
                                }

                                /* OBSERVATIONS PANEL */

                                .bottom-panel th {
                                    font-size: 9px;
                                    padding: 3px;
                                    height: 6.5mm;
                                }

                                .bottom-panel th.visit {
                                    font-weight: bold;
                                }

                                .bottom-panel td {
                                    height: 14mm;
                                }

                            </style>
                        </head>

                        <body>

                        <!-- OBSERVATIONS PANEL (running element, printed on every page) -->
                        <div id="observations">
                            <table class="bottom-panel">
                                <colgroup>
                                    <col style="width: 9%"/>
                                    <col style="width: 32%"/>
                                    <col style="width: 59%"/>
                                </colgroup>
                                <tbody>
                                    <tr>
                                        <th colspan="3" class="visit">
                                            Visite des agents habilités de l'ONSSA - زيارة الأعوان المؤهلين من قبل المكتب الوطني للسلامة الصحية للمنتجات الغذائية
                                        </th>
                                    </tr>
                                    <tr>
                                        <th>Date - التاريخ</th>
                                        <th>Identité de l'agent habilité - هوية العون المؤهل</th>
                                        <th>Observations/recommandations - الملاحظات / التوصيات</th>
                                    </tr>
                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <div class="document-title">
                            <span class="title-fr">Registre des Achats/Ventes des pesticides à usage agricole</span>
                            <span>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;</span>
                            <span class="title-ar" dir="rtl">سجل شراء و بيع مبيدات الآفات الزراعية</span>
                        </div>

                        <table class="register-table">

                            <colgroup>
                                <col style="width: 8.7%"/>
                                <col style="width: 14.5%"/>
                                <col style="width: 7.3%"/>
                                <col style="width: 10.2%"/>
                                <col style="width: 8.6%"/>
                                <col style="width: 7.2%"/>

                                <col style="width: 8.6%"/>
                                <col style="width: 7.3%"/>
                                <col style="width: 10.2%"/>
                                <col style="width: 10.1%"/>
                                <col style="width: 7.3%"/>
                            </colgroup>

                            <thead>

                                <tr class="group-heading">
                                    <th colspan="6">Ventes – بيع</th>
                                    <th colspan="5">Achats – شراء</th>
                                </tr>

                                <tr class="column-heading">
                                    <th><div class="arabic-heading" dir="rtl">رقم بطاقة تعريف المشتري وعنوانه</div></th>
                                    <th><div class="arabic-heading" dir="rtl">اسم المشتري</div></th>
                                    <th><div class="arabic-heading" dir="rtl">الكمية (كلغ)</div></th>
                                    <th><div class="arabic-heading" dir="rtl">رقم ترخيص المبيد</div></th>
                                    <th><div class="arabic-heading" dir="rtl">اسم المبيد</div></th>
                                    <th><div class="arabic-heading" dir="rtl">تاريخ</div></th>

                                    <th><div class="arabic-heading" dir="rtl">اسم البائع</div></th>
                                    <th><div class="arabic-heading" dir="rtl">الكمية (كلغ)</div></th>
                                    <th><div class="arabic-heading" dir="rtl">رقم ترخيص المبيد</div></th>
                                    <th><div class="arabic-heading" dir="rtl">اسم المبيد</div></th>
                                    <th><div class="arabic-heading" dir="rtl">تاريخ</div></th>
                                </tr>

                            </thead>

                            <tbody>
                        """);

        int dataRows = Math.max(saleItems.size(), purchaseItems.size());

        for (int i = 0; i < dataRows; i++) {

            html.append("<tr>");

            // SALES (6 columns)
            if (i < saleItems.size()) {
                SaleItem item = saleItems.get(i);
                Sale sale = item.getSale();

                cell(html, sale.getCustomerCinSnapshot());
                cell(html, sale.getCustomerNameSnapshot());
                cell(html, quantityWithUnit(item.getQuantity(), item.getUnitSnapshot()));
                cell(html, item.getLicenseNumberSnapshot());
                cell(html, item.getProductNameSnapshot(), "small");
                cell(html, DATE_FORMATTER.format(sale.getSaleDate()));
            } else {
                emptyCells(html, 6);
            }

            // PURCHASES (5 columns)
            if (i < purchaseItems.size()) {
                PurchaseItem item = purchaseItems.get(i);
                Purchase purchase = item.getPurchase();

                cell(html, upper(purchase.getProviderNameSnapshot()), "small");
                cell(html, quantityWithUnit(item.getQuantity(), item.getUnitSnapshot()));
                cell(html, item.getLicenseNumberSnapshot());
                cell(html, item.getProductNameSnapshot(), "small");
                cell(html, DATE_FORMATTER.format(purchase.getPurchaseDate()));
            } else {
                emptyCells(html, 5);
            }

            html.append("</tr>");
        }

        // Blank rows at the end, like the paper register
        int blankRows = Math.max(TRAILING_BLANK_ROWS, MIN_ROWS - dataRows);
        for (int i = 0; i < blankRows; i++) {
            html.append("<tr>");
            emptyCells(html, 11);
            html.append("</tr>");
        }

        html.append("""
                    </tbody>
                </table>

                </body>
                </html>
                """);

        return html.toString();
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private void cell(StringBuilder html, Object value) {
        html.append("<td>").append(escape(value)).append("</td>");
    }

    private void cell(StringBuilder html, Object value, String cssClass) {
        html.append("<td class=\"").append(cssClass).append("\">")
                .append(escape(value))
                .append("</td>");
    }

    private void emptyCells(StringBuilder html, int count) {
        for (int i = 0; i < count; i++) {
            html.append("<td></td>");
        }
    }

    private String quantityWithUnit(Object quantity, String unit) {
        String q = quantity == null ? "" : String.valueOf(quantity);
        String u = unit == null ? "" : unit;
        return (q + " " + u).trim();
    }

    private String upper(String value) {
        return value == null ? "" : value.toUpperCase(Locale.ROOT);
    }

    /**
     * Escapes text for XHTML and strips characters that are illegal in XML 1.0.
     */
    private String escape(Object value) {

        if (value == null) {
            return "";
        }

        String text = String.valueOf(value)
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\uFFFE\\uFFFF]", "");

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}