package com.ifba.Facerecognizer.person.service;

import com.ifba.Facerecognizer.person.model.Person;
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

    public static final String UPLOAD_FOLDER_PATTERN = "./uploadPhotos";
    public static final String LOCAL_FACES_DETECTEDS = "./detectFaces";
    JavaCVService javacv = JavaCVService.getInstance();

    public boolean training(MultipartFile[] images, String id) throws Exception {

        Path dirLocal = null;
        Path faceDir = null;

        try{
            dirLocal =  Files.createDirectories(Paths.get(UPLOAD_FOLDER_PATTERN));
            faceDir = Files.createDirectories(Paths.get(LOCAL_FACES_DETECTEDS));
        } catch (IOException e) {
            e.printStackTrace();
        }

        for( int i = 0;  i < images.length; i++ ) {

            String uploadedFileLocation = faceDir.getFileName() + "/" + images[i].getOriginalFilename();

            List<Mat> faces;
            faces = javacv.detectFaces(ImageIO.read(images[i].getInputStream()));

            if(faces.size() > 1)
                throw new Exception("Só são permitidos uma face por foto");

            imwrite(faceDir.getFileName() + "/"+ "person." + id + "."+ i + ".jpg", faces.get(0));

        }

        try {
            javacv.trainClassifier(this.getFiles(faceDir.getFileName().toString()));
            FileUtils.cleanDirectory(new File(faceDir.getFileName().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    public List<String> recognizePeople(MultipartFile[] images) {
        List<Mat> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
                faces = javacv.detectFaces(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> idList = null;
        List<Person> personList = new ArrayList<>();
        Person person;
        for(Mat face : faces) {
            idList = new ArrayList<>();
            String id = javacv.recognizeFaces(face);
            if(id != null) {
                idList.add(id);
            }
        }

        return idList;
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

}
