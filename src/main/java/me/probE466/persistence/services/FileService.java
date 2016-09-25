package me.probE466.persistence.services;


import me.probE466.persistence.entities.File;
import me.probE466.persistence.repos.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FileService {

    @Autowired
    FileRepository fileRepository;

    public boolean createFile(java.io.File file) {
        File dstFile = new File();
        dstFile.setIsImage(false);
        dstFile.setHash("1234");
        dstFile.setName("fileserviceTestFile");
        dstFile.setPath("samplePath");
        fileRepository.save(dstFile);
        return true;
    }
}
