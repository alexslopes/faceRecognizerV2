package com.ifba.Facerecognizer.person.facade;

import com.ifba.Facerecognizer.person.model.FileDetectFace;
import com.ifba.Facerecognizer.person.model.Person;
import com.ifba.Facerecognizer.person.model.PersonFileRecognize;
import com.ifba.Facerecognizer.person.model.ResponseTraine;
import com.ifba.Facerecognizer.person.service.JavaCVService;
import com.ifba.Facerecognizer.person.service.PersonService;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.opencv.opencv_core.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_PLAIN;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;

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
                faces = javacv.detectFacesWithGray(ImageIO.read(images[i].getInputStream()));
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

    public List<PersonFileRecognize> eigenFaceRecognizePeople(MultipartFile[] images) {
        Map<Mat, Rect> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        List<PersonFileRecognize> personFileRecognizeList = new ArrayList<>();
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
                Mat rgbaMat = JavaCVService.BufferedImage2Mat(image);
                faces = javacv.detectFaces(rgbaMat);

                javacv.setEiginFaceRecognizer();

                List<Person> personList = null;
                double confiabilityTotal = 0;
                for(Map.Entry<Mat, Rect> face : faces.entrySet()) {
                    DoublePointer confiability = new DoublePointer(1);
                    Integer id = javacv.recognizeEigenFaces(face.getKey(), confiability);
                    if(id != null) {
                        if(personList == null) {
                            personList = new ArrayList<>();
                        }

                        int x = Math.max(face.getValue().tl().x() - 10, 0);
                        int y = Math.max(face.getValue().tl().y() - 10, 0);

                        putText(rgbaMat, personService.findById(id).get().getName(), new Point(x, y), FONT_HERSHEY_PLAIN, 1.4, new Scalar(0,255,0,0));
                        Person person = personService.findById(id).get();
                        person.setConfiability(Double.toString(confiability.get(0)));
                        person.setFoto(JavaCVService.toByteArray(JavaCVService.toBufferedImage(face.getKey())));
                        personList.add(person);

                        confiabilityTotal = confiabilityTotal + confiability.get(0);
                    }
                }

                if(personList != null) {
                    personFileRecognizeList.add(PersonFileRecognize.builder().filename(img.getOriginalFilename())
                            .personList(personList)
                            .message(personList.size() > 0 ? "Foi/Foram econtrado(s) " + personList.size() + "face(s) conhecida(s)" +
                                    "\n" + "com confiabilidade de:" + confiabilityTotal:
                                    "Não foi encontrado faces conhecidas").build());
                }

                imwrite("ResultadoEigenface(10, 0).jpg", javacv.getRgbaMat());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return personFileRecognizeList;
    }

    public List<PersonFileRecognize> fisherFaceRecognizePeople(MultipartFile[] images) {
        Map<Mat, Rect> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        List<PersonFileRecognize> personFileRecognizeList = new ArrayList<>();
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
                Mat rgbaMat = JavaCVService.BufferedImage2Mat(image);
                faces = javacv.detectFaces(rgbaMat);

                javacv.setFisherFaceRecognizer();

                List<Person> personList = null;
                double confiabilityTotal = 0;
                for(Map.Entry<Mat, Rect> face : faces.entrySet()) {
                    DoublePointer confiability = new DoublePointer(1);
                    Integer id = javacv.recognizeFisherFaces(face.getKey(),confiability);
                    if(id != null) {
                        if(personList == null) {
                            personList = new ArrayList<>();
                        }

                        int x = Math.max(face.getValue().tl().x() - 10, 0);
                        int y = Math.max(face.getValue().tl().y() - 10, 0);

                        Person person = personService.findById(id).get();
                        person.setConfiability(Double.toString(confiability.get(0)));
                        person.setFoto(JavaCVService.toByteArray(JavaCVService.toBufferedImage(face.getKey())));
                        personList.add(person);
                        confiabilityTotal = confiabilityTotal + confiability.get(0);

                        putText(rgbaMat, personService.findById(id).get().getName(), new Point(x, y), FONT_HERSHEY_PLAIN, 1.4, new Scalar(0,255,0,0));
                    }
                }

                if(personList != null) {
                    personFileRecognizeList.add(PersonFileRecognize.builder().filename(img.getOriginalFilename())
                            .personList(personList)
                            .message(personList.size() > 0 ? "Foi/Foram econtrado(s) " + personList.size() + "face(s) conhecida(s)" +
                                    "\n" + "com confiabilidade de:" + confiabilityTotal:
                                    "Não foi encontrado faces conhecidas" + "\n" + "com confiabilidade de:" + confiabilityTotal).build());
                }

                imwrite("ResultadoFisherface(10, 0).jpg", javacv.getRgbaMat());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return personFileRecognizeList;
    }

    public List<PersonFileRecognize> lbphFaceRecognizePeople(MultipartFile[] images) {
        Map<Mat, Rect> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        List<PersonFileRecognize> personFileRecognizeList = new ArrayList<>();
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
                Mat rgbaMat = JavaCVService.BufferedImage2Mat(image);
                faces = javacv.detectFaces(rgbaMat);

                javacv.setLBPHFaceRecognizer();

                List<Person> personList = null;
                double confiabilityTotal = 0;
                for(Map.Entry<Mat, Rect> face : faces.entrySet()) {
                    DoublePointer confiability = new DoublePointer(1);
                    Integer id = javacv.recognizeLPBHFaces(face.getKey(),confiability);
                    if(id != null) {
                        if(personList == null) {
                            personList = new ArrayList<>();
                        }

                        int x = Math.max(face.getValue().tl().x() - 10, 0);
                        int y = Math.max(face.getValue().tl().y() - 10, 0);

                        Person person = personService.findById(id).get();
                        person.setConfiability(Double.toString(confiability.get(0)));
                        person.setFoto(JavaCVService.toByteArray(JavaCVService.toBufferedImage(face.getKey())));
                        personList.add(person);

                        confiabilityTotal = confiabilityTotal + confiability.get(0);

                        putText(rgbaMat, personService.findById(id).get().getName(), new Point(x, y), FONT_HERSHEY_PLAIN, 1.4, new Scalar(0,255,0,0));
                    }
                }

                if(personList != null) {
                    personFileRecognizeList.add(PersonFileRecognize.builder().filename(img.getOriginalFilename())
                            .personList(personList)
                            .message(personList.size() > 0 ? "Foi/Foram econtrado(s) " + personList.size() + "face(s) conhecida(s)" +
                                    "\n" + "com confiabilidade de:" + confiabilityTotal:
                                    "Não foi encontrado faces conhecidas" + "\n" + "com confiabilidade de:" + confiabilityTotal).build());
                }

                imwrite("ResultadoLBPH(12, 10, 15, 15, 0).jpg", javacv.getRgbaMat());
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

    public ResponseTraine traineFace2(MultipartFile[] images, String email) throws Exception {
        Person person = personService.findByEmail(email);

        if(person == null)
            throw new Exception("Usuário não encontrado");

        return this.training2(images, person);
    }

    public ResponseTraine training2(MultipartFile[] images, Person person) {

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
                faces = Arrays.asList(JavaCVService.BufferedImage2Mat(ImageIO.read(images[i].getInputStream())));
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
            } else if ( faces.size() == 0 ){;
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

}
