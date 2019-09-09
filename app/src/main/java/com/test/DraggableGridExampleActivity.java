/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;

public class DraggableGridExampleActivity extends AppCompatActivity {
    private static final String FRAGMENT_TAG_DATA_PROVIDER = "data provider";
    private static final String FRAGMENT_LIST_VIEW = "list view";
    ArrayList<Bitmap> chunkedImages;
    int chunkNumbers = 9;
    private final int RESULT_LOAD_IMAGE = 1;
    Uri selectedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        ///storage/sdcard0/DCIM/Camera/IMG_20190115_101456_1.jpg
        Bitmap bitmap  = BitmapFactory.decodeResource(getResources(),R.drawable.dog);
       // Bitmap bitmap = BitmapFactory.decodeFile("/storage/sdcard0/DCIM/Camera/IMG_20190115_101456_1.jpg");
        splitImage(bitmap, chunkNumbers);
       // alertDialogForCameraImage();
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(new ExampleDataProviderFragment(), FRAGMENT_TAG_DATA_PROVIDER)
                    .commit();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DraggableGridExampleFragment(), FRAGMENT_LIST_VIEW)
                    .commit();
        }
    }

    public AbstractDataProvider getDataProvider() {
       // final Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_DATA_PROVIDER);
        if (chunkedImages == null || chunkedImages.size() <= 0){
            alertDialogForCameraImage();
        }
        Collections.shuffle(chunkedImages);
        return new ImageProvider(chunkedImages);
    }
    void pickImageFromGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//      startActivityForResult(pickPhoto , 0);
        startActivityForResult(pickPhoto, RESULT_LOAD_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {

            case RESULT_LOAD_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    System.out.println("Source Path : "+picturePath);
                    cursor.close();
                    Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                    splitImage(bitmap, chunkNumbers);
                }
        }
    }
    private void splitImage(Bitmap bitmap, int chunkNumbers) {

        //For the number of rows and columns of the grid to be displayed
        int rows, cols;

        //For height and width of the small image chunks
        int chunkHeight, chunkWidth;

        //To store all the small image chunks in bitmap format in this list
        chunkedImages = new ArrayList<Bitmap>(chunkNumbers);

        //Getting the scaled bitmap of the source image
//        BitmapDrawable drawable = (BitmapDrawable) image.getDrawable();
//        image.setImageResource(R.drawable.ic_chevron_right_black_24dp);
//        Bitmap bitmap = drawable.getBitmap();

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        rows = cols = (int) Math.sqrt(chunkNumbers);
        chunkHeight = bitmap.getHeight() / rows;
        chunkWidth = bitmap.getWidth() / cols;
        //xCoord and yCoord are the pixel positions of the image chunks
        int yCoord = 0;
        for (int x = 0; x < rows; x++) {
            int xCoord = 0;
            for (int y = 0; y < cols; y++) {
                chunkedImages.add(Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight));
                xCoord += chunkWidth;
            }
            yCoord += chunkHeight;
        }
        // mergeImage(chunkedImages);

        // Function of merge the chunks images(after image divided in pieces then i can call this function to combine and merge the image as one)
        //


    }
    public void alertDialogForCameraImage() {
        AlertDialog.Builder adb = new AlertDialog.Builder(DraggableGridExampleActivity.this);
        adb.setTitle("Pick Image From Gallery: ");
        adb.setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                pickImageFromGallery();

            }
        });
        adb.show();
    }
}
