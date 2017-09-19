package fi.naf.toasjono;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ToasService {
    private static class PageFields {
        public String viewState;
        public String eventValidate;
        public String viewStateGenerator;
        public String muokkausCount;

        public PageFields(String viewState, String eventValidate, String viewStateGenerator) {
            this.viewState = viewState;
            this.eventValidate = eventValidate;
            this.viewStateGenerator = viewStateGenerator;
        }
        public PageFields(String viewState, String eventValidate, String viewStateGenerator, String muokkausCount) {
            this.viewState = viewState;
            this.eventValidate = eventValidate;
            this.viewStateGenerator = viewStateGenerator;
            this.muokkausCount = muokkausCount;
        }
    }

    private static final String url = "https://asukas.toas.fi/sahkoisetpalvelut/hakemuksenmuokkaus/default.aspx";;

    public static CompletableFuture<List<TOASPosition>> getQueues(Context context) {
        return getLoginData(context)
            .thenCompose((fields) -> loginWithData(context, fields))
            .thenCompose((fields) -> getQueueNumbers(context, fields));
    }

    private static PageFields parsePageFields(Document document) {
        String VIEWSTATE_SELECTOR = "#__VIEWSTATE";
        String EVENTVALIDATION_SELECTOR = "#__EVENTVALIDATION";
        String VIEWSTATEGENERATOR_SELECTOR = "#__VIEWSTATEGENERATOR";
        String viewState = document.select(VIEWSTATE_SELECTOR).attr("value");
        String eventValidate = document.select(EVENTVALIDATION_SELECTOR).attr("value");
        String viewStateGenerator = document.select(VIEWSTATEGENERATOR_SELECTOR).attr("value");
        return new PageFields(viewState, eventValidate, viewStateGenerator);
    }

    private static CompletableFuture<PageFields> getLoginData(Context context) {
        CompletableFuture<PageFields> future = new CompletableFuture<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, (String response) -> {
            Document document = Jsoup.parse(response);
            future.complete(parsePageFields(document));
        },
        error -> {
            Log.d("Error.Response", error.getMessage());
            future.completeExceptionally(error);
        });
        Volley.newRequestQueue(context).add(stringRequest);
        return future;
    }

    private static CompletableFuture<PageFields> loginWithData(Context context, PageFields pageFields) {

        CompletableFuture<PageFields> future = new CompletableFuture<>();
        StringRequest postRequest = new StringRequest(Request.Method.POST, url, (response) -> {
            Document document = Jsoup.parse(response);
            PageFields newPageFields = parsePageFields(document);
            String muokkausCount = document.select("[id~=(.+)hdf_Count").attr("value");
            newPageFields.muokkausCount = muokkausCount;
            future.complete(newPageFields);
        } , (error) -> {
            // error
            Log.d("Error.Response", error.getMessage());
            future.completeExceptionally(error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                return ToasService.getLoginParams(pageFields);
            }
        };

        Volley.newRequestQueue(context).add(postRequest);
        return future;
    }

    private static Map<String, String> getLoginParams(PageFields pageFields) {
        Map<String, String> params = ToasService.getBaseParams(pageFields);
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Tunnus", "");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Salasana", "");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$btnKirjaudu", "Kirjaudu");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$hdf_Count", "");
        return params;
    }

    private static Map<String, String> getBaseParams(PageFields pageFields) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("__EVENTTARGET", "");
        params.put("__EVENTARGUMENT", "");
        params.put("__VIEWSTATE", pageFields.viewState);
        params.put("__VIEWSTATEGENERATOR", pageFields.viewStateGenerator);
        params.put("__EVENTVALIDATION", pageFields.eventValidate);
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$hdf_Count", pageFields.muokkausCount);
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$gv_ApplicationList$ctl03$Jonotus", "Jonotusnumero");
        return params;
    }

    private static CompletableFuture<List<TOASPosition>> getQueueNumbers(Context context, PageFields pageFields) {
        CompletableFuture<List<TOASPosition>> future = new CompletableFuture<>();

        StringRequest postRequest = new StringRequest(Request.Method.POST, "http://naf.fi/dev/debug.html", (response) -> {
            Document document = Jsoup.parse(response);
            Element applications = document.select("#Hakemuksen_Muokkaus_TAAAAAAAA_gv_ApplicationQueues").first();
            Elements rows = applications.select("tr");

            List<TOASPosition> positions = new ArrayList<>();

            for(int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");

                String[] parts = cols.get(0).html().split(" ");
                int id = Integer.parseInt(parts[0]);
                String house = parts[1];
                int queue = Integer.parseInt(cols.get(1).html());

                positions.add(new TOASPosition(id, house, queue));
            }
            Log.d(";;;", "Done updating");
            future.complete(positions);

        }, (VolleyError error) -> {
            Log.d("Error.Response", error.getMessage());
            future.completeExceptionally(error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                return ToasService.getBaseParams(pageFields);
            }
        };

        int socketTimeout = 5 * 60 * 1000; // 5 min
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(context).add(postRequest);
        return future;
    }
}