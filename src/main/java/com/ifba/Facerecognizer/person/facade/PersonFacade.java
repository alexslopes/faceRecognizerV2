package com.ifba.Facerecognizer.person.facade;

import com.ifba.Facerecognizer.person.model.Person;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersonFacade {

    public static final String UPLOAD_FOLDER_PATTERN = Paths.class.getClassLoader().getResource(".").getPath() + "uploadPhotos";
    JavaCv javacv = JavaCv.getInstance();

    public boolean training(MultipartFile[] images) {

    }

    public List<Person> recognizePeople(MultipartFile[] images) {
        List<Mat> faces = null;

        File dirLocal = this.createFolderIfNotExists(UPLOAD_FOLDER_PATTERN);

        String uploadedFileLocation;
        for(MultipartFile img : images) {
            uploadedFileLocation = dirLocal.getAbsolutePath() + "/" + img.getOriginalFilename();
            try {
                this.saveToFile(img.getInputStream(), uploadedFileLocation);
                BufferedImage image = ImageIO.read(new File(uploadedFileLocation));
            } catch (IOException e) {
                e.printStackTrace();
            }
            faces = javacv.detectFaces(image);
        }

        List<String> idList = null;
        List<Person> personList = new ArrayList<>();
        Person person;
        for(Mat face : faces) {
            idList = new ArrayList<>();
            String id = javacv.recognize(face);//Todo: Fazer método javacv
            if(id != null) {
                idList.add(id);
                person = derby.buscar(Integer.parseInt(id));//Todo: fazer busca no repositóriio
                if(person != null)
                    personList.add(person);
            }
        }

        return personList;
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

}
