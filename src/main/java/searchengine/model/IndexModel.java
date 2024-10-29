package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "`index_table`", indexes = {@Index(name = "`page_index`", columnList = "`page_id`"), @Index(name = "`lemma_index`", columnList = "`lemma_id`")})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IndexModel implements Serializable {

    @Id
    @Column(columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "page_id", updatable = false, columnDefinition = "INT NOT NULL")
    private PageModel pageId; // идентификатор страницы

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "lemma_id", columnDefinition = "INT NOT NULL")
    private LemmaModel lemmaId;  // идентификатор леммы

    @Column(name = "`rank`", columnDefinition = "FLOAT NOT NULL")
    private Integer rank;   // количество данной леммы для данной страницы



}
