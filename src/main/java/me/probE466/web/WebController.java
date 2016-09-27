package me.probE466.web;

import me.probE466.persistence.entities.User;
import me.probE466.persistence.repos.UserRepository;
import me.probE466.persistence.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
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

    @RequestMapping("/test")
    public ModelAndView getTest() {
//        fileService.createFile(null, null);
        User user = new User();
        user.setUserKey("secret");
        user.setUserName("admin");
        userRepository.save(user);
        return new ModelAndView("basic");
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public ResponseEntity postFile(@RequestParam(value = "file") MultipartFile file, @RequestParam String key) throws IOException {
        Optional<User> user = userRepository.findByUserKey(key);
        if (user.isPresent()) {
            fileService.createFile(file.getInputStream(), file.getOriginalFilename(), user.get());
        } else {
            throw new SecurityException("API KEY NOT RECOGNIZED");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
