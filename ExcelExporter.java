package com.inventaire;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Exporte l'inventaire en fichier .xlsx (format Office Open XML).
 * Aucune librairie externe requise.
 */
public class ExcelExporter {

    private static final String[] HEADERS = {
        "ID", "Reference", "Nom", "Description", "Categorie",
        "Quantite", "Qte min.", "Prix (EUR)", "Statut"
    };

    private static final String COLOR_HEADER    = "FF2E5090";
    private static final String COLOR_LOW_STOCK = "FFFFD7D7";

    // ── Export principal ─────────────────────────────────────────────────────
    public static void export(List<Product> products, List<String> categories,
                              File outputFile) throws IOException {

        // Regrouper les produits par catégorie
        Map<String, List<Product>> byCategory = new LinkedHashMap<>();
        for (String cat : categories) byCategory.put(cat, new ArrayList<>());
        for (Product p : products) {
            byCategory.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
        }

        // Collecter toutes les shared strings
        List<String> ss = new ArrayList<>();
        for (String h : HEADERS) addSS(ss, h);
        for (Product p : products) {
            addSS(ss, p.getReference());
            addSS(ss, p.getName());
            addSS(ss, p.getDescription() != null ? p.getDescription() : "");
            addSS(ss, p.getCategory());
            addSS(ss, p.getStatus() != null ? p.getStatus() : "");
        }

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(outputFile))) {
            addEntry(zip, "[Content_Types].xml",        buildContentTypes(categories));
            addEntry(zip, "_rels/.rels",                buildRootRels());
            addEntry(zip, "xl/workbook.xml",            buildWorkbook(categories));
            addEntry(zip, "xl/_rels/workbook.xml.rels", buildWorkbookRels(categories));
            addEntry(zip, "xl/styles.xml",              buildStyles());
            addEntry(zip, "xl/sharedStrings.xml",       buildSharedStrings(ss));

            for (int i = 0; i < categories.size(); i++) {
                String cat = categories.get(i);
                List<Product> catProducts = byCategory.getOrDefault(cat, new ArrayList<>());
                addEntry(zip, "xl/worksheets/sheet" + (i + 1) + ".xml",
                         buildSheet(catProducts, ss));
            }

            addEntry(zip, "docProps/app.xml", buildAppProps());
        }
    }

    // ── Feuille de données ───────────────────────────────────────────────────
    private static String buildSheet(List<Product> products, List<String> ss) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");

        // Largeurs colonnes
        sb.append("<cols>");
        sb.append("<col min=\"1\" max=\"1\" width=\"6\"  customWidth=\"1\"/>");
        sb.append("<col min=\"2\" max=\"2\" width=\"12\" customWidth=\"1\"/>");
        sb.append("<col min=\"3\" max=\"3\" width=\"28\" customWidth=\"1\"/>");
        sb.append("<col min=\"4\" max=\"4\" width=\"30\" customWidth=\"1\"/>");
        sb.append("<col min=\"5\" max=\"5\" width=\"16\" customWidth=\"1\"/>");
        sb.append("<col min=\"6\" max=\"6\" width=\"10\" customWidth=\"1\"/>");
        sb.append("<col min=\"7\" max=\"7\" width=\"10\" customWidth=\"1\"/>");
        sb.append("<col min=\"8\" max=\"8\" width=\"12\" customWidth=\"1\"/>");
        sb.append("<col min=\"9\" max=\"9\" width=\"12\" customWidth=\"1\"/>");
        sb.append("</cols>");

        // ── Données ──────────────────────────────────────────────────────────
        sb.append("<sheetData>");

        // En-tête (ligne 1)
        sb.append("<row r=\"1\">");
        for (int c = 0; c < HEADERS.length; c++) {
            sb.append("<c r=\"").append(col(c + 1)).append("1\" t=\"s\" s=\"1\">")
              .append("<v>").append(idxSS(ss, HEADERS[c])).append("</v></c>");
        }
        sb.append("</row>");

        // Lignes produits
        for (int r = 0; r < products.size(); r++) {
            Product p = products.get(r);
            int row = r + 2;
            boolean low = p.getQuantity() <= p.getMinQuantity();
            int base = low ? 4 : 0;

            sb.append("<row r=\"").append(row).append("\">");
            sb.append(nc(col(1), row, p.getId(),          low ? 4 : 2));
            sb.append(sc(col(2), row, idxSS(ss, p.getReference()), base));
            sb.append(sc(col(3), row, idxSS(ss, p.getName()),      base));
            String desc = p.getDescription() != null ? p.getDescription() : "";
            sb.append(sc(col(4), row, idxSS(ss, desc),             base));
            sb.append(sc(col(5), row, idxSS(ss, p.getCategory()),  base));
            sb.append(nc(col(6), row, p.getQuantity(),    low ? 4 : 2));
            sb.append(nc(col(7), row, p.getMinQuantity(), low ? 4 : 2));
            sb.append(sc(col( 8), row, idxSS(ss, p.getStorageLocation()), base));  ;
            String status = p.getStatus() != null ? p.getStatus() : "";
            sb.append(sc(col(9), row, idxSS(ss, status),           base));
            sb.append("</row>");
        }

        sb.append("</sheetData>");
        sb.append("<autoFilter ref=\"A1:I1\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    // ── Helpers cellules ─────────────────────────────────────────────────────
    private static String sc(String c, int r, int ssIdx, int style) {
        return "<c r=\"" + c + r + "\" t=\"s\" s=\"" + style + "\"><v>" + ssIdx + "</v></c>";
    }
    private static String nc(String c, int r, Number v, int style) {
        return "<c r=\"" + c + r + "\" s=\"" + style + "\"><v>" + v + "</v></c>";
    }
    private static String col(int n) {
        StringBuilder sb = new StringBuilder();
        while (n > 0) { n--; sb.insert(0, (char)('A' + n % 26)); n /= 26; }
        return sb.toString();
    }
    private static void addSS(List<String> ss, String v) {
        if (v != null && !ss.contains(v)) ss.add(v);
    }
    private static int idxSS(List<String> ss, String v) {
        int i = ss.indexOf(v); return i >= 0 ? i : 0;
    }
    private static String x(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;");
    }

    // ── XMLs structurels ────────────────────────────────────────────────────
    private static String buildContentTypes(List<String> cats) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">");
        sb.append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>");
        sb.append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>");
        sb.append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>");
        for (int i = 0; i < cats.size(); i++)
            sb.append("<Override PartName=\"/xl/worksheets/sheet").append(i+1)
              .append(".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
        sb.append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
        sb.append("<Override PartName=\"/xl/sharedStrings.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml\"/>");
        sb.append("<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>");
        sb.append("</Types>");
        return sb.toString();
    }

    private static String buildRootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties\" Target=\"docProps/app.xml\"/>"
            + "</Relationships>";
    }

    private static String buildWorkbook(List<String> cats) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" ");
        sb.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
        sb.append("<bookViews><workbookView xWindow=\"0\" yWindow=\"0\" windowWidth=\"14400\" windowHeight=\"8000\"/></bookViews>");
        sb.append("<sheets>");
        for (int i = 0; i < cats.size(); i++)
            sb.append("<sheet name=\"").append(x(cats.get(i)))
              .append("\" sheetId=\"").append(i+1)
              .append("\" r:id=\"rId").append(i+1).append("\"/>");
        sb.append("</sheets></workbook>");
        return sb.toString();
    }

    private static String buildWorkbookRels(List<String> cats) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">");
        for (int i = 0; i < cats.size(); i++)
            sb.append("<Relationship Id=\"rId").append(i+1)
              .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\"")
              .append(" Target=\"worksheets/sheet").append(i+1).append(".xml\"/>");
        int b = cats.size();
        sb.append("<Relationship Id=\"rId").append(b+1)
          .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        sb.append("<Relationship Id=\"rId").append(b+2)
          .append("\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings\" Target=\"sharedStrings.xml\"/>");
        sb.append("</Relationships>");
        return sb.toString();
    }

    private static String buildStyles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<fonts count=\"2\">"
            + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
            + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
            + "</fonts>"
            + "<fills count=\"4\">"
            + "<fill><patternFill patternType=\"none\"/></fill>"
            + "<fill><patternFill patternType=\"gray125\"/></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"" + COLOR_HEADER + "\"/></patternFill></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"" + COLOR_LOW_STOCK + "\"/></patternFill></fill>"
            + "</fills>"
            + "<borders count=\"2\">"
            + "<border><left/><right/><top/><bottom/><diagonal/></border>"
            + "<border>"
            + "<left style=\"thin\"><color rgb=\"FFD0D0D0\"/></left>"
            + "<right style=\"thin\"><color rgb=\"FFD0D0D0\"/></right>"
            + "<top style=\"thin\"><color rgb=\"FFD0D0D0\"/></top>"
            + "<bottom style=\"thin\"><color rgb=\"FFD0D0D0\"/></bottom>"
            + "<diagonal/></border>"
            + "</borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"5\">"
            // 0 : normal avec bordure
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\"/>"
            // 1 : header blanc gras sur fond bleu
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
            // 2 : centré
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
            // 3 : prix 2 décimales
            + "<xf numFmtId=\"4\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\"/>"
            // 4 : stock faible (fond rouge clair)
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFill=\"1\"/>"
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";
    }

    private static String buildSharedStrings(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" count=\"")
          .append(strings.size()).append("\" uniqueCount=\"").append(strings.size()).append("\">");
        for (String s : strings)
            sb.append("<si><t xml:space=\"preserve\">").append(x(s)).append("</t></si>");
        sb.append("</sst>");
        return sb.toString();
    }

    private static String buildAppProps() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
            + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\">"
            + "<Application>Inventaire-v2</Application>"
            + "</Properties>";
    }

    private static void addEntry(ZipOutputStream zip, String name, String content)
            throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes("UTF-8"));
        zip.closeEntry();
    }

    // ── Point d'entrée avec dialogue ────────────────────────────────────────
    public static void exportWithDialog(java.awt.Component parent,
                                        ProductController controller) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exporter vers Excel");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        chooser.setSelectedFile(new File("inventaire_" + ts + ".xlsx"));
        chooser.setFileFilter(new FileNameExtensionFilter("Fichier Excel (*.xlsx)", "xlsx"));

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().endsWith(".xlsx"))
            file = new File(file.getAbsolutePath() + ".xlsx");

        try {
            export(controller.getAllProducts(), controller.getAllCategories(), file);
            JOptionPane.showMessageDialog(parent,
                "Export reussi !\n" + file.getName() + "\n"
                + controller.getAllCategories().size() + " onglet(s) cree(s)",
                "Export Excel", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Erreur lors de l'export : " + ex.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
