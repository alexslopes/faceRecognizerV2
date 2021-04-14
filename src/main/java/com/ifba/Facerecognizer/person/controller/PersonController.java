package com.ifba.Facerecognizer.person.controller;

import com.ifba.Facerecognizer.person.facade.PersonFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clientes")
public class PersonController {

    private final PersonFacade personFacade;

    @Autowired
    public  PersonController(PersonFacade personFacade) {
        this.personFacade = personFacade;
    }

    @PostMapping("traine")
    public String traineFace(@RequestParam("images") MultipartFile[] images) {
        return("Recebi as imagens");
    }

    @PostMapping("recognize")
    public String recognizeFace(@RequestParam("images") MultipartFile[] images) {
        personFacade.recognizePeople(images);
        return("Recebi as imagens");
    }
}
