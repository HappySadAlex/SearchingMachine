package searchengine.config;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "src/main/java/searchengine")
public class JsoupSession {

    @Value("${user-agent}")
    String userAgent;
    @Value("${refer}")
    String refer;

    @Bean
    public Connection JsoupConnection(){
        return Jsoup.newSession().userAgent(userAgent).referrer(refer);
    }

}
