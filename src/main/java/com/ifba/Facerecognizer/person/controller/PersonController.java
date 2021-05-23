package com.ifba.Facerecognizer.person.controller;

import com.ifba.Facerecognizer.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clientes")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public  PersonController(PersonService personService) {
        this.personService = personService;
    }

    @PostMapping("traine")
    public String traineFace(@RequestParam("images") MultipartFile[] images, @RequestParam("id") String id) throws Exception {
        personService.training(images, id);
        return("Recebi as imagens");
    }

    @PostMapping("recognize")
    public String recognizeFace(@RequestParam("images") MultipartFile[] images) {
        personService.recognizePeople(images);
        return("Recebi as imagens");
    }
}
