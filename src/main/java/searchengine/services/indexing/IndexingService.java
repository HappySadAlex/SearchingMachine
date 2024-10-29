package searchengine.services.indexing;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {

    void deleteAllSitesData();
    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    IndexingResponse indexPage(String url);

}
