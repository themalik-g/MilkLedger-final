package com.milkledger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PdfGenerator {

    private static final int PAGE_WIDTH = 595;  // A4 width in points (72 dpi)
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;
    private static final int LINE_HEIGHT = 14;
    private static final int HEADER_SIZE = 12;
    private static final int BODY_SIZE = 10;

    public interface PdfCallback {
        void onPdfGenerated(File pdfFile);
        void onError(String error);
    }

    public static void generatePdf(Context context, String title, String reportText, PdfCallback callback) {
        new Thread(() -> {
            try {
                File pdfFile = createPdfDocument(context, title, reportText);
                if (pdfFile != null && pdfFile.exists()) {
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onPdfGenerated(pdfFile));
                } else {
                    ((android.app.Activity) context).runOnUiThread(() -> callback.onError("Failed to create PDF"));
                }
            } catch (Exception e) {
                ((android.app.Activity) context).runOnUiThread(() -> callback.onError(e.getMessage()));
            }
        }).start();
    }

    private static File createPdfDocument(Context context, String title, String reportText) throws IOException {
        PdfDocument document = new PdfDocument();
        Paint paint = new Paint();
        paint.setTextSize(BODY_SIZE);

        List<String> lines = wrapText(reportText, PAGE_WIDTH - 2 * MARGIN, paint);
        int linesPerPage = (PAGE_HEIGHT - 2 * MARGIN - 60) / LINE_HEIGHT;
        int totalPages = (int) Math.ceil((double) lines.size() / linesPerPage);
        if (totalPages == 0) totalPages = 1;

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNum + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            // Draw header
            Paint headerPaint = new Paint();
            headerPaint.setTextSize(HEADER_SIZE);
            headerPaint.setFakeBoldText(true);
            canvas.drawText("Milk Ledger", MARGIN, MARGIN, headerPaint);
            canvas.drawText(title, MARGIN, MARGIN + 20, headerPaint);

            Paint linePaint = new Paint();
            linePaint.setTextSize(BODY_SIZE);

            int startLine = pageNum * linesPerPage;
            int endLine = Math.min(startLine + linesPerPage, lines.size());
            int y = MARGIN + 50;

            for (int i = startLine; i < endLine; i++) {
                String line = lines.get(i);
                if (line.contains("THEY OWE YOU")) {
                    linePaint.setColor(android.graphics.Color.parseColor("#2E7D32"));
                } else if (line.contains("YOU OWE THEM")) {
                    linePaint.setColor(android.graphics.Color.parseColor("#C62828"));
                } else if (line.startsWith("---")) {
                    linePaint.setFakeBoldText(true);
                    linePaint.setColor(android.graphics.Color.BLACK);
                } else {
                    linePaint.setColor(android.graphics.Color.BLACK);
                    linePaint.setFakeBoldText(false);
                }
                canvas.drawText(line, MARGIN, y, linePaint);
                y += LINE_HEIGHT;
            }

            // Page number
            Paint pageNumPaint = new Paint();
            pageNumPaint.setTextSize(8);
            canvas.drawText("Page " + (pageNum + 1) + " of " + totalPages, PAGE_WIDTH - MARGIN - 50, PAGE_HEIGHT - 20, pageNumPaint);

            document.finishPage(page);
        }

        File dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) dir = context.getFilesDir();
        String time = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        String safeTitle = title.replaceAll("[^a-zA-Z0-9]", "_");
        File pdfFile = new File(dir, "MilkLedger_" + safeTitle + "_" + time + ".pdf");

        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();

        return pdfFile;
    }

    private static List<String> wrapText(String text, int maxWidth, Paint paint) {
        List<String> lines = new ArrayList<>();
        String[] rawLines = text.split("\n");
        for (String rawLine : rawLines) {
            if (rawLine.isEmpty()) {
                lines.add("");
                continue;
            }
            StringBuilder currentLine = new StringBuilder();
            String[] words = rawLine.split(" ");
            for (String word : words) {
                String testLine = currentLine.length() > 0 ? currentLine + " " + word : word;
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = new StringBuilder(testLine);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                    }
                    currentLine = new StringBuilder(word);
                }
            }
            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }
        return lines;
    }

    public static void sharePdf(Context context, File pdfFile) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.setPackage("com.whatsapp");

        try {
            context.startActivity(shareIntent);
        } catch (Exception e) {
            shareIntent.setPackage(null);
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF via"));
        }
    }

    public static void openPdf(Context context, File pdfFile) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "No PDF viewer installed", Toast.LENGTH_SHORT).show();
        }
    }
}
