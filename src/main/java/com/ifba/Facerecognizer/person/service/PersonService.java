package com.ifba.Facerecognizer.person.service;

import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.repository.PersonRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Service
public class PersonService {

    private PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public void save(Person person) {
        this.personRepository.save(person);
    }

    public Optional<Person> findById(int id) {
        return this.personRepository.findById(id);
    }

    public Person findByEmail(String email) {
        return this.personRepository.findByEmail(email);
    }

}
