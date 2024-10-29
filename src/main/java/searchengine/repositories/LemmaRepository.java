package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.LemmaModel;
import searchengine.model.SiteModel;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaModel, Integer> {

    @Query
    Integer countBySiteId(SiteModel model);

    @Modifying
    @Transactional
    @Query(value = "Update search_engine.lemma l set l.frequency = (l.frequency + 1) Where l.lemma = :lemma ", nativeQuery = true)
    void updateLemmasFrequencyWhereLemma(@Param("lemma") String lemma);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO search_engine.lemma (site_id, lemma, frequency) VALUES (:siteId, :lemma, 1) ON DUPLICATE KEY UPDATE frequency = (frequency + 1)", nativeQuery = true)
    void updateOrInsertLemma(@Param("siteId") Integer siteId, @Param("lemma") String lemma);

    @Query
    LemmaModel findByLemma(String lemma);

    @Query(value = "select * from search_engine.lemma l where l.lemma in :lemmas order by frequency", nativeQuery = true)
    List<LemmaModel> findLemmasInSet(@Param("lemmas") Set<String> lemmas);

}
