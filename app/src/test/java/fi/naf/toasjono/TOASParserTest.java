package fi.naf.toasjono;

import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class TOASParserTest {
    @Test
    public void addition_isCorrect() throws Exception {
        java.net.URL url = this.getClass().getClassLoader().getResource("test.html");
        String html = new java.util.Scanner(new File(url.toURI()),"UTF8").useDelimiter("\\Z").next();

        List<TOASPosition> actual = TOASParser.parsePositions(Jsoup.parse(html));

        assertEquals(14, actual.size());
    }
}

