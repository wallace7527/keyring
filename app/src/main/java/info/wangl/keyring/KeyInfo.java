package info.wangl.keyring;

public class KeyInfo {
    public int _id;
    public int catalog;
    public String title;
    public String username;
    public String password;
    public String url;
    public String notes;
    public byte[] image;

    public KeyInfo() {

    }

    public KeyInfo(int catalog, String title, String username, String password, String url, String notes, byte[] image) {
        this.catalog = catalog;
        this.title = title;
        this.username = username;
        this.password = password;
        this.url = url;
        this.notes = notes;
        this.image = image;
    }

}
