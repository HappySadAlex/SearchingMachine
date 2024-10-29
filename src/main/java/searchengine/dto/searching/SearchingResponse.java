package searchengine.dto.searching;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchingResponse {

    private boolean result;
    private Integer count;
    private List<SearchingData> data;

}
