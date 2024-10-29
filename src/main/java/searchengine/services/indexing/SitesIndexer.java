package searchengine.services.indexing;


import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.PageRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.concurrent.*;


public class SitesIndexer implements Callable<Boolean> {

    public volatile boolean isIndexing = IndexingServiceImpl.isIndexing;

    private final PageRepository pageRepository;
    private final String siteUrl;
    protected SiteModel siteModel;

    public SitesIndexer(PageRepository pageRepository, String url, SiteModel model){
        this.pageRepository = pageRepository;
        siteUrl = url;
        siteModel = model;
    }


    @Transactional
    @Override
    public Boolean call() {
        System.out.println(siteModel.getSiteName());





        return true;
    }
}
