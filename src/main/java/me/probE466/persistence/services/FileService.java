package me.probE466.persistence.services;


import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.FileRepository;
import me.probE466.persistence.repos.UserRepository;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityExistsException;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class FileService {


    private final java.io.File fileDir = new java.io.File(System.getProperty("user.home") + "/push/files");
    private final java.io.File imageDir = new java.io.File(System.getProperty("user.home") + "/push/images");
    private final java.io.File pushDir = new java.io.File(System.getProperty("user.home") + "/push");

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserRepository userRepository;

    public FileRepository getFileRepository() {
        return fileRepository;
    }

    public String createFile(java.io.File fsinFile, final String fileName, User user) throws IOException, NoSuchAlgorithmException {
        File dstFile = new File();
        String hash;
        try (FileInputStream hashStream = new FileInputStream(fsinFile)) {
            hash = calcSHA2(hashStream);
        }
        if (fileRepository.findByFileHash(hash).isPresent()) {
            File foundFile = fileRepository.findByFileHash(hash).get();
            String foundUrl = "";
            if (foundFile.getIsImage()) {
                foundUrl += "/img/";
            } else {
                foundUrl += "/file/";
            }
            foundUrl += foundFile.getFileUrl();
            return foundUrl;
        }
        dstFile.setIsImage(isImage(fileName));
        dstFile.setFileHash(hash);
        dstFile.setFileName(fileName);
        dstFile.setFileUrl(generateFileUrl());
        try (FileInputStream saveFileIn = new FileInputStream(fsinFile)) {
            dstFile.setFilePath(saveFile(saveFileIn, dstFile.getIsImage()));
        }
        dstFile.setUserId(user);
        fileRepository.save(dstFile);
        user.getFileList().add(dstFile);
        userRepository.save(user);
        String returnString = "";
        if (dstFile.getIsImage()) {
            returnString += "/img/";
        } else {
            returnString += "/file/";
        }
        returnString += dstFile.getFileUrl();
        return returnString;
    }

    private String generateFileUrl() {
        boolean done = false;
        float probability = 5.0f;
        while (!done) {
            String tmpUrl = RandomStringUtils.randomAlphanumeric((int) probability);
            boolean collided = fileRepository.findByFileUrl(tmpUrl).isPresent();
            if (!collided) {
                return tmpUrl;
            }
            probability += 0.25f;
            if (probability == 10.0f) {
                done = true;
            }
        }
        return UUID.randomUUID().toString();
    }

    private boolean isImage(final String fileName) {
        String[] strArr = fileName.split("\\.");
        if (strArr.length <= 1) {
            return false;
        }
        String ext = strArr[strArr.length - 1].toLowerCase();
        return ext.equals("png") || ext.equals("jpg") || ext.equals("bmp") || ext.equals("gif");
    }

    private String saveFile(InputStream fsin, boolean isImage) throws IOException {
        java.io.File svFile;
        String fileName = UUID.randomUUID().toString();
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        if (fileRepository.findByFileName(fileName).isPresent()) {
            fileName += UUID.randomUUID().toString();
        }

        if (isImage) {
            svFile = new java.io.File(imageDir.getPath(), fileName);
        } else {
            svFile = new java.io.File(fileDir.getPath(), fileName);
        }
        svFile.createNewFile();
        try (FileOutputStream fout = new FileOutputStream(svFile)) {
            IOUtils.copy(fsin, fout);
            fout.flush();
        } finally {
            fsin.close();
        }

        return svFile.getPath();
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

    public long size() {
        final AtomicLong size = new AtomicLong(0);
        Path path = Paths.get(pushDir.toURI());

        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult
                visitFile(Path file, BasicFileAttributes attrs) {

                    size.addAndGet(attrs.size());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult
                visitFileFailed(Path file, IOException exc) {

                    System.out.println("skipped: " + file + " (" + exc + ")");
                    // Skip folders that can't be traversed
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult
                postVisitDirectory(Path dir, IOException exc) {

                    if (exc != null)
                        System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                    // Ignore errors traversing a folder
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size.get();
    }

}
