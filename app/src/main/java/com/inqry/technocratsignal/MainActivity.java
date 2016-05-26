package com.inqry.technocratsignal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String TECHNOCRAT_SIGNAL_LOG = "TechnocratSignal";
    Camera cam = null;
    StatusServer server = null;
    CustomView bs = null;
    HashMap<String,Boolean> choices;
    private boolean notificationsPicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bs = (CustomView)findViewById(R.id.technocratSignal);
        initializeServer();

        // This crashes, says UI cannot be updated from outside of UI thread (makes sense).
//        new Thread(new Runnable() {
//            @Override
//            public void run() {

                int count = 0;
                while (true) {
                    try {
                        if (count == 100) {
                            count = 0;
                            if (server.isInitialized()) {
                                new PollTechnocratSignalTask().execute(server);
                            }
                        } else {
                            count++;
                        }

                        // new LongOperation().execute();
                        bs.update();

                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
//            }}).start();

    }

    public void validateNewValues( HashMap<String,Boolean> newValues ) {
        // Check to see if we have selected an item which is now on
        for (String k : newValues.keySet()) {
            if (newValues.get(k) &&
                    choices.get(k)) {
                // Turn on the technocrat signal!!!
                bs.setTechnocratSignalState(true, "Alert!");
            }
        }

//        if (server.isInitialized() && notificationsPicked ) {
////                                new PollTechnocratSignalTask().execute();
//            if( count == 50 ) {
//                bs.setTechnocratSignalState( true, "Technocrat signal!");
//            }
//            count++;
//        }
////
    }

    private void initializeServer() {

        server = StatusServer.getServer();

        Button loadServerButton = (Button) findViewById(R.id.loadServerButton);
        loadServerButton.setOnClickListener(
                new View.OnClickListener() {
                    @SuppressLint("ShowToast")
                    @Override
                    public void onClick(View v) {
                        EditText serverEditText = (EditText) findViewById(R.id.technocratSignalServer);
                        String serverUrl = serverEditText.getText().toString();

                        server.setUrl(serverUrl);
                        new InitTechnocratSignalTask().execute( server );

                    }
                });
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
        // Icons made http://www.flaticon.com/authors/dave-gandy" title="Dave Gandy">Dave Gandy</a> from
        // <a href="http://www.flaticon.com" title="Flaticon">www.flaticon.com</a> is licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a></div>

        String url = "http://blog.teddyhyde.com/assets/images/code-fork-symbol.png";
        Bitmap bmp = getBitmapFromURL( url );
        bs.setBmp(bmp);
    }


    private class InitTechnocratSignalTask extends AsyncTask<StatusServer, Void, Boolean> {

        protected Boolean doInBackground(StatusServer... servers) {

            StatusServer server = servers[0];
            Boolean rv = false;
            if( server.test() ) {

                // Download the picture.
                downloadPicture();

                // It tested, now get the options.
                choices = server.poll();
                if( null != choices && choices.size() > 0 ) {
                    rv = true;
                }
            }

            return rv;
        }



        @Override
        protected void onPostExecute( Boolean result ) {
            if( result  ) {
                pickNotifications();
            }
            else {
                Toast.makeText( getApplicationContext(),
                        "Please check your server settings", Toast.LENGTH_LONG );
            }
        }
    }

    private void pickNotifications() {
        // Respect: http://stackoverflow.com/questions/32323605/how-do-i-control-on-multichoice-alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        final Set<String> keys = choices.keySet();
        final String[] choicesAsString = keys.toArray(new String[keys.size()]);

        builder.setMultiChoiceItems(choicesAsString, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                String selected = choicesAsString[which];
                choices.put( selected, isChecked );
                Toast.makeText(getApplicationContext(),
                        selected + " " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setCancelable(false);
        builder.setTitle(R.string.pick_group);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notificationsPicked = true;
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private class LongOperation extends AsyncTask<Void,Void,Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            return true;
        }

        @Override
        protected void onPostExecute( Boolean success ) {
            bs.update();
        }
    }


    private class PollTechnocratSignalTask extends AsyncTask<StatusServer, Void, Boolean> {

        HashMap<String, Boolean> newValues;

        protected Boolean doInBackground(StatusServer... servers) {

            StatusServer server = servers[0];
            try {
                newValues = server.poll();
            } catch (Exception e) {
                Log.d(MainActivity.TECHNOCRAT_SIGNAL_LOG, "Got an exception: " + e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                validateNewValues( newValues );
            }
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
