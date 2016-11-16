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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.*;

@Controller
public class WebController {

    private final SecureRandom secureRandom = new SecureRandom();
    private final Gson GSON = new Gson();
    @Autowired
    private FileService fileService;
    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/")
    public ModelAndView getRoot() {
        return new ModelAndView("basic");
    }

    @RequestMapping("/stats")
    public ModelAndView getStats() {
        ModelAndView mav = new ModelAndView("stats");
        List<User> userList = userRepository.findAll();
        Map<String, String> userStatListMap = new HashMap<>();

        for (User user : userList) {
            userStatListMap.put(user.getUserName(), String.valueOf(user.getFileList().size()));
        }

        long freeSpace = new java.io.File("/").getFreeSpace();
        long usedSpace = fileService.size();


        mav.addObject("systemload", "Systemload: " + ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
        mav.addObject("ramload", "RAM Usage: " + getReadableSize(ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()));
        mav.addObject("freespace", "Free Space: " + getReadableSize(freeSpace));
        mav.addObject("usedspace", "Used Space: " + getReadableSize(usedSpace));
        mav.addObject("map", userStatListMap);
        return mav;
    }

    private String getReadableSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
                + " " + units[digitGroups];
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

    @RequestMapping(value = "/v/{res}")
    public ModelAndView displayInBrowser(@PathVariable("res") String resource) {
        return new ModelAndView("res");
    }


    @RequestMapping(value = "/res/{res}", method = RequestMethod.GET)
    public
    @ResponseBody
    void getViewable(@PathVariable("res") String resource, HttpServletResponse response) throws IOException {
        Optional<File> oFile = fileService.getFileRepository().findByFileUrl(resource);
        FileInputStream fsin = null;
        try {
            if (oFile.isPresent() && oFile.get().getIsImage()) {
                File file = oFile.get();
                fsin = new FileInputStream(new java.io.File(file.getFilePath()));

                if (fileService.isVideo(file)) {
                    response.setContentType("video/mp4");
                    response.addHeader("Connection", "keep-alive");
                    response.addHeader("Accept-Ranges", "bytes");
                } else if (fileService.isAudio(file)) {
                    if(fileService.isMp3(file)) {
                        response.addHeader("audio","mpeg");
                        response.addHeader("Accept-Ranges", "bytes");
                        response.setContentType("audio/mpeg");
                    } else {
                        response.addHeader("audio", "wav");
                        response.addHeader("Accept-Ranges", "bytes");
                        response.setContentType("audio/wav");
                    }
                } else {
                    response.setContentType("image/png");
                }

                response.setContentLengthLong(fsin.available());
                IOUtils.copy(fsin, response.getOutputStream());
                response.flushBuffer();
                file.setFileViewed(file.getFileViewed() + 1);
                fileService.getFileRepository().save(file);
            } else {
                response.sendError(404);
                throw new EntityNotFoundException();
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
                file.setFileViewed(file.getFileViewed() + 1);
                fileService.getFileRepository().save(file);
            } else {
                response.sendError(404);
                throw new EntityNotFoundException();
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
