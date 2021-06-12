package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@Builder
public class Person {
    @Id
    @GeneratedValue
    private int id;

    @NonNull
    private String name;

    @NonNull
    private String email;
}
