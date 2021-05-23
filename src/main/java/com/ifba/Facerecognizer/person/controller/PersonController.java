package com.ifba.Facerecognizer.person.controller;

import com.ifba.Facerecognizer.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping("recognize")
    public List<String> recognizeFace(@RequestParam("images") MultipartFile[] images) {
        return personService.recognizePeople(images);
    }
}
