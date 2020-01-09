package com.test.pdfrenderer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1000;
    private final String TAG = getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        checkAppPermissions();
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkAppPermissions() {
        if (!isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                || !isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Permission is not granted, request to grant.
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private boolean isGranted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void initUI() {
        final Button btnRendering = findViewById(R.id.btnRendering);
        final EditText etFilePath = findViewById(R.id.etFilePath);
        final EditText etPageIndex = findViewById(R.id.etPageIndex);
        btnRendering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filePath = etFilePath.getText().toString();
                int pageIndex = Integer.parseInt(etPageIndex.getText().toString());
                render(filePath, pageIndex);
            }
        });
    }

    private void render(String filePath, int currentPage) {
        try {
            ImageView imgRenderArea = findViewById(R.id.imgRenderArea);

            int REQ_WIDTH = 1;
            int REQ_HEIGHT = 1;
            REQ_WIDTH = imgRenderArea.getWidth();
            REQ_HEIGHT = imgRenderArea.getHeight();
            Log.i(TAG, "render(): " + filePath + " page=" + currentPage + " " + REQ_WIDTH + "x" + REQ_HEIGHT);

            Bitmap bitmap = Bitmap.createBitmap(REQ_WIDTH, REQ_HEIGHT, Bitmap.Config.ARGB_4444);

            File file = new File(filePath);
            ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer renderer = new PdfRenderer(fileDescriptor);

            if (currentPage < 0) {
                currentPage = 0;
            } else if (currentPage > renderer.getPageCount()) {
                currentPage = renderer.getPageCount() - 1;
            }

            Matrix matrix = imgRenderArea.getImageMatrix();
            matrix.setScale(.75f, .75f);
            Rect rect = new Rect(0, 0, REQ_WIDTH, REQ_HEIGHT);

            PdfRenderer.Page rendererPage = renderer.openPage(currentPage);
            rendererPage.render(bitmap, rect, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

            imgRenderArea.setImageMatrix(matrix);
            imgRenderArea.setImageBitmap(bitmap);
            imgRenderArea.invalidate();

            /* Recycling of usage. */
            rendererPage.close();
            renderer.close();
            fileDescriptor.close();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.w(TAG, "render(): " + e.getMessage());
            e.printStackTrace();
        }
    }
}
