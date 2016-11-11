package me.probE466.web;

import java.util.Date;

public class FileDTO {

    final private Integer id;
    final private String fileUrl;
    final private String fileDeleteUrl;
    final private boolean isViewAble;
    final private String fileExtension;
    final private String fileName;
    private final Date fileDateCreated;
    private final Date fileDateUpdated;

    public FileDTO(Integer id, String fileUrl, String fileDeleteUrl, boolean isViewAble,
                   String fileExtension, String fileName, Date fileDateCreated,
                   Date fileDateUpdated) {
        this.id = id;
        this.fileUrl = fileUrl;
        this.fileDeleteUrl = fileDeleteUrl;
        this.isViewAble = isViewAble;
        this.fileExtension = fileExtension;
        this.fileName = fileName;
        this.fileDateCreated = fileDateCreated;
        this.fileDateUpdated = fileDateUpdated;
    }
}
