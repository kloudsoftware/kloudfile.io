package me.probE466.web;

import com.google.gson.Gson;
import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Controller
public class ApiController {

    private final Gson GSON = new Gson();
    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping(value = "/api/list", method = RequestMethod.GET, produces = "application/json")
    public
    @ResponseBody
    String getList(@RequestHeader("Authorization") String apiKey, @RequestHeader(required = false, value = "start") Integer start,
                   @RequestHeader(required = false, value = "limit") Integer limit) {
        User user = userRepository.findByUserKey(apiKey).orElseThrow(EntityNotFoundException::new);
        List<FileDTO> returnList = new ArrayList<>();
        List<File> fileList = user.getFileList();
        if (start == null && limit == null) {
            for (File file : fileList) {
                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));
            }
        } else if (start == null && limit != 0 && limit > 0) {
            int currentIndex = 0;

            for (File file : fileList) {
                if (currentIndex == limit) {
                    break;
                }
                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));
                currentIndex++;
            }
        } else if (start != null && start > 0 && limit != 0) {
            if (start > fileList.size()) {
                return "[]";
            }

            for (int i = start; i < start + limit; i++) {
                if (i > fileList.size() - 1) {
                    break;
                }

                File file = fileList.get(i);

                String ext;

                try {
                    ext = fileService.getExt(file.getFileName());
                } catch (InputMismatchException e) {
                    ext = "";
                }

                returnList.add(new FileDTO(file.getId(), file.getFileUrl(), file.getFileDeleteUrl(), file.getIsImage(),
                        ext, file.getFileName(), file.getFileDateCreated(), file.getFileDateUpdated(), file.getFileViewed()));

            }

        }
        return GSON.toJson(returnList);
    }

    @RequestMapping(value = "/api/post", method = RequestMethod.POST, produces = "application/json", headers = "Accept=*/*", consumes = "multipart/*")
    public
    @ResponseBody
    String postFile(HttpServletRequest request) throws IOException, FileUploadException {
        ServletFileUpload servletFileUpload = new ServletFileUpload();
        FileItemIterator iterator = servletFileUpload.getItemIterator(request);
        String key;
        User user = null;
        InputStream filein = null;
        String fileName = "tempfile" + UUID.randomUUID().toString() + System.currentTimeMillis();
        String originalFileName = "";
        java.io.File file = null;
        File fileServiceFile = null;
        boolean authorizedRequest = false;
        boolean badFile = false;
        String jsonResponse = "";

        while (iterator.hasNext()) {
            FileItemStream fileItem = iterator.next();
            if (fileItem.isFormField()) {
                InputStream is = fileItem.openStream();
                key = Streams.asString(is);
                if (userRepository.findByUserKey(key).isPresent()) {
                    user = userRepository.findByUserKey(key).get();
                    authorizedRequest = true;
                }
            } else {
                filein = fileItem.openStream();
                file = new java.io.File(fileName);
                badFile = !file.createNewFile();
                originalFileName = fileItem.getName();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                IOUtils.copy(filein, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }

        if (file == null) {
            badFile = true;
        }

        try {
            if (authorizedRequest && !badFile) {
                fileServiceFile = fileService.createFile(file, originalFileName, user);
                file.delete();
            } else if (badFile) {
                throw new FileUploadException("bad file");
            } else {
                file.delete();
                throw new SecurityException("API KEY NOT RECOGNIZED");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } finally {
            if (filein != null) {
                filein.close();
            }
            file.delete();
        }


        String contextString = "";
        if (fileServiceFile != null) {
            if (fileServiceFile.getIsImage()) {
                contextString += "/img/";
            } else {
                contextString += "/file/";
            }
        }
        contextString += fileServiceFile != null ? fileServiceFile.getFileUrl() : null;

        if (fileServiceFile != null) {
            jsonResponse = GSON.toJson(new UrlDTO(contextString, "/api/delete/" + fileServiceFile.getFileDeleteUrl()));
        }

        return jsonResponse;
    }

    @RequestMapping(value = "/api/delete/{fileDeleteUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity deleteFile(@PathVariable("fileDeleteUrl") String fileUrl) throws IOException {
        final Optional<File> fileOptional = fileService.getFileRepository().findByFileDeleteUrl(fileUrl);

        if (fileOptional.isPresent()) {
            final File file = fileOptional.get();
            final java.io.File javaFile = new java.io.File(file.getFilePath());
            if (javaFile.delete()) {
                fileService.getFileRepository().delete(file);
                return new ResponseEntity(HttpStatus.OK);
            }
        }

        return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
    }
}
