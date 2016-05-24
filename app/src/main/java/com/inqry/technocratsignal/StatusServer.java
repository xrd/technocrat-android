package com.inqry.technocratsignal;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xrdawson on 2/7/16.
 */
public class StatusServer implements IStatusServer {

    private static StatusServer _ss = null;
    private String _url;
    OkHttpClient client = new OkHttpClient();
    HashMap<String,Boolean> _options;

    // Allows us to mock it, eh?
    public static void setStatusServer( StatusServer ss ) {
        _ss = ss;
    }

    public static StatusServer getServer() {
        if( null == _ss ) {
            _ss = new StatusServer();
        }

        return _ss;
    }

    public void setUrl( String url ) {
        _url = url;
        if( null != _url ) {
            // fix the URL if needed
            if (-1 == _url.indexOf("http://")) {
                _url = "http://" + _url;
            }
            if (-1 == _url.indexOf(":8080")) {
                _url += ":8080/hubot/status";
            } else if (-1 == url.indexOf("/hubot/status")) {
                _url += "/hubot/status";
            }
        }
    }

    public StatusServer() {
        _url = null;
        _options = new HashMap<String,Boolean>();
    }

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public HashMap<String,Boolean> poll() {
        loadJsonFromUrl();
        return _options;
    }

    private void loadJsonFromUrl() {

        try {
            String response = run( _url );
            HashMap<String,Boolean> newValues = new HashMap<String,Boolean>();

            // Respect: http://stackoverflow.com/questions/9151619/java-iterate-over-jsonobject
            JSONObject jobj = new JSONObject( response );
            Iterator<?> keys = jobj.keys();
            while( keys.hasNext() ) {
                String key = (String) keys.next();
                if (! "initialized".equals( key ) ) {
                    if ( jobj.get(key) instanceof String ) {
                        String trueFalseString = (String)jobj.get(key);
                        newValues.put( key, trueFalseString.equalsIgnoreCase( "true" ) );
                    }
                }
            }
            _options = newValues;

        }
        catch( JSONException jse ) {
            Log.v( MainActivity.TECHNOCRAT_SIGNAL_LOG, "Error iterating over JSON");
        }
        catch( IOException ioe ) {
            Log.v( MainActivity.TECHNOCRAT_SIGNAL_LOG, "Unable to get JSON from server");
        }
        catch( Exception e ) {
            Log.v( MainActivity.TECHNOCRAT_SIGNAL_LOG, "General error");
        }

    }

    public boolean test() {
        loadJsonFromUrl();
        return null != _options;
    }

    public boolean isInitialized() {
        return( null != _url );
    }
}
