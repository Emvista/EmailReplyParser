package com.emvista.emailreplyparser.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HtmlFilter {

    public List<String> getTableElements(String htmlText) {
        Document doc = Jsoup.parse(htmlText);
        List<String> l = new ArrayList<>();
        Elements newsHeadlines = doc.getElementsByTag("td");
        for (Element headline : newsHeadlines) {
            String text = headline.text();
            if (!text.isEmpty()) {
                l.add(text);
            }

        }
        return l;
    }
}
