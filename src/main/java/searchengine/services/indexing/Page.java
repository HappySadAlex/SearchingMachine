package searchengine.services.indexing;

import lombok.*;

import java.util.HashSet;
import java.util.Objects;

public class Page {
    private String url = "";
    private HashSet<Page> childPages = new HashSet<>();
    private boolean isRootPage = false;
    private boolean isVisited = false;

    public Page(String url) {
        this.url = url;
    }
    public Page(String url, HashSet<Page> pages) {
        this.url = url;
        this.childPages = pages;
    }

    public Page(String url, boolean isRootPage, boolean isVisited) {
        this.url = url;
        this.isRootPage = isRootPage;
        this.isVisited = isVisited;
    }

    public void addChild(String url){
        Page childPage = new Page(url);
        childPages.add(childPage);
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public HashSet<Page> getChildPages() {
        return childPages;
    }
    public void setChildPages(HashSet<Page> childPages) {
        this.childPages = childPages;
    }

    public boolean isRootPage() {
        return isRootPage;
    }

    public void setRootPage(boolean rootPage) {
        isRootPage = rootPage;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    @Override
    public String toString() {
        return "url='" + url + '\'' + (childPages.isEmpty() ? "" : " Pages on url:\n " + childPages);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Page page = (Page) obj;
        return this.url.equals(page.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, childPages, isRootPage, isVisited);
    }
}