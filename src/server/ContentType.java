package server;

public enum ContentType {
    TEXT_PLAIN("text/plain; charset=utf-8"),
    TEXT_HTML("text/html; charset=utf-8"),
    TEXT_CSS("text/css"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_SVG("image/svg+xml");

    private final String descr;

    ContentType(String descr) {
        this.descr =  descr;
    }

    @Override
    public String toString() {
        return descr;
    }
}
