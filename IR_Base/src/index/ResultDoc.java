package index;

public class ResultDoc {
    private int _id;
    private String _title = "[no title]";
    private String _content = "[no content]";
    private String _wos = "[no wos]";

    public ResultDoc(int id) {
        _id = id;
    }

    public int id() {
        return _id;
    }

    public String title() {
        return _title;
    }

    public ResultDoc title(String nTitle) {
        _title = nTitle;
        return this;
    }

    public String content() {
        return _content;
    }
    
    public ResultDoc content(String nContent) {
        _content = nContent;
        return this;
    }

    public String wos() {
        return _wos;
    }
    
    public ResultDoc wos(String nWos) {
        _wos = nWos;
        return this;
    }
}
