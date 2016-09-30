package me.probE466.web;

import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;


@Controller
public class WebController {

    private final SecureRandom secureRandom = new SecureRandom();
    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public ModelAndView getRoot() {
        return new ModelAndView("basic");
    }

//    @RequestMapping(value = "/admin", method = RequestMethod.GET)
//    public ModelAndView getTest() {
//        return new ModelAndView("addapi");
//    }
//
//    @RequestMapping(value = "/admin", method = RequestMethod.POST)
//    public
//    @ResponseBody
//    String addApiKey(@RequestParam("userName") String userName) {
//        User user = new User();
//        String key = generateSecureApiKey(32);
//        user.setUserKey(key);
//        user.setUserName(userName);
//        userRepository.save(user);
//        return key;
//    }


    private String generateSecureApiKey(int length) {
        char[] validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456879".toCharArray();
        Random random = new Random();
        char[] buffer = new char[length];
        for (int i = 0; i < length; ++i) {
            if ((i % 10) == 0) {
                random.setSeed(secureRandom.nextLong());
            }
            buffer[i] = validChars[random.nextInt(validChars.length)];
        }
        return new String(buffer);
    }


    @RequestMapping(value = "/post", method = RequestMethod.POST, produces = "text/plain")
    public
    @ResponseBody
    String postFile(@RequestParam(value = "file") MultipartFile file, @RequestParam String key) throws IOException {
        Optional<User> user = userRepository.findByUserKey(key);
        String url = "";
        if (user.isPresent()) {
            try (final InputStream inputStream = file.getInputStream()) {
                url = fileService.createFile(inputStream, file.getOriginalFilename(), user.get());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
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
        ByteArrayOutputStream bain = null;
        FileInputStream fsin = null;
        try {
            if (oFile.isPresent() && oFile.get().getIsImage()) {
                File file = oFile.get();
                fsin = new FileInputStream(new java.io.File(file.getFilePath()));
                bain = new ByteArrayOutputStream();
                IOUtils.copy(fsin, bain);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.IMAGE_PNG);
                return new ResponseEntity<>(bain.toByteArray(), httpHeaders, HttpStatus.OK);
            } else {
                // TODO: 9/29/2016 Maybe return a 404 response here?
                throw new EntityNotFoundException("IMAGE NOT FOUND");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fsin != null) {
                fsin.close();
            }

            if (bain != null) {
                bain.close();
            }
        }
        return new ResponseEntity<>(new byte[]{0}, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    @RequestMapping(value = "/file/{fileUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    ResponseEntity<byte[]> getFile(@PathVariable("fileUrl") String fileUrl) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(fileUrl);
        FileInputStream fsin = null;
        ByteArrayOutputStream baout = null;
        try {
            if (oFile.isPresent()) {
                File file = oFile.get();

                fsin = new FileInputStream(new java.io.File(file.getFilePath()));
                baout = new ByteArrayOutputStream();
                IOUtils.copy(fsin, baout);
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.set("Content-Disposition", "attachment; filename=" + file.getFileName());
                httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                return new ResponseEntity<>(baout.toByteArray(), httpHeaders, HttpStatus.OK);
            } else {
                throw new EntityNotFoundException("FILE NOT FOUND");
            }
        } catch (IOException | EntityNotFoundException e) {
            // TODO: 9/29/2016 return correct response
            e.printStackTrace();
        } finally {
            if (fsin != null) {
                fsin.close();
            }
            if (baout != null) {
                baout.close();
            }
        }
        return new ResponseEntity<>(new byte[]{0}, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
