package com.inqry.technocratsignal;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xrdawson on 2/7/16.
 */
public class StatusServer implements IStatusServer {

    private static StatusServer _ss = null;
    private String _url;
    private String[] hardcodedOptions = { "Android", "iOS", "Ruby" };

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
    }

    public StatusServer() {
        _url = null;
    }

    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String[] getOptions() {
//        return _options;

        return hardcodedOptions;
    }

    public String[] poll() {
        return null;
    }

    private String[] loadJsonFromUrl( String url ) {
        String[] rv = null;
        try {
            String response = run( _url );

            // Respect: http://stackoverflow.com/questions/3408985/json-array-iteration-in-android-java
            int id;
            String name;
            JSONArray array = new JSONArray( response );
            rv = new String[array.length()];

            for (int i = 0; i < array.length(); i++) {
                JSONObject row = array.getJSONObject(i);
                rv[i] = row.getString("name");
            }
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

        return rv;
    }

    String[] _options;
    public boolean test() {

        boolean rv = true;


        _options = loadJsonFromUrl( _url );
        rv = true;

        return rv;
    }

    public boolean isInitialized() {
        return( null != _url );
    }
}
