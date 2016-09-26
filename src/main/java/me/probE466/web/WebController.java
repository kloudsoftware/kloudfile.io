package me.probE466.web;

import me.probE466.persistence.entities.File;
import me.probE466.persistence.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.FileInputStream;
import java.io.IOException;

@Controller
public class WebController {

    @Autowired
    private FileService fileService;

    @RequestMapping("/")
    public ResponseEntity getRoot() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/test")
    public ModelAndView getTest() {
//        fileService.createFile(null, null);
        return new ModelAndView("basic");
    }

    @RequestMapping(value = "/post", method = RequestMethod.POST)
    public ResponseEntity postFile(@RequestParam MultipartFile file) throws IOException {
        fileService.createFile(file.getInputStream(), file.getName());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
