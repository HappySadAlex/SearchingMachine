package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "`lemma`")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LemmaModel implements Serializable {

    @Id
    @Column(columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "site_id", updatable = false, columnDefinition = "INT NOT NULL")
    private SiteModel siteId; // ID веб-сайта из таблицы site

    @Column(name = "lemma", columnDefinition = "VARCHAR(255) NOT NULL", unique = true)
    private String lemma;  // нормальная форма слова (лемма)

    @Column(name = "frequency", columnDefinition = "INT NOT NULL")
    private Integer frequency;   // количество страниц, на которых слово встречается хотя бы один раз. Максимальное значение не может превышать общее количество слов на сайте

    @OneToMany(mappedBy = "lemmaId", cascade={CascadeType.MERGE, CascadeType.REMOVE})
    private List<IndexModel> indexes;

}
