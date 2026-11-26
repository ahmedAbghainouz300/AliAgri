package com.aliagri.demo.service.report;

import com.aliagri.demo.model.Purchase;
import com.aliagri.demo.model.PurchaseItem;
import com.aliagri.demo.model.Sale;
import com.aliagri.demo.model.SaleItem;
import com.aliagri.demo.repository.PurchaseRepository;
import com.aliagri.demo.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service("excelExporter")
@RequiredArgsConstructor
public class RegistreExcelExporter implements RegistreExporter {

    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_PARSER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final int COLS = 11;
    private static final int FIRST_DATA_ROW = 3;
    private static final int MAX_ROWS_PER_SHEET = 8;
    private static final int[] COLUMN_WIDTHS = { 12, 20, 10, 14, 12, 10, 12, 10, 14, 14, 10 };
    private static final int[] SMALL_FONT_COLS = { 4, 9 };

    @Override
    public ByteArrayInputStream generate() throws IOException {
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

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = createStyles(workbook);

            int sheetCount = 1;
            Sheet currentSheet = workbook.createSheet("Registre_" + sheetCount);
            createTableTemplate(currentSheet, styles);

            int dataRow = FIRST_DATA_ROW;
            int sheetDataCount = 0;

            for (String date : allDates) {
                List<SaleItem> dayVentes = ventesByDate.getOrDefault(date, Collections.emptyList());
                List<PurchaseItem> dayAchats = achatsByDate.getOrDefault(date, Collections.emptyList());

                int numRows = Math.max(dayVentes.size(), dayAchats.size());

                for (int i = 0; i < numRows; i++) {
                    if (sheetDataCount >= MAX_ROWS_PER_SHEET) {
                        sheetCount++;
                        currentSheet = workbook.createSheet("Registre_" + sheetCount);
                        createTableTemplate(currentSheet, styles);
                        dataRow = FIRST_DATA_ROW;
                        sheetDataCount = 0;
                    }

                    Row row = currentSheet.getRow(dataRow);

                    // Sales
                    if (i < dayVentes.size()) {
                        SaleItem item = dayVentes.get(i);
                        Sale sale = item.getSale();
                        String unit = item.getUnitSnapshot() != null ? item.getUnitSnapshot() : "";
                        setText(row, 0, sale.getCustomerCinSnapshot());
                        setText(row, 1, sale.getCustomerNameSnapshot());
                        setText(row, 2, item.getQuantity() + " " + unit);
                        setText(row, 3, item.getLicenseNumberSnapshot());
                        setText(row, 4, item.getProductNameSnapshot());
                        setText(row, 5, DATE_FORMATTER.format(sale.getSaleDate()));
                    }

                    // Purchases
                    if (i < dayAchats.size()) {
                        PurchaseItem item = dayAchats.get(i);
                        Purchase purchase = item.getPurchase();
                        String unit = item.getUnitSnapshot() != null ? item.getUnitSnapshot() : "";
                        setText(row, 6, purchase.getProviderNameSnapshot().toUpperCase());
                        setText(row, 7, item.getQuantity() + " " + unit);
                        setText(row, 8, item.getLicenseNumberSnapshot());
                        setText(row, 9, item.getProductNameSnapshot());
                        setText(row, 10, DATE_FORMATTER.format(purchase.getPurchaseDate()));
                    }

                    dataRow++;
                    sheetDataCount++;
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    // --- Excel-specific helpers ---
    private void setText(Row row, int col, Object value) {
        Cell cell = row.getCell(col);
        if (cell == null)
            cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value.toString());
    }

    private static final class Styles {
        XSSFCellStyle title, header, panel, data, dataSmall;
    }

    private Styles createStyles(Workbook workbook) {
        Styles s = new Styles();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 12);
        titleFont.setUnderline(Font.U_SINGLE);

        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldFont.setFontHeightInPoints((short) 10);

        Font panelFont = workbook.createFont();
        panelFont.setFontHeightInPoints((short) 9);

        Font smallFont = workbook.createFont();
        smallFont.setFontHeightInPoints((short) 8);

        s.title = (XSSFCellStyle) workbook.createCellStyle();
        s.title.setFont(titleFont);
        s.title.setAlignment(HorizontalAlignment.CENTER);
        s.title.setVerticalAlignment(VerticalAlignment.CENTER);

        s.header = (XSSFCellStyle) workbook.createCellStyle();
        applyBorders(s.header);
        applyHeaderFill(s.header);
        s.header.setFont(boldFont);
        s.header.setAlignment(HorizontalAlignment.CENTER);
        s.header.setVerticalAlignment(VerticalAlignment.CENTER);
        s.header.setWrapText(true);

        s.panel = (XSSFCellStyle) workbook.createCellStyle();
        applyBorders(s.panel);
        applyHeaderFill(s.panel);
        s.panel.setFont(panelFont);
        s.panel.setAlignment(HorizontalAlignment.CENTER);
        s.panel.setVerticalAlignment(VerticalAlignment.CENTER);
        s.panel.setWrapText(true);

        s.data = (XSSFCellStyle) workbook.createCellStyle();
        applyBorders(s.data);
        s.data.setAlignment(HorizontalAlignment.CENTER);
        s.data.setVerticalAlignment(VerticalAlignment.CENTER);

        s.dataSmall = (XSSFCellStyle) workbook.createCellStyle();
        applyBorders(s.dataSmall);
        s.dataSmall.setAlignment(HorizontalAlignment.CENTER);
        s.dataSmall.setVerticalAlignment(VerticalAlignment.CENTER);
        s.dataSmall.setFont(smallFont);

        return s;
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void applyHeaderFill(XSSFCellStyle style) {
        style.setFillForegroundColor(new XSSFColor(new java.awt.Color(0xD9, 0xE1, 0xF2), null));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private void mergeStyled(Sheet sheet, Row row, int firstCol, int lastCol, String text, CellStyle style) {
        for (int c = firstCol; c <= lastCol; c++) {
            Cell cell = row.createCell(c);
            cell.setCellStyle(style);
            if (c == firstCol && text != null)
                cell.setCellValue(text);
        }
        if (lastCol > firstCol) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), firstCol, lastCol));
        }
    }

    private void createTableTemplate(Sheet sheet, Styles s) {
        String[] ventesCols = { "رقم بطاقة تعريف المشتري وعنوانه", "اسم المشتري", "الكمية (كلغ)", "رقم ترخيص المبيد",
                "اسم المبيد", "تاريخ" };
        String[] achatsCols = { "اسم البائع", "الكمية (كلغ)", "رقم ترخيص المبيد", "اسم المبيد", "تاريخ" };

        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            sheet.setColumnWidth(i, COLUMN_WIDTHS[i] * 256);
        }

        Row row1 = sheet.createRow(0);
        row1.setHeightInPoints(22);
        mergeStyled(sheet, row1, 0, COLS - 1,
                "سجل شراء و بيع مبيدات الآفات الزراعية        Registre des Achats/Ventes des pesticides à usage agricole",
                s.title);

        Row row2 = sheet.createRow(1);
        row2.setHeightInPoints(18);
        mergeStyled(sheet, row2, 0, 5, "Ventes – بيع", s.header);
        mergeStyled(sheet, row2, 6, 10, "Achats – شراء", s.header);

        Row row3 = sheet.createRow(2);
        row3.setHeightInPoints(30);
        for (int i = 0; i < ventesCols.length; i++) {
            Cell cell = row3.createCell(i);
            cell.setCellValue(ventesCols[i]);
            cell.setCellStyle(s.header);
        }
        for (int i = 0; i < achatsCols.length; i++) {
            Cell cell = row3.createCell(6 + i);
            cell.setCellValue(achatsCols[i]);
            cell.setCellStyle(s.header);
        }

        for (int r = FIRST_DATA_ROW; r < FIRST_DATA_ROW + MAX_ROWS_PER_SHEET; r++) {
            Row row = sheet.createRow(r);
            row.setHeightInPoints(33);
            for (int c = 0; c < COLS; c++) {
                Cell cell = row.createCell(c);
                boolean small = (c == SMALL_FONT_COLS[0] || c == SMALL_FONT_COLS[1]);
                cell.setCellStyle(small ? s.dataSmall : s.data);
            }
        }

        for (int r = 11; r <= 13; r++) {
            Row spacer = sheet.createRow(r);
            spacer.setHeightInPoints(5);
        }

        Row visitRow = sheet.createRow(14);
        visitRow.setHeightInPoints(22);
        mergeStyled(sheet, visitRow, 0, COLS - 1,
                "زيارة الأعوان المؤهلين من قبل المكتب الوطني للسلامة الصحية للمنتجات الغذائية - Visite des agents habilités de l'ONSSA",
                s.header);

        Row bottomRow = sheet.createRow(15);
        bottomRow.setHeightInPoints(25);
        mergeStyled(sheet, bottomRow, 0, 0, "Date - التاريخ", s.panel);
        mergeStyled(sheet, bottomRow, 1, 3, "Identité de l'agent habilité - هوية العون المؤهل", s.panel);
        mergeStyled(sheet, bottomRow, 4, 10, "Observations/recommandations - الملاحظات / التوصيات", s.panel);

        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setAutobreaks(true);
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short) 1);
        sheet.getPrintSetup().setFitHeight((short) 0);

        sheet.setMargin(PageMargin.LEFT, 0.25);
        sheet.setMargin(PageMargin.RIGHT, 0.25);
        sheet.setMargin(PageMargin.TOP, 0.5);
        sheet.setMargin(PageMargin.BOTTOM, 0.5);
        sheet.setMargin(PageMargin.HEADER, 0.3);
        sheet.setMargin(PageMargin.FOOTER, 0.3);
    }
}