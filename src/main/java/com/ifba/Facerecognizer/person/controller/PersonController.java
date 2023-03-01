package com.ifba.Facerecognizer.person.controller;

import com.ifba.Facerecognizer.person.facade.PersonFacade;
import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.model.PersonFileRecognize;
import com.ifba.Facerecognizer.person.model.ResponseTraine;
import com.ifba.Facerecognizer.person.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
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
    public ResponseTraine traineFace(@RequestParam("images") MultipartFile[] images, @RequestParam("email") String email) throws Exception {

        return personFacade.traineFace(images, email);
    }

    @PostMapping("recognize")
    public Map<String, List<PersonFileRecognize>> recognizeFace(@RequestParam("images") MultipartFile[] images) {
        Map<String, List<PersonFileRecognize>> map  = new HashMap<>();
        map.put("eigenFace", personFacade.eigenFaceRecognizePeople(images));
        map.put("fisherFace",personFacade.fisherFaceRecognizePeople(images));
        map.put("lbph",personFacade.lbphFaceRecognizePeople(images));

        return map;
    }

    @PostMapping("traine-register")
    public ResponseTraine traineFace(@RequestParam("images") MultipartFile[] images, @RequestParam("email") String email,  @RequestParam("name") String name) throws Exception {

        personService.save(Person.builder().email(email).name(name).build());
        return personFacade.traineFace2(images, email);
    }

}
