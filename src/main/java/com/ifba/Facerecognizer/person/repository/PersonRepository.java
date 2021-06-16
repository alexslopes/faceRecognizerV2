package com.ifba.Facerecognizer.person.repository;

import com.ifba.Facerecognizer.person.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Integer> {

    Person findByEmail(String email);
}
