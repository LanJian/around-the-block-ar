package com.example.aroundtheblock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;

class RequestTask extends AsyncTask<String, String, String>{

  private PhotoOverlay mOverlay;

  public RequestTask(PhotoOverlay o) {
    mOverlay = o;
  }

  @Override
  protected String doInBackground(String... uri) {
    HttpClient httpclient = new DefaultHttpClient();
    HttpResponse response;
    String responseString = null;
    try {
      response = httpclient.execute(new HttpGet(uri[0]));
      StatusLine statusLine = response.getStatusLine();
      if(statusLine.getStatusCode() == HttpStatus.SC_OK){
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        response.getEntity().writeTo(out);
        out.close();
        responseString = out.toString();
      } else{
        //Closes the connection.
        response.getEntity().getContent().close();
        throw new IOException(statusLine.getReasonPhrase());
      }
    } catch (ClientProtocolException e) {
      //TODO Handle problems..
    } catch (IOException e) {
      //TODO Handle problems..
    }
    return responseString;
  }

  @Override
  protected void onPostExecute(String result) {
    super.onPostExecute(result);

    //System.err.println("result: " + result);

    try {
      mOverlay.clearPhotos();

      JSONObject object = (JSONObject) new JSONTokener(result).nextValue();
      JSONObject photos = object.getJSONObject("photos");
      Iterator<String> keys = photos.keys();
      while (keys.hasNext()) {
        String k = keys.next();
        JSONObject p = photos.getJSONObject(k);
        double lat = p.getJSONObject("location").getDouble("latitude");
        double lon = p.getJSONObject("location").getDouble("longitude");
        String url = p.getString("url");
        Photo photo = new Photo(mOverlay, lat, lon, url);
        mOverlay.addPhoto(photo);
        System.err.println(url);
      }
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }
}
