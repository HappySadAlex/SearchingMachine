package searchengine.config;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import searchengine.features.LemmaFinder;

import java.io.IOException;

@Configuration
@ComponentScan(basePackages = "src/main/java/searchengine")
public class LemmaFinderConf {

    @Bean
    public LemmaFinder LemmaFinder() throws IOException {
        return LemmaFinder.getInstance();
    }

}
