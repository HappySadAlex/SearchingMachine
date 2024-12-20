package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;

    private final SiteRepository siteRepo;

    private final PageRepository pageRepo;

    private final IndexRepository indexRepo;

    private final LemmaRepository lemmaRepo;

    public StatisticsServiceImpl(SitesList sites, SiteRepository siteRepo, PageRepository pageRepo, IndexRepository indexRepo, LemmaRepository lemmaRepo) {
        this.sites = sites;
        this.siteRepo = siteRepo;
        this.pageRepo = pageRepo;
        this.indexRepo = indexRepo;
        this.lemmaRepo = lemmaRepo;
    }

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            SiteModel model = siteRepo.findSiteByUrl(site.getUrl());
            int pages = pageRepo.countBySiteId(model);
            int lemmas = lemmaRepo.countBySiteId(model);
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(model == null ? "Not indexed" : model.getStatus().toString());
            item.setError(model == null ? "No errors" : model.getErrorText());
            item.setStatusTime(model != null ? (System.currentTimeMillis() -
                    model.getStatusTime().getSecond()*1000) : System.currentTimeMillis()*1000);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
