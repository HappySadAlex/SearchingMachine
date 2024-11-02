package searchengine.services.indexing;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.JsoupSession;
import searchengine.features.LemmaFinder;
import searchengine.model.IndexModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;

@Slf4j
public class IndexingTask extends RecursiveTask<Set<PageModel>> {

    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;
    private static JsoupSession connector;
    private static PageRepository pageRepository;
    private static LemmaFinder lematizator;
    public static AtomicBoolean isIndexing = IndexingServiceImpl.isIndexing;
    private final String domain; //Main page
    private final SiteModel model;
    public final static ExecutorService executor = Executors.newFixedThreadPool(20);
    public Page page;
    public Set<String> visited;
    public Set<PageModel> pageModels = ConcurrentHashMap.newKeySet();

    //Constructors
    public IndexingTask(PageRepository pageRepo, IndexRepository indexRepository, LemmaRepository lemmaRepository,
                        Page page, Set<String> visited, String domain, SiteModel siteModel, JsoupSession connector, LemmaFinder lemmaFinder) {
        IndexingTask.lemmaRepository = lemmaRepository;
        IndexingTask.indexRepository = indexRepository;
        IndexingTask.pageRepository = pageRepo;
        IndexingTask.connector = connector;
        IndexingTask.lematizator = lemmaFinder;
        this.page = page;
        this.visited = visited;
        this.domain = domain;
        this.model = siteModel;
    }

    private IndexingTask(String domain, SiteModel model, Page page, Set<String> visited) {
        this.domain = domain;
        this.model = model;
        this.page = page;
        this.visited = visited;
    }

    public LinkedHashSet<Page> getPagesOnPage(Page page) throws IOException {
        try {
            Thread.sleep(150);
        }catch (InterruptedException e){
            log.info("ForkJoin thread is interrupted!");
        }
        LinkedHashSet<Page> pages = new LinkedHashSet<>();
        Elements elements = saveAllPageInfo(page, domain, model);
        page.setVisited(true);
        for (Element e : elements) {
            String url = e.absUrl("href").replace("/$", " ");
            boolean validUrl = url.contains(domain) && !url.contains("#")
                    && !url.equals(page.getUrl()) && !visited.contains(url) && !url.contains(".sql") &&
                    !url.contains(".zip") && !url.contains(".yaml") && !url.contains(".jpg") && !url.contains(".pdf");
            if (validUrl) pages.add(new Page(url));
        }
        page.setChildPages(pages);
        return pages;
    }

    @SneakyThrows
    @Override
    protected Set<PageModel> compute() {
        if(isIndexing.get()) {
            //result.append(page.getUrl());
            Set<IndexingTask> taskListForPage = ConcurrentHashMap.newKeySet();
            visited.add(page.getUrl());
            // заполнение массива подзадач
            for (Page child : getPagesOnPage(page)) {
                //log.info("Child: {}", child.getUrl());
                if ((!child.isVisited() || !visited.contains(child.getUrl())) && isIndexing.get()) {
                    IndexingTask subTask = new IndexingTask(domain, model, child, visited);
                    visited.add(child.getUrl());
                    subTask.fork();
                    taskListForPage.add(subTask);
                }
            }
            // вывод
            if (!taskListForPage.isEmpty()) {
                for (Page p : page.getChildPages()) {
                    for (IndexingTask m : taskListForPage) {
                        if (p.getUrl().equals(m.page.getUrl()) && isIndexing.get()) {
                            try {
                                Thread.sleep(150);
                                m.join();
                            }catch (InterruptedException ignored){}
                        }
                    }
                }
            }
        }
        return pageModels;
    }

    public static Elements saveAllPageInfo(Page page, String domain, SiteModel model) throws IOException {
        Connection.Response response = connector
                .JsoupConnection()
                .newRequest()
                .url(page.getUrl())
                .execute();
        Document doc = response.parse();
        Elements elements = doc.select("a");
        if (!isIndexing.get()) executor.shutdownNow();
        PageModel pageModel = PageModel.builder()
                .path(page.getUrl().replace(domain, ""))
                .content(doc.toString())
                .httpStatusCode(response.statusCode())
                .siteId(model)
                .build();
        pageRepository.save(pageModel);
        saveLemmas(doc, model, pageModel);
        return elements;
    }

    public static void saveLemmas(Document doc, SiteModel model, PageModel pageModel){
        executor.execute(()->{
            Map<String, Integer> lemmas = lematizator.deleteTagsAndCollect(doc.toString());
            lemmas.forEach((k, v)->{
                lemmaRepository.updateOrInsertLemma(model.getId(), k);
                IndexModel index = IndexModel.builder()
                        .lemmaId(lemmaRepository.findByLemma(k)) // лемма
                        .rank(v) // кол-во данной леммы на странице
                        .pageId(pageModel) // страница
                        .build();
                indexRepository.save(index);
            });
            lemmaRepository.flush();
            indexRepository.flush();
            log.info("--- For page: {} - all saved in db", pageModel.getPath());
        });
    }
}