package com.inqry.technocratsignal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TECHNOCRAT_SIGNAL_LOG = "TechnocratSignal";
    Camera cam = null;
    IStatusServer server = null;
    CustomView bs = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializeTechnocratSignalView();

        if( initializeServer() ) {
            while (true) {
                try {
                    new CheckTechnocratSignalTask().execute();
                    Thread.sleep(10 * 1000);
                } catch (Exception e) {
                    Log.d(TECHNOCRAT_SIGNAL_LOG, "Thread sleep failed");
                }
            }
        }
    }


    private boolean initializeServer() {

        boolean rv = false;

        Button loadServerButton = (Button) findViewById(R.id.loadServerButton);
        loadServerButton.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void onClick(View v) {
                        EditText serverEditText = (EditText) findViewById(R.id.technocratSignalServer);
                        String serverUrl = serverEditText.getText().toString();

                        server = StatusServer.getServer();
                        server.setUrl(serverUrl);

                        if( server.test() ) {
                            // It tested, now get the options.
                            String[] options = server.getOptions();
                            if( null != options && options.length > 0 ) {
                                checkedOptions = new boolean[options.length];
                                pickNotifications(options);
                            }
                            else {
                                Toast.makeText( getApplicationContext(),
                                        "The server did not return any options", Toast.LENGTH_LONG );

                            }
                        }
                        else {
                            Toast.makeText( getApplicationContext(),
                                    "Please check your server settings", Toast.LENGTH_LONG );
                        }

                    }
                });

        return rv;
    }

    boolean[] checkedOptions = null;

    private void pickNotifications(String[] options) {
        // Respect: http://stackoverflow.com/questions/32323605/how-do-i-control-on-multichoice-alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        final List<String> colorsList = Arrays.asList(options);

        builder.setMultiChoiceItems(options, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                checkedOptions[which] = isChecked;
                String currentItem = colorsList.get(which);
                Toast.makeText(getApplicationContext(),
                        currentItem + " " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setCancelable(false);
        builder.setTitle(R.string.pick_group);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void initializeTechnocratSignalView() {

        bs = (CustomView)findViewById(R.id.technocratSignal);

        new Thread(new Runnable() {
            @Override
            public void run() {

                // Download the picture.
                downloadPicture();

                while( true ) {
                    try {
                        new LongOperation().execute();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();

    }


    public Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void downloadPicture() {
        String url = "http://blog.teddyhyde.com/assets/images/teddyhyde_96x96.png";
        Bitmap bmp = getBitmapFromURL( url );
        bs.setBmp(bmp);
    }

    private class LongOperation extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        @Override
        protected void onCancelled( Boolean rv ) {
            Log.v( TECHNOCRAT_SIGNAL_LOG, "OK, this failed");
        }

        @Override
        protected void onPostExecute( Boolean rv ) {
            if( rv ) {
                bs.update();
            }
        }
    }


    private class CheckTechnocratSignalTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            try {
                server.poll();
            }
            catch( Exception e ) {
                Log.d( TECHNOCRAT_SIGNAL_LOG, "Got an exception: " + e.toString() );
            }

            return null;
        }

        protected String onPostExecute(Boolean result) {
            return null;
        }

    }



    private void turnFlashlightOff() {
        cam.stopPreview();
        cam.release();
    }

    private void turnFlashlightOn() {
        cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        cam.startPreview();
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
}
