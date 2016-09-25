package me.probE466.persistence.entities;

/**
 * Created by larsg on 25.09.2016.
 */
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author larsg
 */
@Entity
@Table(name = "file")
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "File.findAll", query = "SELECT f FROM File f"),
        @NamedQuery(name = "File.findById", query = "SELECT f FROM File f WHERE f.id = :id"),
        @NamedQuery(name = "File.findByName", query = "SELECT f FROM File f WHERE f.name = :name"),
        @NamedQuery(name = "File.findByHash", query = "SELECT f FROM File f WHERE f.hash = :hash"),
        @NamedQuery(name = "File.findByPath", query = "SELECT f FROM File f WHERE f.path = :path"),
        @NamedQuery(name = "File.findByIsImage", query = "SELECT f FROM File f WHERE f.isImage = :isImage")})
public class File implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @Column(name = "hash")
    private String hash;
    @Basic(optional = false)
    @Column(name = "path")
    private String path;
    @Basic(optional = false)
    @Column(name = "is_image")
    private boolean isImage;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public File() {
    }

    public File(Integer id) {
        this.id = id;
    }

    public File(Integer id, String name, String hash, String path, boolean isImage) {
        this.id = id;
        this.name = name;
        this.hash = hash;
        this.path = path;
        this.isImage = isImage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof File)) {
            return false;
        }
        File other = (File) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "push.File[ id=" + id + " ]";
    }

}

