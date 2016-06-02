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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    public static final String TECHNOCRAT_SIGNAL_LOG = "TechnocratSignal";
    Camera cam = null;
    StatusServer server = null;
    CustomView bs = null;
    HashMap<String,Boolean> choices;
    private boolean notificationsPicked;
    private boolean technocratSignalOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        bs = (CustomView)findViewById(R.id.technocratSignal);
        initializeServer();

        // This crashes, says UI cannot be updated from outside of UI thread (makes sense).
        new Thread(new Runnable() {
            @Override
            public void run() {

                int count = 0;
                while (true) {
                    try {
                        if( count % 100 == 0 ) {
                            count = 0;
                            if (server.isInitialized()) {
                                new PollTechnocratSignalTask().execute(server);
                            }
                        }
                        if( technocratSignalOn ) {
                            if (count % 20 == 0 ) {
                                turnFlashlightOff();
                            }
                            else if( count % 10 == 0 ) {
                                turnFlashlightOn();
                            }
                        }

                        count++;

//                         new LongOperation().execute();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bs.update();
                            }
                        });

                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }}).start();

    }

    public void validateNewValues( HashMap<String,Boolean> newValues ) {
        if( null != choices ) {
            // Check to see if we have selected an item which is now on
            for (String k : newValues.keySet()) {
                    Boolean newValue = newValues.get(k);
                    Boolean choiceValue = choices.get(k);

                    if ( newValue && choiceValue ) {
                        // Turn on the technocrat signal!!!
                        bs.setTechnocratSignalState(true, "Alert!");
                        technocratSignalOn = true;

                        // Change the button to be "Turn Off"
                        loadServerOrTurnOffButton.setText(getString(R.string.turn_off));

                }
            }
        }

    }

    private Button loadServerOrTurnOffButton;

    private void initializeServer() {

        server = StatusServer.getServer();

        loadServerOrTurnOffButton = (Button) findViewById(R.id.loadServerButton);
        if( null != loadServerOrTurnOffButton ) {
            loadServerOrTurnOffButton.setOnClickListener(
                    new View.OnClickListener() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void onClick(View v) {
                            if( technocratSignalOn ) {
                                // Turn the signal off
                                technocratSignalOn = false;
                                bs.setTechnocratSignalState( false, "" );
                                choices = null;
                                // reset the text
                                loadServerOrTurnOffButton.setText( getString( R.string.load_server ));;
                            } else {
                                EditText serverEditText = (EditText) findViewById(R.id.technocratSignalServer);
                                String serverUrl = serverEditText.getText().toString();

                                server.setUrl(serverUrl);
                                new InitTechnocratSignalTask().execute(server);
                            }
                        }
                    });
        }
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

        private HashMap<String, Boolean> options;

        protected Boolean doInBackground(StatusServer... servers) {

            StatusServer server = servers[0];
            Boolean rv = false;
            if( server.test() ) {

                // Download the picture.
                downloadPicture();

                // It tested, now get the options.
                options = server.poll();
                if( null != options && options.size() > 0 ) {
                    rv = true;
                }
            }

            return rv;
        }



        @Override
        protected void onPostExecute( Boolean result ) {
            if( result  ) {
                pickNotifications( options );
            }
            else {
                Toast.makeText( getApplicationContext(),
                        "Please check your server settings", Toast.LENGTH_LONG );
            }
        }
    }

    private void pickNotifications( HashMap<String,Boolean> options ) {
        // Respect: http://stackoverflow.com/questions/32323605/how-do-i-control-on-multichoice-alertdialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // Don't save the choices until we close the dialog
        final HashMap<String,Boolean> consideredChoices = new HashMap<>();

        final Set<String> keys = options.keySet();
        final String[] choicesAsString = keys.toArray(new String[keys.size()]);

        builder.setMultiChoiceItems(choicesAsString, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                String selected = choicesAsString[which];
                consideredChoices.put( selected, isChecked );
//                Toast.makeText(getApplicationContext(),
//                        selected + " " + isChecked, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setCancelable(false);
        builder.setTitle(R.string.pick_group);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notificationsPicked = true;
                choices = consideredChoices;
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                validateNewValues( newValues );
            }
        }
    }


    private void turnFlashlightOff() {
        if( null != cam ) {
            cam.stopPreview();
            cam.release();
        }
    }

    private void turnFlashlightOn() {
        try {
            cam = Camera.open();
            if (null != cam) {
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        }
        catch( RuntimeException rune ) {
            Log.v( TECHNOCRAT_SIGNAL_LOG, "Camera failed to open" );
        }
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
