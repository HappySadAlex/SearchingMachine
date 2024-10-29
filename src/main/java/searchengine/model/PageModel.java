package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity()
@Table(name = "page", indexes = @Index(name = "`path_index`", columnList = "path"))
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class PageModel implements Serializable {

    @Id
    @Column(columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REMOVE})
    @JoinColumn(name = "site_id", updatable = false, columnDefinition = "INT NOT NULL")
    private SiteModel siteId;

    @Column(name = "path", columnDefinition = "VARCHAR(255) NOT NULL")
    private String path;  // адрес страницы от корня сайта (должен начинаться со слэша, например: /news/372189/)

    @Column(name = "code", columnDefinition = "INT NOT NULL")
    private Integer httpStatusCode;   // код HTTP-ответа, полученный при запросе страницы (например, 200, 404, 500 или другие)

    @Column(columnDefinition = "MEDIUMTEXT NOT NULL")
    private String content;  // контент страницы (HTML-код)

    @OneToMany(mappedBy = "pageId", cascade={CascadeType.MERGE, CascadeType.REMOVE})
    private List<IndexModel> indexes;

}
