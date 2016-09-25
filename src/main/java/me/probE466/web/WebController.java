package me.probE466.web;

import me.probE466.persistence.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebController {

    @Autowired
    FileService fileService;

    @RequestMapping("/")
    public ResponseEntity getRoot() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/test")
    public ModelAndView getTest() {
        fileService.createFile(null);
        return new ModelAndView("basic");
    }
}
