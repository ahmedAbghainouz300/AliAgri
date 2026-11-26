package com.aliagri.demo.service.report;

import com.aliagri.demo.model.Purchase;
import com.aliagri.demo.model.PurchaseItem;
import com.aliagri.demo.model.Sale;
import com.aliagri.demo.model.SaleItem;
import com.aliagri.demo.repository.PurchaseRepository;
import com.aliagri.demo.repository.SaleRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("pdfExporter")
@RequiredArgsConstructor
public class RegistrePdfExporter implements RegistreExporter {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public ByteArrayInputStream generate() throws IOException {
        String html = buildHtml();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(html, null);
        builder.toStream(out);
        builder.run();

        return new ByteArrayInputStream(out.toByteArray());
    }

    private String buildHtml() {
        List<Sale> sales = saleRepository.findAllWithDetails();
        List<Purchase> purchases = purchaseRepository.findAllWithDetails();

        Map<String, List<SaleItem>> ventesByDate = new LinkedHashMap<>();
        for (Sale sale : sales) {
            String dateStr = DATE_FORMATTER.format(sale.getSaleDate());
            for (SaleItem item : sale.getItems()) {
                ventesByDate.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(item);
            }
        }

        Map<String, List<PurchaseItem>> achatsByDate = new LinkedHashMap<>();
        for (Purchase purchase : purchases) {
            String dateStr = DATE_FORMATTER.format(purchase.getPurchaseDate());
            for (PurchaseItem item : purchase.getItems()) {
                achatsByDate.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(item);
            }
        }

        Set<String> allDatesSet = new TreeSet<>(Comparator.comparing((String d) -> LocalDate.parse(d, DATE_PARSER)));
        allDatesSet.addAll(ventesByDate.keySet());
        allDatesSet.addAll(achatsByDate.keySet());
        List<String> allDates = new ArrayList<>(allDatesSet);

        StringBuilder html = new StringBuilder();
        html.append(
                """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta charset="UTF-8" />
                                <style>
                                    @page { size: A4 landscape; margin: 20px; }
                                    body { font-family: 'Arial', sans-serif; font-size: 10px; }
                                    h1 { text-align: center; font-size: 14px; font-weight: bold; margin-bottom: 10px; }
                                    table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                                    th, td { border: 1px solid black; padding: 4px; text-align: center; vertical-align: middle; }
                                    th { background-color: #D9E1F2; font-weight: bold; }
                                    .bottom-panel { margin-top: 20px; }
                                </style>
                            </head>
                            <body>
                        """);

        html.append("<h1>سجل شراء و بيع مبيدات الآفات الزراعية<br>");
        html.append("Registre des Achats/Ventes des pesticides à usage agricole</h1>");

        html.append("<table>");
        html.append("<tr>");
        html.append("<th colspan='6'>Ventes – بيع</th>");
        html.append("<th colspan='5'>Achats – شراء</th>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<th>رقم بطاقة تعريف المشتري وعنوانه</th>");
        html.append("<th>اسم المشتري</th>");
        html.append("<th>الكمية (كلغ)</th>");
        html.append("<th>رقم ترخيص المبيد</th>");
        html.append("<th>اسم المبيد</th>");
        html.append("<th>تاريخ</th>");
        html.append("<th>اسم البائع</th>");
        html.append("<th>الكمية (كلغ)</th>");
        html.append("<th>رقم ترخيص المبيد</th>");
        html.append("<th>اسم المبيد</th>");
        html.append("<th>تاريخ</th>");
        html.append("</tr>");

        for (String date : allDates) {
            List<SaleItem> dayVentes = ventesByDate.getOrDefault(date, Collections.emptyList());
            List<PurchaseItem> dayAchats = achatsByDate.getOrDefault(date, Collections.emptyList());
            int numRows = Math.max(dayVentes.size(), dayAchats.size());

            for (int i = 0; i < numRows; i++) {
                html.append("<tr>");

                if (i < dayVentes.size()) {
                    SaleItem item = dayVentes.get(i);
                    Sale sale = item.getSale();
                    String unit = item.getUnitSnapshot() != null ? item.getUnitSnapshot() : "";
                    html.append("<td>").append(sale.getCustomerCinSnapshot()).append("</td>");
                    html.append("<td>").append(sale.getCustomerNameSnapshot()).append("</td>");
                    html.append("<td>").append(item.getQuantity()).append(" ").append(unit).append("</td>");
                    html.append("<td>").append(item.getLicenseNumberSnapshot()).append("</td>");
                    html.append("<td>").append(item.getProductNameSnapshot()).append("</td>");
                    html.append("<td>").append(DATE_FORMATTER.format(sale.getSaleDate())).append("</td>");
                } else {
                    html.append("<td></td><td></td><td></td><td></td><td></td><td></td>");
                }

                if (i < dayAchats.size()) {
                    PurchaseItem item = dayAchats.get(i);
                    Purchase purchase = item.getPurchase();
                    String unit = item.getUnitSnapshot() != null ? item.getUnitSnapshot() : "";
                    html.append("<td>").append(purchase.getProviderNameSnapshot()).append("</td>");
                    html.append("<td>").append(item.getQuantity()).append(" ").append(unit).append("</td>");
                    html.append("<td>").append(item.getLicenseNumberSnapshot()).append("</td>");
                    html.append("<td>").append(item.getProductNameSnapshot()).append("</td>");
                    html.append("<td>").append(DATE_FORMATTER.format(purchase.getPurchaseDate())).append("</td>");
                } else {
                    html.append("<td></td><td></td><td></td><td></td><td></td>");
                }

                html.append("</tr>");
            }
        }

        html.append("</table>");

        html.append("""
                    <table class="bottom-panel">
                        <tr>
                            <th>Date - التاريخ</th>
                            <th colspan="3">Identité de l'agent habilité - هوية العون المؤهل</th>
                            <th colspan="7">Observations/recommandations - الملاحظات / التوصيات</th>
                        </tr>
                        <tr>
                            <td>&nbsp;</td>
                            <td colspan="3">&nbsp;</td>
                            <td colspan="7">&nbsp;</td>
                        </tr>
                    </table>
                """);

        html.append("</body></html>");
        return html.toString();
    }
}