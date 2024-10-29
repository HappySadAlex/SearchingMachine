package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;

import java.util.List;
import java.util.Set;

@Repository
public interface IndexRepository extends JpaRepository<IndexModel, Integer> {

    @Query
    Integer findCountByLemmaId(LemmaModel lemmaModel);

    List<IndexModel> findIndexByPageId(PageModel pageId);

    @Query(value = "select * from search_engine.index_table i where i.page_id = :pageId and i.lemma_id in :lemmas", nativeQuery = true)
    List<IndexModel> findIndexByPageIdAndLemmas(@Param("pageId")Integer pageId, @Param("lemmas") List<Integer> lemmas);
}
