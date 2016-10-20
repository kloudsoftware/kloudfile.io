package me.probE466.persistence.entities;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "file")
@XmlRootElement
//@NamedQueries({
//        @NamedQuery(name = "File.findAll", query = "SELECT f FROM File f"),
//        @NamedQuery(name = "File.findById", query = "SELECT f FROM File f WHERE f.id = :id"),
//        @NamedQuery(name = "File.findByFileName", query = "SELECT f FROM File f WHERE f.fileName = :fileName"),
//        @NamedQuery(name = "File.findByFileHash", query = "SELECT f FROM File f WHERE f.fileHash = :fileHash"),
//        @NamedQuery(name = "File.findByFilePath", query = "SELECT f FROM File f WHERE f.filePath = :filePath"),
//        @NamedQuery(name = "File.findByIsImage", query = "SELECT f FROM File f WHERE f.isImage = :isImage"),
//        @NamedQuery(name = "File.findByFileUrl", query = "SELECT f FROM File f WHERE f.fileUrl = :fileUrl"),
//        @NamedQuery(name = "File.findByFileDeleteUrl", query = "SELECT f FROM File f WHERE f.fileDeleteUrl = :fileDeleteUrl"),
//        @NamedQuery(name = "File.findByFileViewed", query = "SELECT f FROM File f WHERE f.fileViewed = :fileViewed"),
//        @NamedQuery(name = "File.findByFileDateCreated", query = "SELECT f FROM File f WHERE f.fileDateCreated = :fileDateCreated"),
//        @NamedQuery(name = "File.findByFileDateUpdated", query = "SELECT f FROM File f WHERE f.fileDateUpdated = :fileDateUpdated")})
public class File implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @Column(name = "file_name")
    private String fileName;
    @Basic(optional = false)
    @Column(name = "file_hash")
    private String fileHash;
    @Basic(optional = false)
    @Column(name = "file_path")
    private String filePath;
    @Basic(optional = false)
    @Column(name = "is_image")
    private boolean isImage;
    @Basic(optional = false)
    @Column(name = "file_url")
    private String fileUrl;
    @Basic(optional = false)
    @Column(name = "file_delete_url")
    private String fileDeleteUrl;
    @Basic(optional = false)
    @Column(name = "file_viewed")
    private long fileViewed;
    @Column(name = "file_date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fileDateCreated;
    @Column(name = "file_date_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fileDateUpdated;
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private User userId;

    public File() {
    }

    public File(Integer id) {
        this.id = id;
    }

    public File(Integer id, String fileName, String fileHash, String filePath, boolean isImage, String fileUrl, String fileDeleteUrl, long fileViewed) {
        this.id = id;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.filePath = filePath;
        this.isImage = isImage;
        this.fileUrl = fileUrl;
        this.fileDeleteUrl = fileDeleteUrl;
        this.fileViewed = fileViewed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean getIsImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileDeleteUrl() {
        return fileDeleteUrl;
    }

    public void setFileDeleteUrl(String fileDeleteUrl) {
        this.fileDeleteUrl = fileDeleteUrl;
    }

    public long getFileViewed() {
        return fileViewed;
    }

    public void setFileViewed(long fileViewed) {
        this.fileViewed = fileViewed;
    }

    public Date getFileDateCreated() {
        return fileDateCreated;
    }

    public void setFileDateCreated(Date fileDateCreated) {
        this.fileDateCreated = fileDateCreated;
    }

    public Date getFileDateUpdated() {
        return fileDateUpdated;
    }

    public void setFileDateUpdated(Date fileDateUpdated) {
        this.fileDateUpdated = fileDateUpdated;
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
