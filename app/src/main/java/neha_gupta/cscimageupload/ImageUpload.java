package neha_gupta.cscimageupload;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ImageUpload extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO  = 1;
    String mCurrentPhotoPath;
    ImageView ivPreview;
    public static Float Latitude, Longitude;
    public static String size,height,width;
    public static final int RequestPermissionCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        requestPermission();

        Button b = (Button)findViewById(R.id.Button01);
        Button btnCompress=(Button)findViewById(R.id.btnCompress);

        ivPreview=(ImageView) findViewById(R.id.ImageView01);

        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {

                    boolean permissions = checkPermission();

                    if (permissions == true) {
                        ImageCapture();
                    }
                    else
                    {
                        Toast.makeText(ImageUpload.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        finish();
                    }

                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CompressActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void ImageCapture()
    {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }

        builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle("Alert").setMessage("Before take image pls ensure, your 'Save Location Info' is enabled in your camera settings.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                            File photoFile = null;

                            try {
                                photoFile = createImageFile();
                                if (photoFile != null) {
                                    Uri photoURI = FileProvider.getUriForFile(ImageUpload.this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                                }
                            } catch (IOException ex) {
                                return;
                            }

                        }
                    }
                }).setNegativeButton("No", null).show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

                boolean LatLong = CheckLatLng();

                if (LatLong == true) {
                    Uri imageUri = Uri.parse(mCurrentPhotoPath);
                    File file = new File(imageUri.getPath());
                    try {
                        InputStream ims = new FileInputStream(file);
                        ivPreview.setImageBitmap(BitmapFactory.decodeStream(ims));
                    } catch (FileNotFoundException e) {
                        return;
                    }

                    MediaScannerConnection.scanFile(ImageUpload.this,
                            new String[]{imageUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                }
                            });
                    Toast.makeText(ImageUpload.this, "Image Saved Successfully.", Toast.LENGTH_LONG).show();
                } else {
                    Uri fimageUri = Uri.parse(mCurrentPhotoPath);
                    File fdelete = new File(fimageUri.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            mCurrentPhotoPath = "";
                            ivPreview.setImageResource(0);
                            Toast.makeText(ImageUpload.this, "Image has not been captured as per the guidelines. Plz read the guidelines and try again..", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ImageUpload.this, "Image has not been captured as per the guidelines. Plz read the guidelines and try again..", Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(ImageUpload.this, "Image has not been captured as per the guidelines. Plz read the guidelines and try again..", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean CheckLatLng()
    {
        try
        {
            Uri imageUri = Uri.parse(mCurrentPhotoPath);
            String Path = imageUri.getPath();

            File file = new File(Path);
            Long length=file.length();
            Float KBSize=Float.valueOf(length)/1024;
            double KBRound=Math.round(KBSize*100.0)/100.0;
            if (KBSize < 1024)
            {
                size = String.valueOf(KBRound)+" KB";
            }
            else
            {
                Float MBSize=Float.valueOf(KBSize)/1024;
                double MBRound=Math.round(MBSize*100.0)/100.0;
                size = String.valueOf(MBRound)+" MB";
            }

            ExifInterface exif = new ExifInterface(Path);

            String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            height= exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            width= exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);

            if((LATITUDE !=null) && (LATITUDE_REF !=null) && (LONGITUDE != null) && (LONGITUDE_REF !=null))
            {
                if(LATITUDE_REF.equals("N")){
                    Latitude = convertToDegree(LATITUDE);
                }
                else{
                    Latitude = 0 - convertToDegree(LATITUDE);
                }

                if(LONGITUDE_REF.equals("E")){
                    Longitude = convertToDegree(LONGITUDE);
                }
                else{
                    Longitude = 0 - convertToDegree(LONGITUDE);
                }

                return true;
            }
            else
            {
                return false;
            }

        } catch (IOException ex) {
            return false;
        }
    }

    private Float convertToDegree(String stringDMS){
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    };

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("ddMMyyy_HHmmss").format(new Date());
        String imageFileName = "CSC_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(imageFileName,".jpg",  storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(ImageUpload.this, new String[]
                {READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean ReadExternalStoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean WriteExternalStoragePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean CameraPermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    if (CameraPermission && ReadExternalStoragePermission && WriteExternalStoragePermission) {
                        Toast.makeText(ImageUpload.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(ImageUpload.this,"Permission Denied",Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int FirstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        int SecondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int ThirdPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
                ThirdPermissionResult == PackageManager.PERMISSION_GRANTED ;
    }

    @Override
    public void onBackPressed() {
        close();
    }

    public void close()
    {
        try
        {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }

            builder.setIcon(android.R.drawable.ic_dialog_alert).setTitle("Exit").setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();}
                    }).setNegativeButton("No", null).show();
        }
        catch (Exception e){}
    }
}
