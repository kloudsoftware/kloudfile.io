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
import org.springframework.http.MediaType;
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
import java.nio.file.Files;
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


    @RequestMapping(value = "/post", method = RequestMethod.POST, produces = "text/plain", headers = "Accept=*/*", consumes = "multipart/*")
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
            if (authorizedRequest) {
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
            jsonResponse = GSON.toJson(new UrlDTO(contextString, fileServiceFile.getFileDeleteUrl()));
        }

        return jsonResponse;
    }

    @RequestMapping(value = "/delete/{fileUrl}", method = RequestMethod.GET)
    public @ResponseBody String deleteFile(@PathVariable("fileUrl") String fileUrl) throws IOException {
        final Optional<File> fileOptional = fileService.getFileRepository().findByFileDeleteUrl(fileUrl);

        if (fileOptional.isPresent()) {
            final File file = fileOptional.get();
            final java.io.File javaFile = new java.io.File(file.getFilePath());
            if (javaFile.delete()) {
                fileService.getFileRepository().delete(file);
                return "File deleted!";
            }
        }

        return "Error, please try again";
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
                file.setFileViewed(file.getFileViewed() + 1);
                fileService.getFileRepository().save(file);
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
                file.setFileViewed(file.getFileViewed() + 1);
                fileService.getFileRepository().save(file);
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
