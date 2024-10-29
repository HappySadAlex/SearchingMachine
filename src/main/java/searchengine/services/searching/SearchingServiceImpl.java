package searchengine.services.searching;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.searching.SearchingData;
import searchengine.dto.searching.SearchingResponse;
import searchengine.features.LemmaFinder;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SearchingServiceImpl implements SearchingService{
    private static final Logger log = LoggerFactory.getLogger(SearchingServiceImpl.class);
    @Autowired
    private final LemmaRepository lemmaRepository;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final IndexRepository indexRepository;
    @Autowired
    private final LemmaFinder lemmatizator;


    @Override
    public SearchingResponse search(String query, String siteUrl, Integer offset, Integer limit) {
        Set<String> lemmas = lemmatizator.getLemmaSet(query);
        log.info("Lemmas: {}", lemmas);
        List<Integer> lemmasIds = lemmaRepository.findLemmasInSet(lemmas)
                .stream()
                .map(LemmaModel::getId)
                .collect(Collectors.toCollection(ArrayList::new));
        SiteModel siteModel = siteUrl.isEmpty() ? null : siteRepository.findSiteByUrl(siteUrl);
        List<PageModel> pagesWithLemmas = new ArrayList<>(
                pageRepository.findPagesWithLemmasAndSite(
                        lemmas,
                        siteModel == null ? 0 : siteModel.getId(),
                        query.split("\\s+").length)
        );
        //передаем в метод страницы и леммы для нахождения релевантности и создания списка с ответами на запрос
        return makeSearchingResponse(pagesWithLemmas, lemmas, lemmasIds, siteModel, query);
    }

    public SearchingResponse makeSearchingResponse(List<PageModel> pages, Set<String> lemmas, List<Integer> lemmasIds, SiteModel siteModel, String query){
        List<SearchingData> dataList = new ArrayList<>();
        Map<PageModel, Integer> pageWithAbsRelevance = new HashMap<>();
        AtomicInteger maxRankSum = new AtomicInteger(0);
        pages.forEach(p->{
            List<IndexModel> indexes = indexRepository.findIndexByPageIdAndLemmas(p.getId(), lemmasIds);
            int sumOfRanks = indexes.stream().mapToInt(IndexModel::getRank).sum();
            pageWithAbsRelevance.put(p, sumOfRanks);
            if(sumOfRanks > maxRankSum.get()){
                maxRankSum.set(sumOfRanks);
            }
        });
        pageWithAbsRelevance.forEach((page, pageRank) -> {
            log.info("Page: {}", page.getPath());
            String content = page.getContent();
            SearchingData searchingData = SearchingData.builder()
                    .site(siteModel != null ? siteModel.getUrl() : siteRepository.findById(page.getSiteId().getId()).map(SiteModel::getUrl).get())
                    .siteName(siteModel != null ? siteModel.getSiteName() : siteRepository.findById(page.getSiteId().getId()).map(SiteModel::getSiteName).get())
                    .uri(page.getPath())
                    .title(content.substring((content.indexOf("<title>"))+7, content.indexOf("</title>")))
                    .snippet(createSnippet(content, lemmas, query))
                    .relevance(pageRank.doubleValue() / maxRankSum.doubleValue())
                    .build();
            //log.info("Key page: {} \t Value: {}", page.getPath(), pageRank);
            if(!searchingData.getSnippet().isEmpty()) {
                dataList.add(searchingData);
            }
        });
        SearchingResponse response = SearchingResponse.builder()
                .result(true)
                .data(dataList
                        .stream()
                        .sorted(Comparator.comparingDouble(SearchingData::getRelevance).reversed())
                        .toList())
                .count(dataList.size())
                .build();
        // формирование ответа с отсортированными страницами по релевантности
        /*log.info("---Sorted dataList---");
        response.getData().forEach(d->{
            log.info("Data - page path: {} \t page relevance: {}", d.getUri(), d.getRelevance());
        });*/
        return response;
    }

    public String createSnippet(String content, Set<String> lemmas, String query) {
        String snippet = "";
        Document doc = Jsoup.parse(content);
        String metaDescription = doc.select("meta[name=description]").attr("content");
        if(metaDescription.contains(query) || containsAny(metaDescription, lemmas)){
            if(metaDescription.contains(query)){
                snippet = metaDescription.replaceAll(query, "<b>"+query+"</b>");
                return snippet;
            }
            if(containsAny(metaDescription, lemmas)){
                snippet = metaDescription;
                for(String l : lemmas){
                    snippet = snippet.replaceAll(l, "<b>"+l+"</b>");
                }
                return snippet;
            }
        }
        else{
            // Если в мета-тегах ничего нет, ищем в основном тексте
            String text = lemmatizator.deleteAllHtmlTags(content);
            String sentenceRegex = "[^.!?]*[.!?]";
            Pattern pattern = Pattern.compile(sentenceRegex);
            Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    String sentence = matcher.group().trim();
                    if(sentence.length() > 300) {
                        sentence = sentence.substring(0, 300) + "...";
                    }
                    if(sentence.contains(query)){
                        return sentence.replace(query, "<b>"+ query +"</b>");
                    }
                    for(String word : lemmas){
                        if(sentence.contains(word)){
                            snippet = sentence.replaceAll(word, "<b>"+ word +"</b>");
                        }
                    }
                    if(!snippet.isEmpty()) return snippet;
                }
            }
        return snippet;
    }

    public static boolean containsAny(String text, Collection<String> lemmas) {
        for (String lemma : lemmas) {
            if (text.contains(lemma)) {
                return true; // Возвращаем true, если найдено хотя бы одно совпадение
            }
        }
        return false; // Возвращаем false, если ничего не найдено
    }




}
