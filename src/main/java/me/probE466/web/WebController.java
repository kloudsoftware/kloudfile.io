package me.probE466.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebController {

    @RequestMapping("/")
    public ResponseEntity getRoot() {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping("/test")
    public ModelAndView getTest() {
        return new ModelAndView("basic");
    }
}
