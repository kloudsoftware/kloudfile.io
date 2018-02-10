package io.kloudfile.persistence.entities;

import javax.persistence.*;

@Entity
@Table(name = "url")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public Url(String linkRef) {
        this.linkRef = linkRef;
    }

    private String url;

    private String linkRef;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLinkRef() {
        return linkRef;
    }

    public void setLinkRef(String linkRef) {
        this.linkRef = linkRef;
    }
}
