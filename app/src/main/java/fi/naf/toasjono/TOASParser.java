package fi.naf.toasjono;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

public class TOASParser {
    public static class PageFields {
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
    private static TOASPosition parseRow(Element row) {
        Elements cols = row.select("td");
        String[] parts = cols.get(0).html().split(" ");
        int id = Integer.parseInt(parts[0]);
        String house = parts[1];
        int queue = Integer.parseInt(cols.get(1).html());
        return new TOASPosition(id, house, queue);
    }

    public static List<TOASPosition> parsePositions(Document document) {
        Element applications = document.select("#Hakemuksen_Muokkaus_TAAAAAAAA_gv_ApplicationQueues").first();
        Elements rows = applications.select("tr");
        List<Element> withoutHeader = rows.subList(1, rows.size());
        return withoutHeader.stream().map(TOASParser::parseRow).collect(Collectors.toList());
    }
    public static String parseMuokkausCount(Document document) {
        return document.select("[id~=(.+)hdf_Count").attr("value");
    }
    public static PageFields parsePageFields(Document document) {
        String VIEWSTATE_SELECTOR = "#__VIEWSTATE";
        String EVENTVALIDATION_SELECTOR = "#__EVENTVALIDATION";
        String VIEWSTATEGENERATOR_SELECTOR = "#__VIEWSTATEGENERATOR";
        String viewState = document.select(VIEWSTATE_SELECTOR).attr("value");
        String eventValidate = document.select(EVENTVALIDATION_SELECTOR).attr("value");
        String viewStateGenerator = document.select(VIEWSTATEGENERATOR_SELECTOR).attr("value");
        return new PageFields(viewState, eventValidate, viewStateGenerator);
    }
}

