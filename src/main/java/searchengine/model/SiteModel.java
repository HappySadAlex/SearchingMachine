package searchengine.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "site")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class SiteModel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "INT NOT NULL AUTO_INCREMENT")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL ")
    private StatusType status;

    @Column(name = "status_time", columnDefinition = "DATETIME NOT NULL")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String errorText;   // текст ошибки индексации или NULL, если её не было;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL")
    private String url;  // адрес главной страницы сайта;

    @Column(name = "name", columnDefinition = "VARCHAR(255) NOT NULL")
    private String siteName;

    @OneToMany(cascade={CascadeType.MERGE, CascadeType.REMOVE}, mappedBy = "siteId")
    private List<PageModel> pages;


    public enum StatusType {
        INDEXING,
        INDEXED,
        FAILED;
    }
}

