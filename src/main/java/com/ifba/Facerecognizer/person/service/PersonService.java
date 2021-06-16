package com.ifba.Facerecognizer.person.service;

import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.repository.PersonRepository;
import com.ifba.Facerecognizer.person.service.JavaCVService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public Person findByEmail(String email) {
        return this.personRepository.findByEmail(email);
    }

}
