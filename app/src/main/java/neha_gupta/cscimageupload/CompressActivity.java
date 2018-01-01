package neha_gupta.cscimageupload;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CompressActivity extends AppCompatActivity {

    Button btnChoose,btnCompress;
    ImageView imgPreview;
    private static final int GALLERY_CODE = 201;
    private static Uri mImageCaptureUri;
    Bitmap bitmap1;
    ByteArrayOutputStream bytearrayoutputstream;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compress);

        btnChoose=(Button)findViewById(R.id.btnChoose);
        btnCompress=(Button)findViewById(R.id.btnCompress);
        imgPreview=(ImageView) findViewById(R.id.imgPreview);

        bytearrayoutputstream = new ByteArrayOutputStream();

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, GALLERY_CODE);
            }
        });

        btnCompress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mImageCaptureUri !=null) {
                        InputStream imageStream = getContentResolver().openInputStream(mImageCaptureUri);
                        bitmap1 = BitmapFactory.decodeStream(imageStream);

                        String timeStamp = new SimpleDateFormat("ddMMyyy_HHmmss").format(new Date());
                        String imageFileName = "CSC_Compress_" + timeStamp + "_";
                        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

                        try {
                            FileOutputStream out = new FileOutputStream(image);
                            bitmap1 = Bitmap.createScaledBitmap(bitmap1, 500, 500, false);
                            bitmap1.compress(Bitmap.CompressFormat.JPEG, 20, out);

                            ExifInterface oldexif = new ExifInterface(filePath);
                            ExifInterface newexif = new ExifInterface(image.getAbsolutePath());

                            if (oldexif.getAttribute("GPSLatitude") != null) {
                                newexif.setAttribute("GPSLatitude", oldexif.getAttribute("GPSLatitude"));
                            }
                            if (oldexif.getAttribute("GPSLatitudeRef") != null) {
                                newexif.setAttribute("GPSLatitudeRef", oldexif.getAttribute("GPSLatitudeRef"));
                            }
                            if (oldexif.getAttribute("GPSLongitude") != null) {
                                newexif.setAttribute("GPSLongitude", oldexif.getAttribute("GPSLongitude"));
                            }
                            if (oldexif.getAttribute("GPSLatitudeRef") != null) {
                                newexif.setAttribute("GPSLongitudeRef", oldexif.getAttribute("GPSLongitudeRef"));
                            }

                            newexif.saveAttributes();

                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(CompressActivity.this, "Image Compress Successfully", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(CompressActivity.this, "Please Choose Image then Click on Compress Image Button.", Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && null != data) {

            mImageCaptureUri = data.getData();
            filePath = getPath(mImageCaptureUri);

            try {
                InputStream imageStream = getContentResolver().openInputStream(mImageCaptureUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                imgPreview.setImageBitmap(selectedImage);
            } catch (FileNotFoundException e) {
                return;
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), ImageUpload.class);
        startActivity(intent);
        finish();
    }
}
