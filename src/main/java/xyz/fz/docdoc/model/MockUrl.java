package xyz.fz.docdoc.model;

public class MockUrl {
    private String url;

    private String owner;

    private boolean restful;

    public MockUrl() {
    }

    public MockUrl(String url, String owner, boolean restful) {
        this.url = url;
        this.owner = owner;
        this.restful = restful;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isRestful() {
        return restful;
    }

    public void setRestful(boolean restful) {
        this.restful = restful;
    }
}
