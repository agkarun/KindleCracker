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
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

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

public class MyAccessibility extends AccessibilityService {
    private String fileName;
    private int i=1,x,y,totalPages,time,fromPage,oldFilePages;
    private static double compression= 30*0.09;
    private static Document document;
    private static PdfCopy copy;
    private boolean continueFromPage;
    private static boolean compress=false;
    private static int rate;
    private PdfReader reader;
    MyAccessibility myAccessibility;
    Display display;
    MediaProjection projection;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
    }

    public MyAccessibility(){
    }

    public MyAccessibility(Display display, MediaProjection projection){
        this.display=display;
        this.projection=projection;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("++++++On Start+++++++","1");
        getValues(intent);
        touch(x,y);
        new AsyncCracker().execute();
        return START_STICKY;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }


    @Override
    public void onDestroy() {
        if (MyAccessibility.document!=null){
            MyAccessibility.document.close();
            MyAccessibility.document=null;
            MyAccessibility.copy=null;
        }
        super.onDestroy();
    }


    public void getValues(Intent intent){

        try{
            fromPage=intent.getExtras().getInt("fromPage");
            compress=intent.getExtras().getBoolean("compress");
            rate=intent.getExtras().getInt("compressRate");
            Log.e("From Page",""+fromPage);
            if(compress)
            {
                if(rate>100)
                {
                    rate=100;
                }
            }
            if(fromPage==-1&&!compress){
                continueFromPage=false;
                compress=false;
            }

            if(fromPage==-1&&compress){
                continueFromPage=false;
                compress=true;
            }

            else if(fromPage>=1&&!compress) {
                continueFromPage=true;
                compress=false;
                i=fromPage;
            }

            else if(fromPage>=1&&compress) {
                continueFromPage=true;
                compress=true;
                i=fromPage;
            }
            myAccessibility = new MyAccessibility();
            fileName=intent.getStringExtra("file");
            x=intent.getExtras().getInt("x");
            y=intent.getExtras().getInt("y");
            time=intent.getExtras().getInt("time");
            totalPages=intent.getExtras().getInt("totalPages");
            Log.e("X",""+x);
            Log.e("Y",""+y);
            Log.e("Total Pages",""+totalPages);
        }
        catch (Exception e){

        }
    }

   public class AsyncCracker extends AsyncTask<String,Void,String>{
       @Override
       protected void onPreExecute() {
           super.onPreExecute();
           Toast.makeText(getApplicationContext(),fileName+" Will be started to convert within "+time+" Seconds",Toast.LENGTH_SHORT).show();
           }

       @Override
       protected String doInBackground(String... strings) {
           try {
               Log.e("Thread","");
               Thread.sleep(time*1000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }

           //if totalpages=0 the loop will run infinitly
           if(totalPages==0&&!continueFromPage){
               try{
                   while (true){
                       myAccessibility.takeScreenshot(String.valueOf(i));
//                       myAccessibility.convertTopdf(String.valueOf(i));
//                       myAccessibility.mergePdf(String.valueOf(i),fileName);
                       Log.e("Touch","");
//                       Toast.makeText(getApplicationContext(),"1",Toast.LENGTH_SHORT).show();
//                       touch(x,y);
                       i++;
                   }
               }
               catch (Exception e) {
                   if(document!=null) document.close();
                   e.printStackTrace();
               }

           }

          //if number of pages is defined this loop will run
           else if(totalPages>=i&&!continueFromPage) {
               try{
                   while (totalPages>=i){
                       myAccessibility.takeScreenshot(String.valueOf(i));
//                       myAccessibility.convertTopdf(String.valueOf(i));
//                       myAccessibility.mergePdf(String.valueOf(i),fileName);
                       Log.e("Touch","");
//                       Toast.makeText(getApplicationContext(),"2",Toast.LENGTH_SHORT).show();
//                       touch(x,y);
                       i++;
                   }
               }
               catch (Exception e) {
                   if(document!=null) document.close();
                   e.printStackTrace();
//                   Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_SHORT).show();
               }
           }


           //if user wants to continue from old pdf file this code will be executed
           else if(continueFromPage) {

               //if totalpages=0 the loop will run infinitly
               if (totalPages == 0 && continueFromPage) {
                   try {
                       while (true) {
                           myAccessibility.takeScreenshot(String.valueOf(i));
//                           myAccessibility.convertTopdf(String.valueOf(i));
//                           myAccessibility.continuePDFmerge(String.valueOf(i), fileName);
                           Log.e("Touch","");
//                           Toast.makeText(getApplicationContext(),"3",Toast.LENGTH_SHORT).show();
//                           touch(x, y);
                           i++;
                       }
                   } catch (Exception e) {
                       document.close();
                       e.printStackTrace();
//                       Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_SHORT).show();
                   }

               }

               //if number of pages is defined this loop will run
               else if (totalPages >= i && continueFromPage) {
                   try {
                       while (totalPages >= i) {
                           myAccessibility.takeScreenshot(String.valueOf(i));
//                           myAccessibility.convertTopdf(String.valueOf(i));
//                           myAccessibility.continuePDFmerge(String.valueOf(i), fileName);
                           Log.e("Touch","");
//                           Toast.makeText(getApplicationContext(),"4",Toast.LENGTH_SHORT).show();
//                           touch(x, y);
                           i++;
                       }

                   }
                   catch (Exception e) {
                       if(MyAccessibility.document!=null)  MyAccessibility.document.close();
                       e.printStackTrace();
//                       Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_SHORT).show();
                   }
               }
           }

           return null;
       }


       @Override
       protected void onPostExecute(String s)
       {
           showNotification(fileName+" Successfully Converted to PDF");
           Toast.makeText(getApplicationContext(),fileName+" Successfully Converted to PDF",Toast.LENGTH_LONG).show();
           i=1;
           if(MyAccessibility.document!=null) MyAccessibility.document.close();
           MyAccessibility.document=null;
           MyAccessibility.copy=null;
           stopSelf();
       }
   }

    /**
     * the touch method used for automate the touch in device screen with given coordinate points on the screen
     * @param x for x coordinate
     * @param y for y coordinate
     * @return reurns boolian
     */
@TargetApi(24)
    public void touch(int x, int y)
    {
        Log.e("Completed 1","");
        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path p = new Path();
        Point position = new Point(x,y);
        p.moveTo(position.x,position.y);
        builder.addStroke(new GestureDescription.StrokeDescription(p,10L,200L));
        GestureDescription gesture = builder.build();
        boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.e("Completed 2","");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.e("Not Completed","");
                super.onCancelled(gestureDescription);
            }
        },null);
//        Toast.makeText(CrackService.this,""+isDispatched,Toast.LENGTH_SHORT).show();
//        return isDispatched;

    }

    public void takeScreenshot(final String fileName) {
        Log.e("++++","1");
        final DisplayMetrics metrics = new DisplayMetrics();
        Log.e("+++++","2");
        MainActivity.display.getMetrics(metrics);
        Log.e("+++++","3");
        Point size = new Point();
        Log.e("+++++","4");
        MainActivity.display.getRealSize(size);
        Log.e("+++++","5");
        final int width = size.x;
        Log.e("+++++","6");
        final int height = size.y;
        Log.e("+++++","7");
        int density = metrics.densityDpi;
        Log.e("+++++","8");
        final ImageReader imageReader = ImageReader.newInstance(width,height, PixelFormat.RGBA_8888,2);
        Log.e("+++++","9");
        final Handler handler = new Handler(Looper.getMainLooper());
        Log.e("+++++","10");
        int flags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        Log.e("+++++","11");
        MainActivity.projection.createVirtualDisplay("screen-mirror",width,height,density,flags,imageReader.getSurface(),null,handler);
        Log.e("+++++","12");
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.e("+++++", "13");
                reader.setOnImageAvailableListener(null,handler);
                android.media.Image image = reader.acquireLatestImage();
                Log.e("+++++", "14");
                final android.media.Image.Plane[] planes = image.getPlanes();
                Log.e("+++++", "15");
                final ByteBuffer buffer = planes[0].getBuffer();
                Log.e("+++++", "16");
                int pixelStride = planes[0].getPixelStride();
                Log.e("+++++", "17");
                int rowStride = planes[0].getRowStride();
                Log.e("+++++", "18");
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;
                Log.e("+++++", "19");
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride),
                        metrics.heightPixels, Bitmap.Config.ARGB_8888);
                Log.e("+++++", "20");
                bmp.copyPixelsFromBuffer(buffer);
                image.close();
                reader.close();
                Log.e("+++++", "21");
                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());
                bmp.recycle();
                Log.e("+++++", "22");
                File file = new File(Environment.getExternalStorageDirectory() + "/KindleCracker/" + fileName + ".png");
                Log.e("+++++", "23");
                try {
                    FileOutputStream out = new FileOutputStream(file.getAbsolutePath());
                    realSizeBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },handler);
    }



    /**
     * this method used for convert image file to pdf
     * @param name
     */
    public  void convertTopdf(String name) throws Exception{
        Document doc = new Document(PageSize.getRectangle("LEGAL"),0,0,0,0);
        doc.setMargins(0,0,0,0);
        Rectangle rect = doc.getPageSize();
        try {
            PdfWriter writer = PdfWriter.getInstance(doc,new FileOutputStream(Environment
                    .getExternalStorageDirectory()+"/"+"KindleCracker"+"/"+name+".pdf"));
            doc.open();
            File file = new File(Environment.getExternalStorageDirectory()+"/KindleCracker/"+name+".png");
            Image img = Image.getInstance(String.valueOf(file.toURI()));
            img.setCompressionLevel((int)compression);
            img.setBorder(Rectangle.BOX);
            img.setBorderWidth(0);
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bmap=BitmapFactory.decodeFile(file.toString(),options);
            img.scaleToFit(doc.getPageSize().getWidth(),doc.getPageSize().getHeight());
            img.setAbsolutePosition((rect.getWidth()-img.getScaledWidth())/2,
                    (rect.getHeight()-img.getScaledHeight())/2);

            addPagenumber(rect,writer,String.valueOf(name));

            doc.add(img);
            doc.close();
            if (compress) {
                new MyAccessibility().compressPdf(name,rate);
            }
            file.delete();


        } catch (Exception e) {

            if (document !=null)
            {
                document.close();
            }
            else {
                Log.e("This Error:",""+e);
            }
        }
    }

    /**
     * this method will merge two pdf's into one
     * @param name exsisting pdf file name
     * @param fileName Converted file name
     * @throws IOException
     * @throws DocumentException
     */


    //merge pdf for unknown number of pages
    public void mergePdf(String name,String fileName) throws IOException, DocumentException {
        File filepath= new File(Environment.getExternalStorageDirectory()+"/KindleCracker/"+name+".pdf");
        try{
            if(MyAccessibility.document==null&& MyAccessibility.copy==null){
                MyAccessibility.document=new Document();
                MyAccessibility.copy= new PdfCopy(document,new FileOutputStream
                        (Environment.getExternalStorageDirectory()+"/KindleCracker/"+"Converted"+"/"+fileName+"-ConvertedPDF"+".pdf"));
            }
            MyAccessibility.document.open();
            PdfReader reader = new PdfReader(filepath.toString());
            MyAccessibility.copy.addPage(copy.getImportedPage(reader,1));
            filepath.delete();
        }
        catch (Exception e){
            filepath.delete();
            MyAccessibility.document.close();
        }
    }


    //adding page number to pdf and app name
    public void addPagenumber(Rectangle rect,PdfWriter writer,String pageNumber){
        Phrase phrase;
        if (pageNumber.contentEquals("1")||pageNumber.contentEquals("50")||pageNumber.contentEquals("100"))
        {
            phrase = new Phrase(pageNumber+"                       [KindleCracker]");
        }
        else {
            phrase = new Phrase(pageNumber);
        }
        float position = ((rect.getRight()+rect.getLeft())/2);
        ColumnText.showTextAligned(writer.getDirectContent(),Element.ALIGN_BOTTOM,phrase,position,
                rect.getBottom()+25,0);
    }

    //to continue convert pdf's with already converted folder
    public void continuePDFmerge(String name,String fileName){
        File filepath= new File(Environment.getExternalStorageDirectory()+"/KindleCracker/"+name+".pdf");
        File oldFile=new File(Environment.getExternalStorageDirectory()+"/KindleCracker/Converted/"+fileName+"-ConvertedPDF"+".pdf");
        try {
            if (MyAccessibility.document == null && MyAccessibility.copy == null) {
                MyAccessibility.document = new Document();
                MyAccessibility.copy = new PdfCopy(MyAccessibility.document, new FileOutputStream
                        (Environment.getExternalStorageDirectory() + "/KindleCracker/" + "Converted" + "/" +"Continued(Please_Rename_to _original_file_name)"+ ".pdf"));
                MyAccessibility.document.open();
                reader= new PdfReader(oldFile.toString());
                oldFilePages=reader.getNumberOfPages();

                for(int page=1;page<=oldFilePages;page++){
                    MyAccessibility.copy.addPage(copy.getImportedPage(reader,page));
                }
                //first file shoud be added first method call
                MyAccessibility.copy.addPage(copy.getImportedPage(new PdfReader(filepath.toString()),1));
                filepath.delete();
            }

            else{
                MyAccessibility.document.open();
                PdfReader continuereader = new PdfReader(filepath.toString());
                MyAccessibility.copy.addPage(copy.getImportedPage(continuereader,1));
                filepath.delete();
            }
        }

        catch (Exception e){
            MyAccessibility.document.close();
        }
    }

    /**
     * Compress the size of converted pdf
     * @param name pdf file name
     * @param rate compression rate in %
     */

    public void compressPdf(String name,int rate){

        try{
            File file = new File(Environment.getExternalStorageDirectory()+"/"+"KindleCracker"+"/"+name+".pdf");
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
                    Log.e("CompRate",""+rate);
                    outBitmap.compress(Bitmap.CompressFormat.JPEG,rate, imgBytes);
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
                    new FileOutputStream(Environment.getExternalStorageDirectory()+"/"+"KindleCracker"+"/"+name+".pdf"));
            stamper.setFullCompression();
            stamper.close();
            reader.close();
        }
        catch (Exception e){
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
        notificationmanager.notify(10101,notify.build());

    }

    }
