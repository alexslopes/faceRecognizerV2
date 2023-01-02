package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.*;

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

    @Lob
    private byte[] foto;

    private String confiability;
}
