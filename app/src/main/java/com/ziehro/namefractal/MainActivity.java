package com.ziehro.namefractal;

import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private EditText nameEditText;
    private ImageView imageHolder;
    private DatePicker birthdatePicker;
    int WIDTH = 500;
    int HEIGHT = 500;
    int MAX_ITERATIONS = 15000;

    double MAX_REAL = 0.5;
    double MIN_REAL = -0.5;
    double MAX_IMAG = 0.5;
    double MIN_IMAG = -0.5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        birthdatePicker = findViewById(R.id.birthdatePicker);
        imageHolder = findViewById(R.id.imageViewFractal);


        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                int year = birthdatePicker.getYear();
                int month = birthdatePicker.getMonth();
                int day = birthdatePicker.getDayOfMonth();
                Date birthdate = new GregorianCalendar(year, month, day).getTime();

                Bitmap fractalImage = generateFractalImage(name, birthdate);
                displayImage(fractalImage);
                birthdatePicker.setVisibility(View.GONE);
            }

        });
        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                int year = birthdatePicker.getYear();
                int month = birthdatePicker.getMonth();
                int day = birthdatePicker.getDayOfMonth();
                Date birthdate = new GregorianCalendar(year, month, day).getTime();
                Bitmap fractalImage = generateFractalImage(name, birthdate);
                downloadImage(fractalImage);
            }
        });



    }

    private Bitmap generateFractalImage(String name, Date birthdate) {
        Bitmap bitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        double birthdateValue = getBirthdateValue(birthdate) * 0.0001;
        double nameValue = getNameValue(name);
        double cReal, cImag, zReal = 0, zImag = 0, temp;
        int color;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                cReal = mapPixelToComplexDomain(x, y).getReal() + nameValue;
                cImag = mapPixelToComplexDomain(x, y).getImaginary() + birthdateValue;
                int iterations = 0;

                while (zReal * zReal + zImag * zImag <= 4 && iterations < MAX_ITERATIONS) {
                    temp = zReal * zReal - zImag * zImag + cReal;
                    zImag = 2 * zReal * zImag + cImag;
                    zReal = temp;
                    iterations++;
                }

                if (iterations == MAX_ITERATIONS) {
                    color = Color.BLACK;
                } else {
                    float[] hsv = new float[]{(float) iterations * 100 % 360, 1, 1};
                    color = Color.HSVToColor(hsv);
                }

                bitmap.setPixel(x, y, color);
            }
        }

        return bitmap;
    }



    private void displayImage(Bitmap bitmap) {
        ImageView imageView = new ImageView(this);
        imageHolder.setImageBitmap(bitmap);

       /* LinearLayout layout = findViewById(R.id.layout);
        layout.addView(imageView);*/
    }

    private int getColorForPoint(Complex z, double nameValue, double birthdateValue) {
        int iterations = 0;
        int MAX_ITERATIONS=150;
        Complex c = new Complex(nameValue, birthdateValue);

        while (iterations < MAX_ITERATIONS && z.abs() <= 2.0) {
            z = z.pow(2).add(c);
            iterations++;
        }

        if (iterations == MAX_ITERATIONS) {
            return Color.BLACK;
        } else {
            float hue = (float)iterations / (float)MAX_ITERATIONS;
            float saturation = 1.0f;
            float brightness = 1.0f;
            float[] hsb = new float[] { hue * 360, saturation, brightness };
            return Color.HSVToColor(hsb);
        }
    }

    private Complex mapPixelToComplexDomain(int x, int y) {
        double real = (double) x / WIDTH * (MAX_REAL - MIN_REAL) + MIN_REAL;
        double imag = (double) y / HEIGHT * (MAX_IMAG - MIN_IMAG) + MIN_IMAG;
        return new Complex(real, imag);
    }



    private double getNameValue(String name) {
        double sum = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            sum += (int) c;
        }
        return sum;
    }

    private double getBirthdateValue(Date birthdate) {
        double value = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(birthdate);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        value += year * 10000 + month * 100 + day;

        return value;
    }

    private void downloadImage(Bitmap bitmap) {
        String filename = "fractal.png";
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.addCompletedDownload(filename, "Fractal Image", true, "image/png", file.getAbsolutePath(), file.length(), true);

        Toast.makeText(this, "Image downloaded to Downloads directory", Toast.LENGTH_SHORT).show();
    }
}


