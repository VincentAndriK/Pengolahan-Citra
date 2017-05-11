package com.pencit.tugaspengolahancitra;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;

public class MainActivity extends Activity {

    Bitmap bi = null;
    boolean isColored;

    LinearLayout view1;
    LinearLayout view2;

    boolean flag;

    private int SIZE = 256;
    // Red, Green, Blue
    private int NUMBER_OF_COLOURS = 3;

    public final int RED = 0;
    public final int GREEN = 1;
    public final int BLUE = 2;

    private int[][] colourBins;
    private volatile boolean loaded = false;
    private int maxY;

    private static final int LDPI = 0;
    private static final int MDPI = 1;
    private static final int TVDPI = 2;
    private static final int HDPI = 3;
    private static final int XHDPI = 4;

    float offset = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.pencitraan_layout);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if(metrics.densityDpi==metrics.DENSITY_LOW)
            offset = 0.75f;
        else if(metrics.densityDpi==metrics.DENSITY_MEDIUM)
            offset = 1f;
        else if(metrics.densityDpi==metrics.DENSITY_TV)
            offset = 1.33f;
        else if(metrics.densityDpi==metrics.DENSITY_HIGH)
            offset = 1.5f;
        else if(metrics.densityDpi==metrics.DENSITY_XHIGH)
            offset = 2f;

        Log.e("NIRAV",""+offset);

        colourBins = new int[NUMBER_OF_COLOURS][];
        for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
            colourBins[i] = new int[SIZE];
        }

        loaded = false;
/// UNTUK AMBIL GAMBAR
        Button upload = (Button) findViewById(R.id.btnSelect);
        upload.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (flag) {
                    view1.removeAllViews();
                    view2.removeAllViews();
                }
                Intent it = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(it, 101);
                flag = true;

            }
        });


/*///UNTUK Peningkatan Kontras linear
        Button btnKontras = (Button) findViewById(R.id.btnKon);
        btnKontras.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) findViewById(R.id.modifed_pic);
               img.setImageBitmap(adjustedContrast(bi,1000));
                isColored = true;
                view2 = (LinearLayout) findViewById(R.id.histogram2);
                view2.addView(new MyHistogram(getApplicationContext(),bi));
            }
        });
///UNTUK Equalization
        Button btnEqualizer = (Button)findViewById(R.id.btnEq);
        btnEqualizer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) findViewById(R.id.modifed_pic);
                img.setImageBitmap(histogram_equalize(bi));
                isColored = true;
                view2 = (LinearLayout) findViewById(R.id.histogram2);
                view2.addView(new MyHistogram(getApplicationContext(),bi));
            }
        });
/// UNTUK Median FILTER
        Button btnMedian = (Button)findViewById(R.id.btnFillter);
        btnMedian.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageView img = (ImageView) findViewById(R.id.modifed_pic);
                img.setImageBitmap(new MedianFilter(bi).applyFilter());
                isColored = true;
                view2 = (LinearLayout) findViewById(R.id.histogram2);
                view2.addView(new MyHistogram(getApplicationContext(),bi));
            }
        });
    */
    }

    protected void onActivityResult(int requestCode, int resultCode,Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {

            case 101:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturnedIntent.getData();
                    String filename = getRealPathFromURI(selectedImage);
                    bi = BitmapFactory.decodeFile(filename);
                /*
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bi.compress(Bitmap.CompressFormat.JPEG,10,out);
                bi = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));*/

                    if(bi!=null)
                    {
                        try {
                            new MyAsync().execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
        }
    }

    /*////PENINGKATAN KONTRAS
    private Bitmap adjustedContrast(Bitmap src, double value)
    {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap

        // create a mutable empty bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

        // create a canvas so that we can draw the bmOut Bitmap from source bitmap
        Canvas c = new Canvas();
        c.setBitmap(bmOut);

        // draw bitmap to bmOut from src bitmap so we can modify it
        c.drawBitmap(src, 0, 0, new Paint(Color.BLACK));

        // color information
        int A, R, G, B;
        int pixel;
        // get contrast value
        double contrast = Math.pow((100 + value) / 100, 2);

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                // apply filter contrast for every channel R, G, B
                R = Color.red(pixel);
                R = (int)(((((R / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(R < 0) { R = 0; }
                else if(R > 255) { R = 255; }

                G = Color.green(pixel);
                G = (int)(((((G / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(G < 0) { G = 0; }
                else if(G > 255) { G = 255; }

                B = Color.blue(pixel);
                B = (int)(((((B / 255.0) - 0.5) * contrast) + 0.5) * 255.0);
                if(B < 0) { B = 0; }
                else if(B > 255) { B = 255; }

                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }
        return bmOut;
    }
    */

    public String getRealPathFromURI(Uri contentUri) {
        Log.e("TEST", "GetRealPath : " + contentUri);

        try {
            if (contentUri.toString().contains("video")) {
                String[] proj = { MediaStore.Video.Media.DATA };
                Cursor cursor = managedQuery(contentUri, proj, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } else {
                String[] proj = { MediaStore.Images.Media.DATA };
                Cursor cursor = managedQuery(contentUri, proj, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    class MyAsync extends AsyncTask
    {
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            showDialog(0);
        }

        @Override
        protected Object doInBackground(Object... params) {
            // TODO Auto-generated method stub

            try {
                load(bi);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            ImageView img = (ImageView) findViewById(R.id.original_pic);

            img.setImageBitmap(bi);
            dismissDialog(0);
            isColored = true;
            //view1 = (LinearLayout) findViewById(R.id.histogram1);
           // view1.addView(new MyHistogram(getApplicationContext(),bi));
        }

    }

    public void load(Bitmap bi) throws IOException {

        if (bi != null) {
            // Reset all the bins
            for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
                for (int j = 0; j < SIZE; j++) {
                    colourBins[i][j] = 0;
                }
            }

            for (int x = 0; x < bi.getWidth(); x++) {
                for (int y = 0; y < bi.getHeight(); y++) {

                    int pixel = bi.getPixel(x, y);

                    colourBins[RED][Color.red(pixel)]++;
                    colourBins[GREEN][Color.green(pixel)]++;
                    colourBins[BLUE][Color.blue(pixel)]++;
                }
            }

            maxY = 0;

            for (int i = 0; i < NUMBER_OF_COLOURS; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (maxY < colourBins[i][j]) {
                        maxY = colourBins[i][j];
                    }
                }
            }
            loaded = true;
        } else {
            loaded = false;
        }
    }

    /*
    class MyHistogram extends View {

        public MyHistogram(Context context, Bitmap bi) {
            super(context);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            if (loaded) {
                canvas.drawColor(Color.GRAY);

                Log.e("NIRAV", "Height : " + getHeight() + ", Width : "
                        + getWidth());

                int xInterval = (int) ((double) getWidth() / ((double) SIZE + 1));

                for (int i = 0; i < NUMBER_OF_COLOURS; i++) {

                    Paint wallpaint;

                    wallpaint = new Paint();
                    if (isColored) {
                        if (i == RED) {
                            wallpaint.setColor(Color.RED);
                        } else if (i == GREEN) {
                            wallpaint.setColor(Color.GREEN);
                        } else if (i == BLUE) {
                            wallpaint.setColor(Color.BLUE);
                        }
                    } else {
                        wallpaint.setColor(Color.WHITE);
                    }

                    wallpaint.setStyle(Style.FILL);

                    Path wallpath = new Path();
                    wallpath.reset();
                    wallpath.moveTo(0, getHeight());
                    for (int j = 0; j < SIZE - 1; j++) {
                        int value = (int) (((double) colourBins[i][j] / (double) maxY) * (getHeight()+100));
                        wallpath.lineTo(j * xInterval * offset, getHeight() - value);
                    }
                    wallpath.lineTo(SIZE * offset, getHeight());
                    canvas.drawPath(wallpath, wallpaint);
                }

            }

        }
    }
    */

    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dataLoadProgress = new ProgressDialog(this);
        dataLoadProgress.setMessage("Loading...");
        dataLoadProgress.setIndeterminate(true);
        dataLoadProgress.setCancelable(false);
        dataLoadProgress.setProgressStyle(android.R.attr.progressBarStyleLarge);
        return dataLoadProgress;
    }

    /*///HISTOGRAM
    public static int[] histogram(Bitmap img){

        int width = img.getWidth();
        int height = img.getHeight();

        int[] nivel_cinza = new int[256];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int color = img.getPixel(x, y);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                nivel_cinza[red]++;
                nivel_cinza[green]++;
                nivel_cinza[blue]++;
            }
        }

        return nivel_cinza;

    }

    public static int[] histogram_acumulate(Bitmap img){

        int[] histogram = histogram(img);

        for(int i=1; i<256; i++){

            histogram[i] = histogram[i-1] + histogram[i];

        }

        return histogram;

    }

    public static Bitmap histogram_equalize(Bitmap img){

        int width = img.getWidth();
        int height = img.getHeight();

        Bitmap new_image = Bitmap.createBitmap(width, height, img.getConfig());
        int[] histogram_acumulate = histogram_acumulate(img);
        int MN = img.getHeight() * img.getWidth();
        int G1 = 256-1;
        double fator = (double)G1/MN;

        int[] T = new int[256];


        for(int i=1; i<256; i++){


            T[i] = (int)(fator*histogram_acumulate[i]);

        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int pixel = img.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                pixel = Color.rgb(T[red], T[green], T[blue]);
                int color = Color.rgb(pixel, pixel, pixel);
                new_image.setPixel(x, y, color);
            }
        }
        return new_image;
    }
    */
}
