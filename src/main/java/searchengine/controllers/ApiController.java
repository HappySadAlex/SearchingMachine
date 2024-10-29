package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.searching.SearchingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.searching.SearchingService;
import searchengine.services.statistics.StatisticsService;

import java.util.function.Supplier;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchingService searchingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchingService searchingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchingService = searchingService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() throws InterruptedException {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing(){
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url){
        return ResponseEntity.ok(indexingService.indexPage(url));
    }

    @GetMapping("/delete")
    public ResponseEntity<IndexingResponse> deleteAll(){
        indexingService.deleteAllSitesData();
        return ResponseEntity.ok(IndexingResponse.builder().result(true).error("All deleted!").build());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchingResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "") String site,
            @RequestParam Integer offset,
            @RequestParam Integer limit){

        SearchingResponse searchingResponse = time(()->searchingService.search(query, site, offset, limit));
        return ResponseEntity.ok(searchingResponse);
    }

    public <T> T time(Supplier<T> supplier){
        long start = System.currentTimeMillis();
        T t = supplier.get();
        long finish = System.currentTimeMillis();
        System.out.println("Время: " + (finish - start));
        return t;
    }

}
