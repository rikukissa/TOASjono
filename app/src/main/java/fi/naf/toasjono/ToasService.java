package fi.naf.toasjono;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ToasService extends IntentService {
    private static final String url = "https://asukas.toas.fi/sahkoisetpalvelut/hakemuksenmuokkaus/default.aspx";;

    public ToasService() {
        super("ToasService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Do the task here
        Log.i("MyTestService", "Service running");
        DBHandler db = new DBHandler(this);

        Log.i("data", Integer.toString(db.getQueue(170)));
        Log.i("data", Integer.toString(db.getQueue(130)));

        getLoginData();
    }

    private void getLoginData() {
        // Instantiate the RequestQueue.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document document = Jsoup.parse(response);
                        String viewState = document.select("#__VIEWSTATE").attr("value");
                        String eventValidate = document.select("#__EVENTVALIDATION").attr("value");
                        String viewStateGenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");

                        Log.i("viewstate", viewState);
                        Log.i("eventvalidate", eventValidate);
                        Log.i("viewStateGenerator", viewStateGenerator);

                        loginWithData(viewState, eventValidate, viewStateGenerator);

                        Log.d("getLoginData", "yes");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("error", error.getMessage());
                    }
                }
        );

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }
    private void loginWithData(final String viewState, final String eventValidate, final String viewStateGenerator) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document document = Jsoup.parse(response);

                        String viewState = document.select("#__VIEWSTATE").attr("value");
                        String eventValidate = document.select("#__EVENTVALIDATION").attr("value");
                        String viewStateGenerator = document.select("#__VIEWSTATEGENERATOR").attr("value");
                        String muokkausCount = document.select("[id~=(.+)hdf_Count").attr("value");

                        getQueueNumbers(viewState, eventValidate, viewStateGenerator, muokkausCount);

                        Log.d("mCount", muokkausCount);
                        Log.d("loginData", "yes");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("__EVENTTARGET", "");
                params.put("__EVENTARGUMENT", "");
                params.put("__VIEWSTATE", viewState);
                params.put("__VIEWSTATEGENERATOR", viewStateGenerator);
                params.put("__EVENTVALIDATION", eventValidate);
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Tunnus", "pyry.rouvila@gmail.com");
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Salasana", "");
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$btnKirjaudu", "Kirjaudu");
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$hdf_Count", "");

                return params;
            }
        };
        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }

    private void getQueueNumbers(final String viewState, final String eventValidate, final String viewStateGenerator, final String muokkausCount) {
        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://naf.fi/dev/debug.html",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Document document = Jsoup.parse(response);
                        Element applications = document.select("#Hakemuksen_Muokkaus_TAAAAAAAA_gv_ApplicationQueues").first();
                        Elements rows = applications.select("tr");

                        DBHandler db = new DBHandler(getApplicationContext());

                        for(int i = 1; i < rows.size(); i++) {
                            Element row = rows.get(i);
                            Elements cols = row.select("td");

                            String[] parts = cols.get(0).html().split(" ");
                            int id = Integer.parseInt(parts[0]);
                            String house = parts[1];
                            int queue = Integer.parseInt(cols.get(1).html());

                            db.updateQueue(id, house, queue);
                        }
                        Log.d(";;;", "Done updating");

                        // add time
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = prefs.edit();

                        Date currentTime = Calendar.getInstance().getTime();

                        editor.putString("lastsaved", currentTime.toString());
                        editor.commit();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("__EVENTTARGET", "");
                params.put("__EVENTARGUMENT", "");
                params.put("__VIEWSTATE", viewState);
                params.put("__VIEWSTATEGENERATOR", viewStateGenerator);
                params.put("__EVENTVALIDATION", eventValidate);
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$hdf_Count", muokkausCount);
                params.put("Hakemuksen_Muokkaus_TAAAAAAAA$gv_ApplicationList$ctl03$Jonotus", "Jonotusnumero");

                return params;
            }
        };
        int socketTimeout = 5 * 60 * 1000; // 5 min
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);

        Volley.newRequestQueue(getApplicationContext()).add(postRequest);
    }
}