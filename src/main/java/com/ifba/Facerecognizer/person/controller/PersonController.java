package com.ifba.Facerecognizer.person.controller;

import com.ifba.Facerecognizer.person.facade.PersonFacade;
import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class PersonController {

    private final PersonFacade personFacade;
    private final PersonService personService;

    @Autowired
    public  PersonController(PersonFacade personFacade,
                             PersonService personService) {
        this.personFacade = personFacade;
        this.personService = personService;
    }

    @PostMapping("register")
    public void registerUser(@RequestBody Person person) {

        personService.save(person);
    }

    @PostMapping("traine")
    public String traineFace(@RequestParam("images") MultipartFile[] images, @RequestParam("id") String id) throws Exception {

        personFacade.training(images, id);
        return("Recebi as imagens");
    }

    @PostMapping("recognize")
    public List<String> recognizeFace(@RequestParam("images") MultipartFile[] images) {

        return personFacade.recognizePeople(images);
    }
}
