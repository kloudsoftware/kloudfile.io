package me.probE466.persistence.services;


import me.probE466.persistence.entities.File;
import me.probE466.persistence.repos.FileRepository;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    private final java.io.File fileDir = new java.io.File("files");
    private final java.io.File imageDir = new java.io.File("images");


    public boolean createFile(final java.io.FileInputStream fsin, String fileName) {
        File dstFile = new File();
        String hash = "";
        try {
            hash = calcSHA1(fsin);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (checkIfFileExists(hash)) {
            throw new EntityExistsException();
        }
        dstFile.setIsImage(isImage(fileName));
        dstFile.setHash(hash);
        dstFile.setName(fileName);
        dstFile.setPath(saveFile(fsin, fileName, dstFile.getIsImage()));
        fileRepository.save(dstFile);
        return true;
    }

    private boolean isImage(String fileName) {
        String[] strArr = fileName.split("\\.");
        if (strArr.length < 1) {
            throw new UnsupportedOperationException();
        }
        String ext = strArr[strArr.length - 1];
        return ext.equals(".png") || ext.equals(".jpg") || ext.equals(".bmp") || ext.equals(".gif");
    }

    private String saveFile(java.io.FileInputStream fsin, String fileName, boolean isImage) {
        String path = "";
        java.io.File svFile;
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        if (!imageDir.exists()) {
            imageDir.mkdir();
        }
        if (isImage) {
            svFile = new java.io.File(imageDir.getPath(), fileName);
            try (FileOutputStream fout = new FileOutputStream(svFile)) {
                IOUtils.copy(fsin, fout);
                fout.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    private boolean checkIfFileExists(final String hash) {
        return fileRepository.findAll().stream().anyMatch(file -> file.getHash().equals(hash));
    }

    private String calcSHA1(final java.io.FileInputStream input) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-256");
        input.mark(input.available());
        byte[] buffer = new byte[8192];
        int len = input.read(buffer);

        while (len != -1) {
            sha1.update(buffer, 0, len);
            len = input.read(buffer);
        }
        input.reset();
        return new HexBinaryAdapter().marshal(sha1.digest());
    }

}
