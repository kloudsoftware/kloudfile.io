package me.probE466.web;

import me.probE466.persistence.entities.File;
import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
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
