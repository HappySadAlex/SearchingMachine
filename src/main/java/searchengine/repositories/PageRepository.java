package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Repository
public interface PageRepository extends JpaRepository<PageModel, Integer> {

    void deleteAllBySiteId(SiteModel siteModel);

    boolean existsByPath(String path);

    @Query
    Integer countBySiteId(SiteModel model);

    @Query(value = """
            SELECT *
            FROM search_engine.page
            WHERE id IN (
                SELECT page_id
                FROM search_engine.index_table
                WHERE lemma_id IN (
                    SELECT l.id
                    FROM search_engine.lemma l
                    JOIN search_engine.page p ON l.site_id = p.site_id
                    WHERE l.lemma IN :lemmas
                    GROUP BY l.id, l.frequency, l.site_id
                    HAVING (l.frequency / COUNT(p.site_id)) < 0.95
                    ORDER BY  l.frequency DESC
                )
                GROUP BY page_id
                HAVING COUNT(DISTINCT lemma_id) = :lemmaCount
            )
            AND site_id = CASE
            WHEN :siteId = 0 THEN site_id
            ELSE :siteId END""", nativeQuery = true)
    List<PageModel> findPagesWithLemmasAndSite(@Param("lemmas") Set<String> lemmas, @Param("siteId") Integer siteModelId, @Param("lemmaCount")Integer lemmaCount);

    @Query(value = """
            SELECT *
            FROM search_engine.page
            WHERE id IN (
                SELECT page_id
                FROM search_engine.index_table
                WHERE lemma_id IN :lemmas
                GROUP BY page_id
                HAVING COUNT(DISTINCT lemma_id) = :lemmaCount
            )""", nativeQuery = true)
    List<PageModel> findPagesWithLemmas(@Param("lemmas") List<Integer> lemmasIds, @Param("lemmaCount")Integer lemmaCount);
}
