package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Person {
    @Id
    @GeneratedValue
    private int id;

    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Column(unique = true)
    private String email;
}
