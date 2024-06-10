/***
 * Kindle Cracker Convert kindle book into PDF format for personal use only
 *     Copyright (C) 2018  Karunakaran
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Also add information on how to contact you by electronic and paper mail.
 *
 *   If the program does terminal interaction, make it output a short
 * notice like this when it starts in an interactive mode:
 *
 *     <program>  Copyright (C) <year>  <name of author>
 *     This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
 *     This is free software, and you are welcome to redistribute it
 *     under certain conditions; type `show c' for details.
 *
 * The hypothetical commands `show w' and `show c' should show the appropriate
 * parts of the General Public License.  Of course, your program's commands
 * might be different; for a GUI interface, you would use an "about box".
 *
 *   You should also get your employer (if you work as a programmer) or school,
 * if any, to sign a "copyright disclaimer" for the program, if necessary.
 * For more information on this, and how to apply and follow the GNU GPL, see
 * <https://www.gnu.org/licenses/>.
 *
 *   The GNU General Public License does not permit incorporating your program
 * into proprietary programs.  If your program is a subroutine library, you
 * may consider it more useful to permit linking proprietary applications with
 * the library.  If this is what you want to do, use the GNU Lesser General
 * Public License instead of this License.  But first, please read
 * <https://www.gnu.org/licenses/why-not-lgpl.html>.
 */

package com.agkarun.kindlecracker.Service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.agkarun.kindlecracker.Activity.MainActivity;
import com.agkarun.kindlecracker.R;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CrackAccessibilityService extends AccessibilityService {
    private String userDefinedfileName;
    private int i = 1, x, y, totalPages, time, fromPage, existingFilePages;
    private static double compression = 30 * 0.09;
    private static Document document;
    private static PdfCopy pdfCopy;
    private boolean continueFromPage;
    private static boolean compress = false;
    private static int rate;
    private PdfReader pdfReader;
    private CrackAccessibilityService crackAccessibilityService;
    private static Display display;
    private static MediaProjection mediaProjection;
    private static ImageReader imageReader;
    private static ExecutorService executor;

    public CrackAccessibilityService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        crackAccessibilityService = new CrackAccessibilityService();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getValues(intent);
        startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("Intent");
        MediaProjectionManager projectionManager =
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED && data != null) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection != null) {
                Log.d("ScreenCaptureService", "MediaProjection obtained successfully");
                if (mediaProjection == null)
                    Log.e("++++++++++", "MEDIA PROJECTION NULL WHILE CREATION++++++++++");
                WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                display = windowManager.getDefaultDisplay();
                if (display == null) Log.e("++++++++++", "DISPLAY NULL WHILE CREATION++++++++++");
                if (display != null)
                    Log.e("++++++++++", "DISPLAY NOT NULL WHILE CREATION++++++++++");
            } else {
                Log.e("ScreenCaptureService", "MediaProjection is null");
            }
        } else {
            Log.e("ScreenCaptureService", "Invalid resultCode or data");
        }
        CrackAccessibilityService.executor = Executors.newSingleThreadExecutor();
        CrackAccessibilityService.executor.execute(new Runnable() {
            @Override
            public void run() {
                startConvert();
            }
        });
        return START_NOT_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public boolean stopService(Intent name) {
        Log.e("++STOP SERVICE++", "STOP");
        return super.stopService(name);
    }

// onDestroy() Will be called after the user stops the service by pressing Stop Converting button
// this method will stop the executor thread, media projection, foreground service and closes the documents
    @Override
    public void onDestroy() {
        Log.e("++DESTROY++", "DESTROY");
        if (CrackAccessibilityService.document != null) {
            CrackAccessibilityService.document.close();
            CrackAccessibilityService.document = null;
            CrackAccessibilityService.pdfCopy = null;
        }
        if (mediaProjection != null) mediaProjection.stop();
        try{
            CrackAccessibilityService.executor.shutdownNow();
            CrackAccessibilityService.executor=null;
        }
        catch (Exception e){
            Log.e("++CANCEL++", ""+e);
        }
        stopForeground(true);
        stopSelf();
        super.onDestroy();
    }

    // Getting user entered values
    public void getValues(Intent intent) {
        try {
            fromPage = intent.getExtras().getInt("fromPage");
            compress = intent.getExtras().getBoolean("compress");
            rate = intent.getExtras().getInt("compressRate");
            if (compress) {
                if (rate > 100) {
                    rate = 100;
                }
            }
            if (fromPage == -1 && !compress) {
                continueFromPage = false;
                compress = false;
            }

            if (fromPage == -1 && compress) {
                continueFromPage = false;
                compress = true;
            } else if (fromPage >= 1 && !compress) {
                continueFromPage = true;
                compress = false;
                i = fromPage;
            } else if (fromPage >= 1 && compress) {
                continueFromPage = true;
                compress = true;
                i = fromPage;
            }
            Log.e("CONTINUE PAGE", "" + continueFromPage);
            crackAccessibilityService = new CrackAccessibilityService();
            userDefinedfileName = intent.getStringExtra("file");
            x = intent.getExtras().getInt("x");
            y = intent.getExtras().getInt("y");
            time = intent.getExtras().getInt("time");
            totalPages = intent.getExtras().getInt("totalPages");
        } catch (Exception e) {
            Log.e("GET VALUES", "" + e);
        }
    }

    // startConvert is used for doing the converting related operation in background
    private void startConvert() {
        Looper.prepare();
        Toast.makeText(getApplicationContext(), userDefinedfileName + " will be started to convert within " + time + " Seconds", Toast.LENGTH_SHORT).show();
        try {
            synchronized (CrackAccessibilityService.executor) {
                executor.wait(time * 1000);
            }
        } catch (InterruptedException e) {

        }
        // if totalpages=0 the loop will run infinitly
        if (totalPages == 0 && !continueFromPage) {
            try {
                while (true) {
                    Log.e("++CALLING SCREENSHOT++", "CALLING SCREENSHOT METHOD");
                    crackAccessibilityService.takeScreenshot(String.valueOf(i));
                    synchronized (CrackAccessibilityService.executor) {
                        /* Executor will be locked and wait() called for capturing screenshot and
                           saving as PNG, because Imagelistener will work concurrently. If we did
                           not wait here, before saving the PNG the converTopdf() method will be called
                           eventually the application will crash. So after the PNG image generated notify()
                           will be called on the executor after that executor resumes and starts to convert
                           the generated PNG into PDF */
                        CrackAccessibilityService.executor.wait();
                    }
                    Log.e("++CALLING CONVERT++", "CALLING CONVERT");
                    crackAccessibilityService.convertPngToPdf(String.valueOf(i));
                    Log.e("++CALLING MERGE++", "CALLING MERGE");
                    crackAccessibilityService.mergePdf(String.valueOf(i), userDefinedfileName);
                    Log.e("++CALLING TOUCH++", "CALLING TOUCH");
                    touch(x, y);
                    Thread.sleep(1000);
                    i++;
                }
            } catch (Exception e) {
                if (document != null) document.close();
                Log.e("++CONVERTING EXCEPTION++", ""+e);
                e.printStackTrace();
            }
        }
        // if number of pages is defined this loop will run
        else if (totalPages >= i && !continueFromPage) {
            try {
                Log.e("++ENTERING LOOP++", "Entering Loop");
                while (totalPages >= i) {
                    Log.e("++CALLING SCREENSHOT++", "CALLING SCREENSHOT METHOD");
                    crackAccessibilityService.takeScreenshot(String.valueOf(i));
                    synchronized (CrackAccessibilityService.executor) {
                        /* Executor will be locked and wait() called for capturing screenshot and
                           saving as PNG, because Imagelistener will work concurrently. If we did
                           not wait here, before saving the PNG the converTopdf() method will be called
                           eventually the application will crash. So after the PNG image generated notify()
                           will be called on the executor after that executor resumes and starts to convert
                           the generated PNG into PDF */
                        CrackAccessibilityService.executor.wait();
                    }
                    Log.e("++CALLING CONVERT++", "CALLING CONVERT");
                    crackAccessibilityService.convertPngToPdf(String.valueOf(i));
                    Log.e("++CALLING MERGE++", "CALLING MERGE");
                    crackAccessibilityService.mergePdf(String.valueOf(i), userDefinedfileName);
                    Log.e("++CALLING TOUCH++", "CALLING TOUCH");
                    touch(x, y);
                    Thread.sleep(1000);
                    i++;
                }
            } catch (Exception e) {
                if (document != null) document.close();
                Log.e("++CONVERTING EXCEPTION++", ""+e);
                e.printStackTrace();
            }
        }

        // if user wants to continue from old pdf file this code will be executed
        else if (continueFromPage) {

            // if totalpages=0 the loop will run infinitly
            if (totalPages == 0) {
                try {
                    while (true) {
                        Log.e("++CALLING SCREENSHOT++", "CALLING SCREENSHOT METHOD");
                        crackAccessibilityService.takeScreenshot(String.valueOf(i));
                        synchronized (CrackAccessibilityService.executor) {
                        /* Executor will be locked and wait() called for capturing screenshot and
                           saving as PNG, because Imagelistener will work concurrently. If we did
                           not wait here, before saving the PNG the converTopdf() method will be called
                           eventually the application will crash. So after the PNG image generated notify()
                           will be called on the executor after that executor resumes and starts to convert
                           the generated PNG into PDF */
                            CrackAccessibilityService.executor.wait();
                        }
                        Log.e("++CALLING CONVERT++", "CALLING CONVERT");
                        crackAccessibilityService.convertPngToPdf(String.valueOf(i));
                        Log.e("++CALLING MERGE++", "CALLING MERGE");
                        crackAccessibilityService.continuePDFmerge(String.valueOf(i), userDefinedfileName);
                        Log.e("++CALLING TOUCH++", "CALLING TOUCH");
                        touch(x, y);
                        Thread.sleep(1000);
                        i++;
                    }
                } catch (Exception e) {
                    if (document != null) document.close();
                    Log.e("++CONVERTING EXCEPTION++", ""+e);
                    e.printStackTrace();
                }

            }

            // if number of pages is defined this loop will run
            else if (totalPages >= i) {
                try {
                    while (totalPages >= i) {
                        Log.e("++CALLING SCREENSHOT++", "CALLING SCREENSHOT METHOD");
                        crackAccessibilityService.takeScreenshot(String.valueOf(i));
                        synchronized (CrackAccessibilityService.executor) {
                        /* Executor will be locked and wait() called for capturing screenshot and
                           saving as PNG, because Imagelistener will work concurrently. If we did
                           not wait here, before saving the PNG the converTopdf() method will be called
                           eventually the application will crash. So after the PNG image generated notify()
                           will be called on the executor after that executor resumes and starts to convert
                           the generated PNG into PDF */
                            CrackAccessibilityService.executor.wait();
                        }
                        Log.e("++CALLING CONVERT++", "CALLING CONVERT");
                        crackAccessibilityService.convertPngToPdf(String.valueOf(i));
                        Log.e("++CALLING MERGE++", "CALLING MERGE");
                        crackAccessibilityService.continuePDFmerge(String.valueOf(i), userDefinedfileName);
                        Log.e("++CALLING TOUCH++", "CALLING TOUCH");
                        touch(x, y);
                        Thread.sleep(1000);
                        i++;
                    }

                } catch (Exception e) {
                    if (document != null) document.close();
                    Log.e("++CONVERTING EXCEPTION++", ""+e);
                    e.printStackTrace();
                }
            }
        }
        //After the converting loop ends
        if(i>totalPages){
            Log.e("++I VALUE++", ""+i);
            i = 1;
            Log.e("++I VALUE++", ""+i);
            if (CrackAccessibilityService.document != null) CrackAccessibilityService.document.close();
            CrackAccessibilityService.document = null;
            CrackAccessibilityService.pdfCopy = null;
            mediaProjection.stop();
            stopForeground(true);
            stopSelf();
            CrackAccessibilityService.executor.shutdownNow();
            CrackAccessibilityService.executor=null;
            showNotification(userDefinedfileName + " Successfully Converted to PDF");
            Toast.makeText(getApplicationContext(), userDefinedfileName + " Successfully Converted to PDF", Toast.LENGTH_LONG).show();
        }
        else{
            i = 1;
            if (CrackAccessibilityService.document != null) CrackAccessibilityService.document.close();
            CrackAccessibilityService.document = null;
            CrackAccessibilityService.pdfCopy = null;
            mediaProjection.stop();
            stopForeground(true);
            stopSelf();
            CrackAccessibilityService.executor.shutdownNow();
            CrackAccessibilityService.executor=null;
            Toast.makeText(getApplicationContext(),"Some Error occurred during PDF conversion!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * the touch method used for automate the touch event in device screen with given coordinate points
     *
     * @param x for x coordinate
     * @param y for y coordinate
     * @return reurns boolian
     */
    @TargetApi(24)
    public void touch(int x, int y) {
        Log.e("Touch 1", "");
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        Point position = new Point(x, y);
        p.moveTo(position.x, position.y);
        builder.addStroke(new GestureDescription.StrokeDescription(p, 10L, 200L));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.e("TOUCH COMPLETED", "TOUCH COMPLETED");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e("Touch Cancelled", gestureDescription.toString());
            }
        }, null);
        Log.e("++isDispatched++", "" + isDispatched);
    }

    /**
     * The take screenshot method will get parameter for PNG file name to be saved and capture
     * screenshot and save as PNG file
     *
     * @param fileName for PNG file to be saved
     */
    public void takeScreenshot(String fileName) throws InterruptedException {
        Log.e("++INSIDE TAKE SCREENSHOT++", "INSIDE TAKE SCREENSHOT");
        final DisplayMetrics metrics = new DisplayMetrics();
        Log.e("++++", "1");
        CrackAccessibilityService.display.getMetrics(metrics);
        Log.e("++++", "2");
        Point size = new Point();
        Log.e("++++", "3");
        CrackAccessibilityService.display.getRealSize(size);
        Log.e("++++", "4");
        final int width = size.x;
        Log.e("++++", "5");
        final int height = size.y;
        Log.e("++++", "6");
        int density = metrics.densityDpi;
        Log.e("++++", "7");
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        Log.e("++++", "8");
        final Handler handler = new Handler(Looper.getMainLooper());
        Log.e("++++", "9");
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        Log.e("++++", "10");
        mediaProjection.createVirtualDisplay("screen-mirror", width, height, density, flags, imageReader.getSurface(), null, handler);
        Log.e("++++", "11");
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                try {
                    Log.e("++INSIDE IMAGE LISTENER++", "INSIDE IMAGE LISTENER");
                    reader.setOnImageAvailableListener(null, handler);
                    Log.e("++++", "12");
                    android.media.Image image = reader.acquireLatestImage();
                    Log.e("++++", "13");
                    final android.media.Image.Plane[] planes = image.getPlanes();
                    Log.e("++++", "14");
                    final ByteBuffer buffer = planes[0].getBuffer();
                    Log.e("++++", "15");
                    int pixelStride = planes[0].getPixelStride();
                    Log.e("++++", "16");
                    int rowStride = planes[0].getRowStride();
                    Log.e("++++", "17");
                    int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                    Log.e("++++", "18");
                    Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride),
                            metrics.heightPixels, Bitmap.Config.ARGB_8888);
                    Log.e("++++", "19");
                    bmp.copyPixelsFromBuffer(buffer);
                    Log.e("++++", "20");
                    image.close();
                    Log.e("++++", "21");
                    reader.close();
                    Log.e("++++", "22");
                    Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());
                    Log.e("++++", "23");
                    bmp.recycle();
                    Log.e("++++", "24");
                    Log.e("++SAVING PNG++", "SAVING PNG");
                    File file = new File(Environment.getExternalStorageDirectory().getPath() + MainActivity.destFolder + fileName + ".png");
                    Log.e("++++", "25");
                    FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
                    Log.e("++++", "26");
                    realSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Log.e("++++", "27");
                    out.close();
                    synchronized (CrackAccessibilityService.executor) {
                        CrackAccessibilityService.executor.notify();
                    }
                    Log.e("++OBJ VALUE++", "" + CrackAccessibilityService.executor);
                    Log.e("++NOTIFIED++", "NOTIFIED");
                    Log.e("++CLOSING PNG++", "CLOSING PNG");
                    Log.e("++++", "28");
                } catch (Exception e) {
                    Log.e("+++++PNG PATH+++++", "" + e);
                    e.printStackTrace();
                }
            }
        }, handler);
    }

    /**
     * this method will get PNG file from the path and converts to PDF file of
     * the image in the same path
     *
     * @param name
     */
    public void convertPngToPdf(String name) throws Exception {
        Log.e("++INSIDE CONVERT++", "INSIDE CONVERT");
        Log.e("++PROCESSING PNG++", "NAME====>" + name);
        Document doc = new Document(PageSize.getRectangle("LEGAL"), 0, 0, 0, 0);
        doc.setMargins(0, 0, 0, 0);
        Rectangle rect = doc.getPageSize();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(Environment
                    .getExternalStorageDirectory() + MainActivity.destFolder + name + ".pdf"));
            doc.open();
            File file = new File(Environment.getExternalStorageDirectory() + MainActivity.destFolder + name + ".png");
            Image img = Image.getInstance(String.valueOf(file.toURI()));
            img.setCompressionLevel((int) compression);
            img.setBorder(Rectangle.BOX);
            img.setBorderWidth(0);
            img.scaleToFit(doc.getPageSize().getWidth(), doc.getPageSize().getHeight());
            img.setAbsolutePosition((rect.getWidth() - img.getScaledWidth()) / 2,
                    (rect.getHeight() - img.getScaledHeight()) / 2);
            addPagenumber(rect, writer, String.valueOf(name));
            doc.add(img);
            doc.close();
            Log.e("++CONVERT DONE++", "CONVERT DONE");
            if (compress) {
                new CrackAccessibilityService().compressPdf(name, rate);
            }
            file.delete();
        } catch (Exception e) {
            Log.e("+++Convert to PDF Error 1+++:", "" + e);
            if (document != null) {
                document.close();
            } else {
                Log.e("+++Convert to PDF Error 2++++:", "" + e);
            }
        }
    }

    /**
     * This method will add pages into the converted PDF file which is located inside the Converted folder
     *
     * @param name     existing pdf file name
     * @param fileName file name to be converted
     * @throws IOException
     * @throws DocumentException
     */
    public void mergePdf(String name, String fileName) throws IOException, DocumentException {
        Log.e("++INSIDE MERGE++", "INSIDE MERGE");
        File filepath = new File(Environment.getExternalStorageDirectory() + MainActivity.destFolder + name + ".pdf");
        try {
            // If it is first time an empty PDF will be created into the Converted folder for adding consequent pages
            if (CrackAccessibilityService.document == null && CrackAccessibilityService.pdfCopy == null) {
                CrackAccessibilityService.document = new Document();
                CrackAccessibilityService.pdfCopy = new PdfCopy(document, new FileOutputStream
                        (Environment.getExternalStorageDirectory() + MainActivity.destFolder + "Converted" + "/" + fileName + "-ConvertedPDF" + ".pdf"));
            }
            CrackAccessibilityService.document.open();
            PdfReader reader = new PdfReader(filepath.toString());
            CrackAccessibilityService.pdfCopy.addPage(pdfCopy.getImportedPage(reader, 1));
            filepath.delete();
            Log.e("++MERGE DONE++", "MERGE DONE");
        } catch (Exception e) {
            filepath.delete();
            CrackAccessibilityService.document.close();
        }
    }


    // adding page number and app name to converted pdf
    public void addPagenumber(Rectangle rect, PdfWriter writer, String pageNumber) {
        Log.e("++INSIDE ADD PAGE NUMBER++", "INSIDE ADD PAGE NUMBER");
        Phrase phrase;
        if (pageNumber.contentEquals("1") || pageNumber.contentEquals("50") || pageNumber.contentEquals("100")) {
            phrase = new Phrase(pageNumber + "                       [KindleCracker]");
        } else {
            phrase = new Phrase(pageNumber);
        }
        float position = ((rect.getRight() + rect.getLeft()) / 2);
        ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_BOTTOM, phrase, position,
                rect.getBottom() + 25, 0);
        Log.e("++PAGE NUMBER ADDED++", "PAGE NUMBER ADDED");
    }

    // to continue to convert PDFs which is already in converted folder
    public void continuePDFmerge(String name, String fileName) {
        File filepath = new File(Environment.getExternalStorageDirectory() + MainActivity.destFolder + name + ".pdf");
        File oldFile = new File(Environment.getExternalStorageDirectory() + MainActivity.destFolder + "Converted/" + fileName + "-ConvertedPDF" + ".pdf");
        try {
            if (CrackAccessibilityService.document == null && CrackAccessibilityService.pdfCopy == null) {
                CrackAccessibilityService.document = new Document();
                CrackAccessibilityService.pdfCopy = new PdfCopy(CrackAccessibilityService.document, new FileOutputStream
                        (Environment.getExternalStorageDirectory() + MainActivity.destFolder + "Converted/" + "Continued(Please_Rename_to _original_file_name)" + ".pdf"));
                CrackAccessibilityService.document.open();
                pdfReader = new PdfReader(oldFile.toString());
                existingFilePages = pdfReader.getNumberOfPages();

                for (int page = 1; page <= existingFilePages; page++) {
                    CrackAccessibilityService.pdfCopy.addPage(pdfCopy.getImportedPage(pdfReader, page));
                }
                // first file should be added first method call
                CrackAccessibilityService.pdfCopy.addPage(pdfCopy.getImportedPage(new PdfReader(filepath.toString()), 1));
                filepath.delete();
            } else {
                CrackAccessibilityService.document.open();
                PdfReader continuereader = new PdfReader(filepath.toString());
                CrackAccessibilityService.pdfCopy.addPage(pdfCopy.getImportedPage(continuereader, 1));
                filepath.delete();
            }
        } catch (Exception e) {
            CrackAccessibilityService.document.close();
        }
    }

    /**
     * Compress the size of converted pdf
     *
     * @param name pdf file name
     * @param rate compression rate in %
     */

    public void compressPdf(String name, int rate) {

        try {
            File file = new File(Environment.getExternalStorageDirectory() + MainActivity.destFolder + name + ".pdf");
            PdfReader reader = new PdfReader(file.toString());
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;

            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isStream())
                    continue;
                stream = (PRStream) object;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    PdfImageObject image = new PdfImageObject(stream);
                    byte[] imageBytes = image.getImageAsBytes();
                    Bitmap bmp;
                    bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bmp == null) continue;
                    int width = bmp.getWidth();
                    int height = bmp.getHeight();
                    Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas outCanvas = new Canvas(outBitmap);
                    outCanvas.drawBitmap(bmp, 0f, 0f, null);
                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                    outBitmap.compress(Bitmap.CompressFormat.JPEG, rate, imgBytes);
                    stream.clear();
                    stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                    stream.put(PdfName.TYPE, PdfName.XOBJECT);
                    stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                    stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                    stream.put(PdfName.WIDTH, new PdfNumber(width));
                    stream.put(PdfName.HEIGHT, new PdfNumber(height));
                    stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                }
            }
            reader.removeUnusedObjects();
            file.delete();
            // Save altered PDF
            PdfStamper stamper = new PdfStamper(reader,
                    new FileOutputStream(Environment.getExternalStorageDirectory() + MainActivity.destFolder + name + ".pdf"));
            stamper.setFullCompression();
            stamper.close();
            reader.close();
        } catch (Exception e) {
        }
    }

    public void showNotification(String message) {
        androidx.core.app.NotificationCompat.Builder notify = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Kindle Cracker")
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setAutoCancel(false);
        NotificationManagerCompat notificationmanager = NotificationManagerCompat.from(getApplicationContext());
        notificationmanager.notify(10101, notify.build());
    }

    // Creating Foreground service notification
    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationChannel channel = null;
        channel = new NotificationChannel("screencapture", "Screen Capture", NotificationManager.IMPORTANCE_LOW);
        getApplicationContext().getSystemService(NotificationManager.class).createNotificationChannel(channel);
        return new Notification.Builder(this, "screencapture")
                .setContentTitle("Screen Capture")
                .setContentText("Screen capture is running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }
}