package me.probE466.web;

import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;


@Controller
public class WebController {

    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public ResponseEntity getRoot() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/admin")
    public ModelAndView getTest() {
//        fileService.createFile(null, null);
        User user = new User();
        user.setUserKey("secret");
        user.setUserName("admin");
        userRepository.save(user);
        return new ModelAndView("basic");
    }

    public String createUser() {
        User user = new User();
        user.setUserKey("secret");
        user.setUserName("admin");
        userRepository.save(user);
        return user.getUserKey();
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST, produces = "text/plain")
    public
    @ResponseBody
    String postFile(@RequestParam(value = "file") MultipartFile file, @RequestParam String key) throws IOException {
        Optional<User> user = userRepository.findByUserKey(key);
        String url;
        if (user.isPresent()) {
            url = fileService.createFile(file.getInputStream(), file.getOriginalFilename(), user.get());
        } else {
            throw new SecurityException("API KEY NOT RECOGNIZED");
        }
        return url;
    }

    @RequestMapping(value = "/img/{imgUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<byte[]> getImage(@PathVariable("imgUrl") String imgUrl) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(imgUrl);
        if (oFile.isPresent() && oFile.get().getIsImage()) {
            File file = oFile.get();
            FileInputStream fsin = new FileInputStream(new java.io.File(file.getFilePath()));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(fsin, byteArrayOutputStream);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), httpHeaders, HttpStatus.OK);
        } else {
            throw new EntityNotFoundException("IMAGE NOT FOUND");
        }
    }

    @RequestMapping(value = "/file/{fileUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<byte[]> getFile(@PathVariable("fileUrl") String fileUrl) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(fileUrl);
        if (oFile.isPresent()) {
            File file = oFile.get();
            FileInputStream fsin = new FileInputStream(new java.io.File(file.getFilePath()));
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(fsin, byteArrayOutputStream);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("Content-Disposition", "attachment; filename=" + file.getFileName());
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), httpHeaders, HttpStatus.OK);
        } else {
            throw new EntityNotFoundException("FILE NOT FOUND");
        }
    }
}
