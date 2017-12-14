package br.ufpe.cin.if710.podcast.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Walber on 12/12/2017.
 */
@Entity
public class Episode {
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    /*public final static String _ID = "_id";
        public final static String EPISODE_TITLE = "title";
        public final static String EPISODE_DATE = "pubDate";
        public final static String EPISODE_LINK = "link";
        public final static String EPISODE_DESC = "description";
        public final static String EPISODE_DOWNLOAD_LINK = "downloadLink";
        public final static String EPISODE_FILE_URI = "downloadUri";
    */
    @PrimaryKey(autoGenerate = true)
    private int _id;
    private String title;
    private String pubDate;
    private String link;
    private String description;
    private String downloadLink;
    private String downloadUri;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }



}
