package astro.note.service;

import astro.note.entity.Note;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExcelService {
    public ByteArrayInputStream exportNotesToExcel(List<Note> notes) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("<?xml version=\"1.0\"?>");
            sb.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" ");
            sb.append("xmlns:o=\"urn:schemas-microsoft-com:office:office\" ");
            sb.append("xmlns:x=\"urn:schemas-microsoft-com:office:excel\" ");
            sb.append("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");

            sb.append("<Styles>");
            sb.append("<Style ss:ID=\"HeaderStyle\">");
            sb.append("<Font ss:Bold=\"1\" ss:Size=\"12\" ss:FontName=\"Times New Roman\"/>");
            sb.append("<Interior ss:Color=\"#92C6E9\" ss:Pattern=\"Solid\"/>");
            sb.append("</Style>");
            sb.append("<Style ss:ID=\"TitleStyle\">");
            sb.append("<Font ss:Bold=\"1\" ss:Size=\"36\" ss:FontName=\"Times New Roman\" ss:Color=\"#FF0000\"/>");
            sb.append("<Interior ss:Color=\"#FFFF00\" ss:Pattern=\"Solid\"/>");
            sb.append("<Alignment ss:Horizontal=\"Center\"/>");
            sb.append("</Style>");
            sb.append("<Style ss:ID=\"TimeStyle\">");
            sb.append("<Font ss:Size=\"12\" ss:FontName=\"Times New Roman\" ss:Color=\"#FF0000\"/>");
            sb.append("</Style>");
            sb.append("</Styles>");

            sb.append("<Worksheet ss:Name=\"Notes\">");
            sb.append("<Table>");

            // Column widths
            sb.append("<Column ss:AutoFitWidth=\"0\" ss:Width=\"215\"/>");
            sb.append("<Column ss:AutoFitWidth=\"0\" ss:Width=\"550\"/>");
            sb.append("<Column ss:AutoFitWidth=\"0\" ss:Width=\"160\"/>");

            // Title row
            sb.append("<Row>");
            sb.append("<Cell ss:MergeAcross=\"2\" ss:StyleID=\"TitleStyle\"><Data ss:Type=\"String\">List of notes</Data></Cell>");
            sb.append("</Row>");

            // Header row
            sb.append("<Row>");
            sb.append("<Cell ss:StyleID=\"HeaderStyle\"><Data ss:Type=\"String\">CONTENT</Data></Cell>");
            sb.append("<Cell ss:StyleID=\"HeaderStyle\"><Data ss:Type=\"String\">TITLE</Data></Cell>");
            sb.append("<Cell ss:StyleID=\"HeaderStyle\"><Data ss:Type=\"String\">TIME</Data></Cell>");
            sb.append("</Row>");

            // Data rows
            for (Note note : notes) {
                sb.append("<Row>");
                sb.append("<Cell><Data ss:Type=\"String\">").append(note.getContent()).append("</Data></Cell>");
                sb.append("<Cell><Data ss:Type=\"String\">").append(note.getTitle()).append("</Data></Cell>");
                sb.append("<Cell ss:StyleID=\"TimeStyle\"><Data ss:Type=\"String\">").append(note.getTime().toString()).append("</Data></Cell>");
                sb.append("</Row>");
            }

            sb.append("</Table>");
            sb.append("</Worksheet>");
            sb.append("</Workbook>");

            return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
