package pdfprint.aspire.com.pdfprint.coder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sunisha on 4/4/2018.
 */

public class PrintDocumentAdapterN extends PrintDocumentAdapter {
    Context context;
//    private int pageHeight;
//    private int pageWidth;
    public PdfDocument myPdfDocument;
    View mView;
    PDFView mPdfView;
//    public int totalpages = 4;
    public int totalpages ;
    public PrintDocumentAdapterN(Context context, View mView, int countPages, PDFView mPdfView) {
        this.context = context;
        this.mView=mView;
        this.mPdfView = mPdfView;
        this.totalpages=countPages;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes,
                         PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal,
                         LayoutResultCallback callback,
                         Bundle metadata) {

        myPdfDocument = new PrintedPdfDocument(context, newAttributes);

        /*pageHeight =
                newAttributes.getMediaSize().getHeightMils() / 1000 * 72;
        pageWidth =
                newAttributes.getMediaSize().getWidthMils() / 1000 * 72;*/

        if (cancellationSignal.isCanceled()) {
            callback.onLayoutCancelled();
            return;
        }

        if (totalpages > 0) {
            PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                    .Builder("print_output.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(totalpages);

            PrintDocumentInfo info = builder.build();
            callback.onLayoutFinished(info, true);
        } else {
            callback.onLayoutFailed("Page count is zero.");
        }
    }

    @Override
    public void onWrite(final PageRange[] pageRanges,
                        final ParcelFileDescriptor destination,
                        final CancellationSignal cancellationSignal,
                        final WriteResultCallback callback) {

        for (int i = 0; i < totalpages; i++) {
            if (pageInRange(pageRanges, i))
            {
//                mPdfView.getPageAtPositionOffset(1);
                mPdfView.loadPages();

                mPdfView.getCurrentPage();
                PdfDocument.PageInfo newPage = new PdfDocument.PageInfo.Builder(mPdfView.getWidth(),
                        mPdfView.getHeight(), mPdfView.getCurrentPage()).create();

                PdfDocument.Page page =
                        myPdfDocument.startPage(newPage);

                if (cancellationSignal.isCanceled()) {
                    callback.onWriteCancelled();
                    myPdfDocument.close();
                    myPdfDocument = null;
                    return;
                }
                drawPage(page, i);
                myPdfDocument.finishPage(page);
            }
        }

        try {
            myPdfDocument.writeTo(new FileOutputStream(
                    destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            myPdfDocument.close();
            myPdfDocument = null;
        }

        callback.onWriteFinished(pageRanges);
    }

    private boolean pageInRange(PageRange[] pageRanges, int page) {
        for (int i = 0; i < pageRanges.length; i++) {
            if ((page >= pageRanges[i].getStart()) &&
                    (page <= pageRanges[i].getEnd()))
                return true;
        }
        return false;
    }

    private void drawPage(PdfDocument.Page page,
                          int pagenumber) {
//        Canvas canvas = page.getCanvas();


//        int titleBaseLine = 72;
//        int leftMargin = 54;
//
//        Paint paint = new Paint();
//        paint.setColor(Color.BLACK);
//        paint.setTextSize(40);
//        canvas.drawText(
//                "Test Print Document Page " + pagenumber,
//                leftMargin,
//                titleBaseLine,
//                paint);
//
//        paint.setTextSize(14);
//        canvas.drawText("This is some test content to verify that custom document printing works", leftMargin, titleBaseLine + 35, paint);
//
//        if (pagenumber % 2 == 0)
//            paint.setColor(Color.RED);
//        else
//            paint.setColor(Color.GREEN);
//
//        PdfDocument.PageInfo pageInfo = page.getInfo();
//
//
//        canvas.drawCircle(pageInfo.getPageWidth() / 2,
//                pageInfo.getPageHeight() / 2,
//                150,
//                paint);



        // Create a bitmap and put it a canvas for the view to draw to. Make it the size of the view
        Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(),
                Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        pagenumber++; // Make sure page numbers start at 1

        mView.draw(canvas);
        // create a Rect with the view's dimensions.
        Rect src = new Rect(0, 0, mView.getWidth(), mView.getHeight());
        // get the page canvas and measure it.
        Canvas pageCanvas = page.getCanvas();
        float pageWidth = pageCanvas.getWidth();
        float pageHeight = pageCanvas.getHeight();
        // how can we fit the Rect src onto this page while maintaining aspect ratio?
        float scale = Math.min(pageWidth/src.width(), pageHeight/src.height());
        float left = pageWidth / 2 - src.width() * scale / 2;
        float top = pageHeight / 2 - src.height() * scale / 2;
        float right = pageWidth / 2 + src.width() * scale / 2;
        float bottom = pageHeight / 2 + src.height() * scale / 2;
        RectF dst = new RectF(left, top, right, bottom);

        pageCanvas.drawBitmap(bitmap, src, dst, null);

    }
}
