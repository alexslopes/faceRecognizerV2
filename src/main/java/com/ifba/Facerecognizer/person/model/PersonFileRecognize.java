package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
public class PersonFileRecognize {
    private String filename;
    private String message;
    private List<Person> personList;
}
