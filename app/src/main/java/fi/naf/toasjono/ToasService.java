package fi.naf.toasjono;

import android.content.Context;
import com.android.volley.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ToasService {
    private static final String url = "https://asukas.toas.fi/sahkoisetpalvelut/hakemuksenmuokkaus/default.aspx";;

    public static CompletableFuture<List<TOASPosition>> getQueues(Context context) {
        return getLoginData(context)
            .thenCompose((fields) -> loginWithData(context, fields))
            .thenCompose((fields) -> getQueueNumbers(context, fields));
    }

    private static CompletableFuture<TOASParser.PageFields> getLoginData(Context context) {
        return RequestUtil.createRequest(context, Request.Method.GET, url, (response) -> {
            Document document = Jsoup.parse(response);
            return TOASParser.parsePageFields(document);
        });
    }

    private static CompletableFuture<TOASParser.PageFields> loginWithData(Context context, TOASParser.PageFields pageFields) {
        return RequestUtil.createRequest(context, Request.Method.POST, url, ToasService.getLoginParams(pageFields), (response) -> {
            Document document = Jsoup.parse(response);
            TOASParser.PageFields newPageFields = TOASParser.parsePageFields(document);
            String muokkausCount = TOASParser.parseMuokkausCount(document);
            newPageFields.muokkausCount = muokkausCount;
            return newPageFields;
        });
    }

    private static CompletableFuture<List<TOASPosition>> getQueueNumbers(Context context, TOASParser.PageFields pageFields) {
        return RequestUtil.createRequest(context, Request.Method.POST, "http://naf.fi/dev/debug.html", ToasService.getBaseParams(pageFields), (response) -> {
            Document document = Jsoup.parse(response);
            return TOASParser.parsePositions(document);
        });
    }

    private static Map<String, String> getLoginParams(TOASParser.PageFields pageFields) {
        Map<String, String> params = ToasService.getBaseParams(pageFields);
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Tunnus", "");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$Salasana", "");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$ucKayttajatoiminnot$ucKayttajatoiminnot_Kirjautuminen$btnKirjaudu", "Kirjaudu");
        params.put("Hakemuksen_Muokkaus_TAAAAAAAA$hdf_Count", "");
        return params;
    }

    private static Map<String, String> getBaseParams(TOASParser.PageFields pageFields) {
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
}