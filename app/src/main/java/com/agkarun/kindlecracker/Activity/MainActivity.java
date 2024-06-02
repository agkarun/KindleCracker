package com.agkarun.kindlecracker.Activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.agkarun.kindlecracker.R;
import com.agkarun.kindlecracker.Service.MyAccessibility;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.io.File;


public class MainActivity extends AppCompatActivity {

    private TextInputLayout continueLay;
    private TextInputLayout compressLay;
    private TextInputEditText xCoord, yCoord, file, pages, prepTime, pageContinue, compressPDF;
    private CheckBox continueChkbox, compressCheckbox;
    private Button startButton, stopBtn;
    private int x, y, time, totalPages, fromPage, compress;
    public static final String destFolder = "/Documents/KindleCracker/";
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int SCREENCAPTURE_PERMISSION_CODE = 2;
    private File mergedFolder;
    private String fileName;
    private View snackView;
    private SharedPreferences preferences;
    private boolean isfromPage, isCompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Initializing variables while starting
        xCoord = findViewById(R.id.Xcoord);
        yCoord = findViewById(R.id.Ycoord);
        file = findViewById(R.id.filename);
        pages = findViewById(R.id.pages);
        prepTime = findViewById(R.id.preptime);
        compressPDF = findViewById(R.id.compresspdf);
        continueChkbox = findViewById(R.id.continuechkbox);
        compressCheckbox = findViewById(R.id.compresschkbox);
        pageContinue = findViewById(R.id.continuepage);
        startButton = findViewById(R.id.convertbtn);
        stopBtn = findViewById(R.id.stopConvertbtn);
        continueLay = findViewById(R.id.continuetextLay);
        compressLay = findViewById(R.id.compresspdfLay);
        snackView = findViewById(R.id.rootView);
//        Checking if EULA is already accepted or not
        preferences = getSharedPreferences("EULA", MODE_PRIVATE);

        if (preferences.getBoolean("NOTAGREED", true)) {
            showEULA();
        }
//        Requesting Storage and Screen Record Permission
        requestStoragePermission();

//        Checkbox used to make visible text field for getting from page number
        continueChkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueChkbox.isChecked()) {
                    continueLay.setVisibility(View.VISIBLE);
                } else {
                    continueLay.setVisibility(View.GONE);
                }
            }
        });

//        Checkbox used to make visible text field for getting from page number
        compressCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (compressCheckbox.isChecked()) {
                    compressLay.setVisibility(View.VISIBLE);
                } else {
                    compressLay.setVisibility(View.GONE);
                }
            }
        });

//        Starting the pdf converting service
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getValues()) {
//                    Start converting from already converted PDF file
                    if (continueChkbox.isChecked() && !compressCheckbox.isChecked()) {
                        if (pageContinue.getText().toString().trim().length() <= 0 ||
                                pageContinue.getText().toString().trim().contentEquals("0")) {
                            Snackbar.make(snackView, "All fields are mandatory don't leave fields empty", Snackbar.LENGTH_LONG).show();
                        } else if (pageContinue.getText().toString().trim().length() > 0 &&
                                !pageContinue.getText().toString().trim().contentEquals("0")) {
                            fromPage = Integer.parseInt(pageContinue.getText().toString().trim());
                            startConvertingService(true, false);
                        }
                    }
//                    Start converting new PDF
                    if (!continueChkbox.isChecked() && compressCheckbox.isChecked()) {
                        if (compressPDF.getText().toString().trim().length() <= 0 ||
                                compressPDF.getText().toString().trim().contentEquals("0")) {
                            Snackbar.make(snackView, "All fields are mandatory don't leave fields empty", Snackbar.LENGTH_LONG).show();
                        } else if (compressPDF.getText().toString().trim().length() > 0 &&
                                !compressPDF.getText().toString().trim().contentEquals("0")) {
                            compress = Integer.parseInt(compressPDF.getText().toString().trim());
                            startConvertingService(false, true);
                        }
                    }

//                    Start converting from already converted PDF file
                    if (continueChkbox.isChecked() && compressCheckbox.isChecked()) {
                        if (compressPDF.getText().toString().trim().length() <= 0 ||
                                compressPDF.getText().toString().trim().contentEquals("0") ||
                                pageContinue.getText().toString().trim().length() <= 0 ||
                                pageContinue.getText().toString().trim().contentEquals("0")) {
                            Snackbar.make(snackView, "All fields are mandatory don't leave fields empty", Snackbar.LENGTH_LONG).show();
                        } else if (compressPDF.getText().toString().trim().length() > 0 &&
                                !compressPDF.getText().toString().trim().contentEquals("0") &&
                                pageContinue.getText().toString().trim().length() > 0 &&
                                !pageContinue.getText().toString().trim().contentEquals("0")) {
                            fromPage = Integer.parseInt(pageContinue.getText().toString().trim());
                            compress = Integer.parseInt(compressPDF.getText().toString().trim());
                            startConvertingService(true, true);
                        }
                    }
//                    Start converting new PDF file
                    else if (!continueChkbox.isChecked() && !compressCheckbox.isChecked()) {
                        startConvertingService(false, false);
                    }
                }
            }
        });

//        Stop pdf converting
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopServiceintent = new Intent(MainActivity.this, MyAccessibility.class);
                stopServiceintent.setAction("STOP_SERVICE");
                startService(stopServiceintent);
                Snackbar.make(snackView, "Converting Stopped...", Snackbar.LENGTH_LONG).show();
            }
        });

    }

//    Handling Screen record permission while user Accepted/ Rejected
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREENCAPTURE_PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                Intent serviceIntent = new Intent(getApplicationContext(), MyAccessibility.class);
                serviceIntent.putExtra("resultCode",resultCode);
                serviceIntent.putExtra("resultCode",resultCode);
                serviceIntent.putExtra("Intent",data);
                startService(initializeIntent(serviceIntent,isfromPage,isCompress));
            }
        }
//        Permission Denied
        if (requestCode == SCREENCAPTURE_PERMISSION_CODE && resultCode == 0) {
            Toast.makeText(this, "MEDIA PROJECTION permission needed to Capture your Display", Toast.LENGTH_LONG).show();
        }
    }

//    This method will be called after storage permission granted or denied by the user
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,
                                           int[] grantResults) {

        if (requestCode == STORAGE_PERMISSION_CODE) {
//            Request for storage permission
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Permission has been granted.
            } else {
//                Permission request was denied.
                Toast.makeText(this,"Storage permission needed to Save PDF files",Toast.LENGTH_LONG).show();
                finish();
                moveTaskToBack(true);
            }
        }
    }

    private void requestStoragePermission() {
//        Permission has not been granted for previous request and must be requested again.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
//        If this is the first time requesting permission
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

//    getting values from user
    public boolean getValues() {
        boolean values=false;
        if (xCoord.getText().toString().trim().length() > 0 &&
                yCoord.getText().toString().trim().length() > 0 &&
                pages.getText().toString().trim().length() > 0 &&
                prepTime.getText().toString().trim().length() > 0 &&
                file.getText().toString().trim().length() > 0)
        {
            try {
                x = Integer.parseInt(xCoord.getText().toString().trim());
                y = Integer.parseInt(yCoord.getText().toString().trim());
                totalPages = Integer.parseInt(pages.getText().toString().trim());
                time = Integer.parseInt(prepTime.getText().toString().trim());
                fileName = file.getText().toString().trim();
                values=true;
            } catch (Exception e) {
                Snackbar.make(snackView,"Some field contains invalid value",Snackbar.LENGTH_LONG).show();
            }

        }
        else {
            Snackbar.make(snackView,"All fields are mandatory  don't leave fields empty...",Snackbar.LENGTH_LONG).show();
        }

        return values;
    }

    /**
     * Starting PDF converting service
     * @param fromPage coverting from already having pdf
     * @param compress compress the pdf file
     */
    public void startConvertingService(boolean fromPage,boolean compress){
        isfromPage = fromPage;
        isCompress = compress;
//        Getting screen record permission
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(manager.createScreenCaptureIntent(),SCREENCAPTURE_PERMISSION_CODE);
        try{
            File sShotfolder = new File(Environment.getExternalStorageDirectory().getPath()+destFolder);
            mergedFolder= new File(Environment.getExternalStorageDirectory().getPath()+destFolder+"Converted/");
            if(!sShotfolder.exists()&&!mergedFolder.exists())
            {
                sShotfolder.mkdir();
                mergedFolder.mkdir();
            }
        }
        catch (Exception e){
            Log.e("++STORAGE++",e.toString());
            e.printStackTrace();
        }
    }
    public Intent initializeIntent(Intent intent, boolean fromPage, boolean compress){
        if (fromPage&&!compress){
            intent.putExtra("fromPage",this.fromPage);
            intent.putExtra("compress",false);
        }
        else if (!fromPage&&compress){
            intent.putExtra("fromPage",-1);
            intent.putExtra("compress",true);
            intent.putExtra("compressRate",this.compress);
        }
        else if (fromPage&&compress){
            intent.putExtra("fromPage",this.fromPage);
            intent.putExtra("compress",true);
            intent.putExtra("compressRate",this.compress);
        }
        else {
            intent.putExtra("fromPage",-1);
            intent.putExtra("compress",false);
        }
        intent.putExtra("file", fileName);
        intent.putExtra("x",x);
        intent.putExtra("y",y);
        intent.putExtra("totalPages", totalPages);
        intent.putExtra("time",time);
        return intent;
    }
    public void showEULA(){
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Click AGREE button to Agree the licence agreement.")
                .setTitle("End User Licence Agreement")
                .setCancelable(false)
                .setNeutralButton("View Licence", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/1tzLFN9Abvlkm6zvt2a6_YUUIGczAER7x/view?usp=sharing"));
                        startActivity(browserIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        moveTaskToBack(true);
                    }
                })
                .setPositiveButton("AGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences.edit().putBoolean("NOTAGREED",false).apply();
                    }
                });
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.guid:
                startActivity(new Intent(this,GuidActivity.class));
                return true;
            case R.id.about:
                startActivity(new Intent(this,AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
