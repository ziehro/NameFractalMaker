package com.ziehro.namefractal;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.commons.math3.complex.Complex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    private EditText nameEditText;
    private ImageView imageHolder;
    private DatePicker birthdatePicker;
    private ProgressDialog progressDialog;
    Bitmap result;



    int WIDTH = 1000;
    int HEIGHT = 1000;
    int MAX_ITERATIONS = 100;

    double MAX_REAL = 1.5;
    double MIN_REAL = -1.5;
    double MAX_IMAG = 1.5;
    double MIN_IMAG = -1.5;
    int initialZoomLevel = 1;
    float  zoomLevel = 1.0F;
    float THRESHOLD = 2.5F;

    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEditText = findViewById(R.id.nameEditText);
        //birthdatePicker = findViewById(R.id.birthdatePicker);
        imageHolder = findViewById(R.id.imageViewFractal);

       /* progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Generating Fractal");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
*/



        Button generateButton = findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();

                Date birthdate = new GregorianCalendar(year, month, day).getTime();

                Bitmap fractalImage = generateFractalImage(name, birthdate, initialZoomLevel);
                displayImage(fractalImage);
                //birthdatePicker.setVisibility(View.GONE);
                result=fractalImage;
            }


        });

        Button btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });





        EditText zoomLevelEditText = findViewById(R.id.zoomLevelEditText);
        zoomLevelEditText.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String name = nameEditText.getText().toString();
                int year = birthdatePicker.getYear();
                int month = birthdatePicker.getMonth();
                int day = birthdatePicker.getDayOfMonth();
                Date birthdate = new GregorianCalendar(year, month, day).getTime();
                String zoomLevelString = textView.getText().toString();
                int zoomLevel = Integer.parseInt(zoomLevelString);
                Bitmap fractalImage = generateFractalImage(name, birthdate, zoomLevel);
                displayImage(fractalImage);
                return true;
            }
            return false;
        });

        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    // Permission is granted
                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }

                String dateHere = (String.valueOf(year + " " + month +" "+ day));

                downloadImage(result,name, dateHere);
            }
        });

        // In onCreate() method:
        SeekBar zoomSeekBar = findViewById(R.id.zoom_seekbar);
        zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            String name = nameEditText.getText().toString();

            Date birthdate = new GregorianCalendar(year, month, day).getTime();
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the zoom level when the seekbar progress changes
                double MAX_ZOOM = 1000.0;
                double MIN_ZOOM = 1.0;
                zoomLevel = (float) ((double) progress / zoomSeekBar.getMax() * (MAX_ZOOM - MIN_ZOOM) + MIN_ZOOM);
                generateFractalImage(name, birthdate, zoomLevel);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });




    }

    private Bitmap generateFractalImage(String name, Date birthdate, float zoomLevel) {
        //progressDialog.show();
        Log.d("Date ", String.valueOf(year + month + day));


        // Set up image dimensions
        int width = WIDTH;
        int height = HEIGHT;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Set up complex plane coordinates
        double minReal = -2.0 / zoomLevel;
        double maxReal = 1.0 / zoomLevel;
        double minImag = -1.5 / zoomLevel;
        double maxImag = 1.5 / zoomLevel;

        // Set up constants
        final int MAX_ITERATIONS = 100;
        final double THRESHOLD = 40.0;

        // Set up hue range for coloring pixels
        float[] hsv = new float[3];
        float hueRange = 360.0f;
        float hueStart = 0.0f;
        StringBuilder combined = new StringBuilder(name + birthdate);
        // Set up complex number corresponding to user's name and birthdate
        Complex c = mapStringToComplexDomain(combined.toString(), minReal, maxReal, minImag, maxImag);




        // Generate fractal image pixel by pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Map pixel coordinates to complex plane coordinates
                double real = minReal + (maxReal - minReal) * x / (width - 1);
                double imag = maxImag - (maxImag - minImag) * y / (height - 1);
                Complex z = new Complex(real, imag);

                // Iterate the fractal formula
                int iterations = 0;
                while (z.abs() < THRESHOLD && iterations < MAX_ITERATIONS) {
                    z = z.multiply(z).add(c);
                    iterations++;
                }
                //int progress = (int) ((float) iterations / (float) MAX_ITERATIONS * 100);
                //progressDialog.setProgress(progress);


                // Color the pixel based on number of iterations
                if (iterations < MAX_ITERATIONS) {
                    float hue = hueStart + (float) iterations * hueRange / (float) MAX_ITERATIONS;
                    float saturation = (float) Math.min(1f, iterations / THRESHOLD * 1.2f);
                    float value = (float) (1f - Math.min(1f, iterations / THRESHOLD * 0.8f));
                    hsv[0] = hue % 360.0f;
                    hsv[1] = saturation;
                    hsv[2] = value;
                    int color = hsbToColor(hsv[0],hsv[1],hsv[2]);
                    bitmap.setPixel(x, y, color);
                } else {
                    bitmap.setPixel(x, y, Color.BLACK);
                }
            }
        }
        //progressDialog.dismiss();

        return bitmap;
    }

    private int hsbToColor(float hue, float saturation, float brightness) {
        return Color.HSVToColor(new float[]{hue, saturation, brightness});
    }

    public static Complex mapStringToComplexDomain(String s, double minReal, double maxReal, double minImag, double maxImag) {
        double real = mapStringToDomain(s, minReal, maxReal);
        double imag = mapStringToDomain(s, minImag, maxImag);
        return new Complex(real, imag);
    }

    private static double mapStringToDomain(String s, double min, double max) {
        // We convert the string to a hash code to get a unique integer value for each string
        int hash = s.hashCode();

        // We map the hash code to a value in the range [0, 1]
        double value = (hash & 0x7FFFFFFF) / (double) 0x7FFFFFFF;

        // We map the value to the range [min, max]
        return min + value * (max - min);
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

    private void downloadImage(Bitmap bitmap, String name, String date) {
        // Get the directory to save the image in
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "fractals");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Create a new file in the directory with a unique name
        String fileName = ("FractalFor" + name + " " + date +  ".png").trim();
        Log.d("Hreeeee", date);
        File file = new File(dir, fileName);

        try {
            // Save the bitmap to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Tell the download manager to download the file
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            downloadManager.addCompletedDownload(fileName, "Fractal image", true, "image/png", file.getAbsolutePath(), file.length(), true);

            // Show a toast message to indicate that the download was successful
            Toast.makeText(this, "Image saved to fractals folder", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image to fractals folder", Toast.LENGTH_LONG).show();
        }
    }

    private void showDatePickerDialog() {
        // Get the current date as the default date for the date picker


        // Create a new instance of DatePickerDialog and show it
        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year1, int month1, int dayOfMonth1) {
                        // Do something with the selected date
                        // For example, update a TextView with the selected date
                        TextView tvSelectedDate = findViewById(R.id.tv_selected_date);
                        tvSelectedDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1);
                        year = year1;
                        month = month1;
                        day = dayOfMonth1;

                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    downloadImage(result,name, new StringBuilder(year + month + day).toString());
                } else {
                    // Permission denied
                    Toast.makeText(this, "Write external storage permission is required to save the image.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }*/


}


