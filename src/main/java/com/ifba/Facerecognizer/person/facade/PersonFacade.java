package com.ifba.Facerecognizer.person.facade;

import com.ifba.Facerecognizer.person.model.FileDetectFace;
import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.model.PersonFileRecognize;
import com.ifba.Facerecognizer.person.model.ResponseTraine;
import com.ifba.Facerecognizer.person.service.JavaCVService;
import com.ifba.Facerecognizer.person.service.PersonService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.bytedeco.opencv.opencv_core.Mat;
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

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;

@Service
public class PersonFacade {

    private final PersonService personService;

    public static final String UPLOAD_FOLDER_PATTERN = "./uploadPhotos";
    public static final String LOCAL_FACES_DETECTEDS = "./detectFaces";

    JavaCVService javacv = JavaCVService.getInstance();

    public PersonFacade(PersonService personService) {
        this.personService = personService;
    }


    public ResponseTraine training(MultipartFile[] images, Person person) {

        Path faceDir = null;

        try{
            faceDir = Files.createDirectories(Paths.get(LOCAL_FACES_DETECTEDS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<FileDetectFace> fileDetectFaces = new ArrayList<>();
        int totalFacetraineSucess = 0;
        int totalErroNoface = 0;
        int totalErroMAnyfaces = 0;
        for( int i = 0;  i < images.length; i++ ) {

            List<Mat> faces = null;
            String message = null;
            String status = null;
            try {
                faces = javacv.detectFaces(ImageIO.read(images[i].getInputStream()));
            } catch (IOException e) {
                message = "Erro ao ler imagem";
                status = "Error";
            }

            if(faces.size() == 1) {
                message = "Face detectada com sucesso";
                status = "Ok";
                totalFacetraineSucess++;
                imwrite(faceDir.getFileName() + "/" + "person." + person.getId() + "." + i + ".jpg", faces.get(0));

            } else if ( faces.size() > 1 ){
                message = "Mais de uma face encontrada";
                totalErroMAnyfaces++;
                status = "Error";
            } else if ( faces.size() == 0 ){
                message = "Não foim encontrada face";
                totalErroNoface++;
                status = "Error";
            }

            FileDetectFace fileDetectFace = FileDetectFace.builder().
                    filename(images[i].getOriginalFilename()).
                    message(message).
                    status(status).build();

            fileDetectFaces.add(fileDetectFace);

        }

        try {
            javacv.trainClassifier(this.getFiles(faceDir.getFileName().toString()));
            //FileUtils.cleanDirectory(new File(faceDir.getFileName().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseTraine.builder().
                totalFace(images.length)
                .totalErroMAnyfaces(totalErroMAnyfaces)
                .totalFacetraineSucess(totalFacetraineSucess)
                .totalErroNoface(totalErroNoface)
                .fileDetectFaceList(fileDetectFaces).build();
    }

    public List<PersonFileRecognize> recognizePeople(MultipartFile[] images) {
        List<Mat> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        List<PersonFileRecognize> personFileRecognizeList = new ArrayList<>();
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
                faces = javacv.detectFaces(image);

                List<Person> personList = null;
                for(Mat face : faces) {
                    Integer id = javacv.recognizeFaces(face);
                    if(id != null) {
                        if(personList == null)
                            personList = new ArrayList<>();

                        personList.add(personService.findById(id).get());
                    }
                }

                if(personList != null)
                    personFileRecognizeList.add(PersonFileRecognize.builder().filename(img.getOriginalFilename())
                                    .personList(personList)
                                    .message(personList.size() > 0 ? "Foi/Foram econtrado(s) " + personList.size() + "face(s) conhecida(s)" :
                                            "Não foi encontrado faces conhecidas").build());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return personFileRecognizeList;
    }

    public static File createFolderIfNotExists(String dirName) throws SecurityException {
        File theDir = new File(dirName);
        if (!theDir.exists()) {
            theDir.mkdir();
        }

        return theDir;
    }

    public static void saveToFile(InputStream inStream, String target) throws IOException {
        OutputStream out = null;
        int read = 0;
        byte[] bytes = new byte[1024];

        out = new FileOutputStream(new File(target));
        while ((read = inStream.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
    }

    public static File[] getFiles(String local) {
        File photosFolder = new File(local);
        if (!photosFolder.exists()) return null;

        FilenameFilter imageFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg") || name.endsWith(".gif") || name.endsWith(".png");
            }
        };

        return photosFolder.listFiles(imageFilter);
    }

    public ResponseTraine traineFace(MultipartFile[] images, String email) throws Exception {
        Person person = personService.findByEmail(email);

        if(person == null)
            throw new Exception("Usuário não encontrado");

        return this.training(images, person);
    }

}
