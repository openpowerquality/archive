package utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * Utility class to create an "inlined" CSS version of an Html document.
 * Original credit to StackOverflow user Grekz:
 * http://stackoverflow.com/questions/4521557/automatically-convert-style-sheets-to-inline-style
 *
 */
public class CssInliner {
    /**
     * Will convert an Html string containing internal CSS rules into one with only inline css rules.
     *
     * @param html The html string to convert.
     * @return The converted inline-only Html string.
     */
    public static String toInlinedCss (String html) {
        String parsedHtml = removeComments(html); //First must remove CSS comment lines because doesn't play nicely with Jsoup.
        final String style = "style";
        Document doc = Jsoup.parse(parsedHtml);
        Elements els = doc.select(style); //Grab all style elements.
        for (Element e : els) {
            String styleRules = e.getAllElements().get(0).data().replaceAll("\n", "").trim();
            String delims = "{}";
            StringTokenizer st = new StringTokenizer(styleRules, delims);
            while (st.countTokens() > 1) {
                String selector = st.nextToken(), properties = st.nextToken();
                if (!selector.contains(":")) { // Skip rules with ":" in it.
                    Elements selectedElements = doc.select(selector);
                    for (Element selElem : selectedElements) {
                        String oldProperties = selElem.attr(style);
                        selElem.attr(style, oldProperties.length() > 0 ? concatenateProperties(oldProperties, properties) : properties);
                    }
                }
            }
            e.remove();
        }
        return doc.toString();
    }

    /**
     * Removes all CSS styled comment lines from Html string.
     *
     * @param html The Html string to parse.
     * @return The cleaned up Html string.
     */
    public static String removeComments(String html) {
        Scanner scanner = new Scanner(html);
        StringBuilder sb = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.contains("/*") && !line.contains("*/")) {
                sb.append(line);
            }
        }

        return sb.toString();
    }

    private static String concatenateProperties(String oldProp, String newProp) {
        oldProp = oldProp.trim();
        if (!oldProp.endsWith(";"))
            oldProp += ";";
        return oldProp + newProp.replaceAll("\\s{2,}", " ");
    }
}
