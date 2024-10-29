package searchengine.services.searching;

import searchengine.dto.searching.SearchingResponse;

public interface SearchingService {

    SearchingResponse search(String query, String siteUrl, Integer offset, Integer limit);

}
