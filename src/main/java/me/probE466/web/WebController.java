package me.probE466.web;

import javassist.tools.web.BadHttpRequest;
import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.apache.tomcat.util.http.fileupload.*;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

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

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    public ModelAndView getAddApi() {
        return new ModelAndView("addapi");
    }

    @RequestMapping(value = "/admin/list", method = RequestMethod.GET)
    public ModelAndView listApiKeys() {
        ModelAndView modelAndView = new ModelAndView("listapi");
        Map<String, String> userKeyMap = new HashMap<>();
        List<User> users = userRepository.findAll();

        for (User user : users) {
            userKeyMap.put(user.getUserName(), user.getUserKey());
        }

        modelAndView.addObject("map", userKeyMap);
        return modelAndView;
    }

    @RequestMapping(value = "/admin/revoke/{key}")
    public String revokeUserKey(@PathVariable("key") String key) {
        Optional<User> userOptional = userRepository.findByUserKey(key);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            userRepository.delete(user);
        } else {
            throw new EntityNotFoundException("User not found");
        }
        return "redirect:/admin/list";
    }

    @RequestMapping(value = "/admin", method = RequestMethod.POST)
    public
    @ResponseBody
    String addApiKey(@RequestParam("userName") String userName) {
        User user = new User();
        String key = generateSecureApiKey(32);
        user.setUserKey(key);
        user.setUserName(userName);
        userRepository.save(user);
        return key;
    }


    private String
    generateSecureApiKey(int length) {
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


    @RequestMapping(value = "/post", method = RequestMethod.POST, produces = "text/plain", headers = "Accept=*/*", consumes = "multipart/*")
    public
    @ResponseBody
    String postFile(HttpServletRequest request) throws IOException, FileUploadException {
//        Optional<User> user = userRepository.findByUserKey(key);
        String url = "";
        ServletFileUpload servletFileUpload = new ServletFileUpload();
        FileItemIterator iterator = servletFileUpload.getItemIterator(request);
        String key;
        User user = null;
        InputStream filein = null;
        String fileName = "tempfile" + UUID.randomUUID().toString() + System.currentTimeMillis();
        String originalFileName = "";
        java.io.File file = null;
        boolean authorizedRequest = false;
        boolean badFile = false;

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
                file.createNewFile();
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
            if (authorizedRequest) {
                url = fileService.createFile(file, originalFileName, user);
                file.delete();
            } else if (badFile) {
                throw new FileUploadException("bad file");
            } else {
                if (file != null) {
                    file.delete();
                }
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

        return url;
    }

    @RequestMapping(value = "/img/{imgUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    void getImage(@PathVariable("imgUrl") String imgUrl, HttpServletResponse response) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(imgUrl);
        FileInputStream fsin = null;
        try {
            if (oFile.isPresent() && oFile.get().getIsImage()) {
                File file = oFile.get();
                fsin = new FileInputStream(new java.io.File(file.getFilePath()));
                response.setContentType("image/png");
                response.setContentLengthLong(fsin.available());
                IOUtils.copy(fsin, response.getOutputStream());
                response.flushBuffer();
            } else {
                response.sendError(404);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fsin != null) {
                fsin.close();
            }
            response.getOutputStream().close();
        }
    }

    @RequestMapping(value = "/file/{fileUrl}", method = RequestMethod.GET)
    public
    @ResponseBody
    void getFile(@PathVariable("fileUrl") String fileUrl, HttpServletResponse response) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(fileUrl);
        FileInputStream fsin = null;
        try {
            if (oFile.isPresent()) {
                File file = oFile.get();

                fsin = new FileInputStream(new java.io.File(file.getFilePath()));

                response.setContentType(MediaType.APPLICATION_OCTET_STREAM.toString());
                response.addHeader("Content-Disposition", "attachment; filename=" + file.getFileName());
                IOUtils.copy(fsin, response.getOutputStream());
                fsin.close();
                response.flushBuffer();
            } else {
                throw new EntityNotFoundException("FILE NOT FOUND");
            }
        } catch (EntityNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fsin != null) {
                fsin.close();
            }
            response.getOutputStream().close();
        }
    }
}
