package fi.naf.toasjono;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by rrou on 19/09/2017.
 */

public class RequestUtil {
    public interface Listener<String, T> {
        T onResponse(String response);
    }
    public interface ErrorListener {
        void onErrorResponse(VolleyError error);
    }

    public static <T extends Object> CompletableFuture<T> createRequest(
            Context context,
            int method,
            String url,
            Map<String, String> params,
            Listener<String, T> responseListener,
            ErrorListener errorListener
    ) {

        CompletableFuture<T> future = new CompletableFuture<>();
        StringRequest stringRequest = new StringRequest(method, url, (String response) -> {
            future.complete(responseListener.onResponse(response));
        }, error -> {
            if(errorListener != null) {
                errorListener.onErrorResponse(error);
            }
            future.completeExceptionally(error);
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        int socketTimeout = 5 * 60 * 1000; // 5 min
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        Volley.newRequestQueue(context).add(stringRequest);
        return future;
    }

    public static <T extends Object> CompletableFuture<T> createRequest(
            Context context,
            int method,
            String url,
            Listener<String, T> responseListener,
            ErrorListener errorListener
    ) {
        return createRequest(context, method, url, new HashMap<>(), responseListener, errorListener);
    }
    public static <T extends Object> CompletableFuture<T> createRequest(
            Context context,
            int method,
            String url,
            Listener<String, T> responseListener
    ) {
        return createRequest(context, method, url, new HashMap<>(), responseListener, null);
    }

    public static <T extends Object> CompletableFuture<T> createRequest(
            Context context,
            int method,
            String url,
            Map<String, String> params,
            Listener<String, T> responseListener
    ) {
        return createRequest(context, method, url, params, responseListener, null);
    }

}
