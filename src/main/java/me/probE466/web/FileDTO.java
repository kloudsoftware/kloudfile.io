package me.probE466.web;

import java.util.Date;

public class FileDTO {

    private Integer id;
    private String fileUrl;
    private String fileDeleteUrl;
    private boolean isViewAble;
    private String fileExtension;
    private String fileName;
    private Date fileDateCreated;
    private Date fileDateUpdated;
    private long viewCount;

    public FileDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public boolean isViewAble() {
        return isViewAble;
    }

    public void setViewAble(boolean viewAble) {
        isViewAble = viewAble;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    public FileDTO(Integer id, String fileUrl, String fileDeleteUrl, boolean isViewAble,
                   String fileExtension, String fileName, Date fileDateCreated,
                   Date fileDateUpdated, long viewCount) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileDeleteUrl = fileDeleteUrl;
        this.isViewAble = isViewAble;
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.fileDateCreated = fileDateCreated;
        this.fileDateUpdated = fileDateUpdated;
        this.viewCount = viewCount;
    }
}
