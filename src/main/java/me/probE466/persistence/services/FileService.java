package me.probE466.persistence.services;


import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.FileRepository;
import me.probE466.persistence.repos.UserRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.tomcat.jni.Buffer;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class FileService {


    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    private final java.io.File fileDir = new java.io.File(System.getProperty("user.home") + "/push/files");
    private final java.io.File imageDir = new java.io.File(System.getProperty("user.home") + "/push/images");
    private SecureRandom random = new SecureRandom();


    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public String createFile(InputStream fsin, final String fileName, User user) {
        File dstFile = new File();
        String hash = "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = null;
        try {
            IOUtils.copy(fsin, baos);
            bytes = baos.toByteArray();
            ByteArrayInputStream bsin = new ByteArrayInputStream(bytes);
            hash = calcSHA2(bsin);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (checkIfFileExists(hash)) {
            throw new EntityExistsException();
        }
        dstFile.setIsImage(isImage(fileName));
        dstFile.setFileHash(hash);
        dstFile.setFileName(fileName);
        dstFile.setFilePath(saveFile(new ByteArrayInputStream(bytes != null ? bytes : new byte[0]), fileName, dstFile.getIsImage()));
        dstFile.setUserId(user);
        dstFile.setFileUrl(generateFileUrl());
        fileRepository.save(dstFile);
        user.getFileList().add(dstFile);
        userRepository.save(user);
        String returnString = "";
        if(dstFile.getIsImage()) {
            returnString += "/img/";
        } else {
            returnString += "/file/";
        }
        returnString += dstFile.getFileUrl();
        return returnString;
    }

    private String generateFileUrl() {
        boolean done = false;
        float probability = 6.0f;
        while (!done) {
            String tmpUrl = RandomStringUtils.randomAlphanumeric((int) probability);
            boolean collided = fileRepository.findByFileUrl(tmpUrl).isPresent();
            if (!collided) {
                return tmpUrl;
            }
            probability += 0.5f;
            if (probability == 10.0f) {
                done = true;
            }
        }
        return UUID.randomUUID().toString();
    }

    private boolean isImage(final String fileName) {
        String[] strArr = fileName.split("\\.");
        if (strArr.length <= 1) {
            throw new UnsupportedOperationException();
        }
        String ext = strArr[strArr.length - 1].toLowerCase();
        return ext.equals("png") || ext.equals("jpg") || ext.equals("bmp") || ext.equals("gif");
    }

    private String saveFile(InputStream fsin, String fileName, boolean isImage) {
        java.io.File svFile;
        fileName = fileName.replaceAll("\\s+", "");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }
        if (isImage) {
            svFile = new java.io.File(imageDir.getPath(), fileName);
        } else {
            svFile = new java.io.File(fileDir.getPath(), fileName);
        }
        try {
            svFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (FileOutputStream fout = new FileOutputStream(svFile)) {
            IOUtils.copy(fsin, fout);
            fout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return svFile.getPath();
    }

    private boolean checkIfFileExists(final String hash) {
        return fileRepository.findByFileHash(hash).isPresent();
//        return fileRepository.findAll().stream().anyMatch(file -> file.getHash().equals(hash));
    }

    private String calcSHA2(InputStream input) throws IOException, NoSuchAlgorithmException {
        MessageDigest sha2 = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int len = input.read(buffer);

        while (len != -1) {
            sha2.update(buffer, 0, len);
            len = input.read(buffer);
        }

        return new HexBinaryAdapter().marshal(sha2.digest());
    }

}
