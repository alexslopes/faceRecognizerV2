package com.ifba.Facerecognizer.person.service;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;
import org.bytedeco.opencv.opencv_face.FaceRecognizer;
import org.bytedeco.opencv.opencv_face.FisherFaceRecognizer;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bytedeco.opencv.global.opencv_imgproc.CV_BGR2GRAY;

public class JavaCVService {

    private static CascadeClassifier faceDetector;
    private static FaceRecognizer eigenFaceRecognizer;
    private static FaceRecognizer lbphRecognizer;
    private static FaceRecognizer fisherFaceRecognizer;
    private static final int IMG_SIZE = 160;
    public static JavaCVService javaCVService;

    public static final String EIGEN_FACES_CLASSIFIER = "resources/eigenFacesClassifier.yml";
    public static final String FISHER_FACES_CLASSIFIER = "resources/fisherFacesClassifier.yml";
    public static final String LBPH_FACES_CLASSIFIER = "resources/lbphFacesClassifier.yml";
    public static final String FRONTAL_FACE_CLASSIFIER = "resources/frontalface.xml";
    public static final String UPLOAD_FOLDER_PATTERN = "data/uploadPhotos";
    public static final String LOCAL_FACES_DETECTEDS = "data/detectFaces";


    public Mat getRgbaMat() {
        return rgbaMat;
    }

    private Mat rgbaMat;

    private JavaCVService() {
        setFaceDetector(FRONTAL_FACE_CLASSIFIER);

        //setRecognizer(FISHER_FACES_CLASSIFIER);

        //setRecognizer(LBPH_FACES_CLASSIFIER);

    }

    public static JavaCVService getInstance() {
        if(javaCVService == null) {
            javaCVService = new JavaCVService();
        }
        return javaCVService;
    }

    public CascadeClassifier getFaceDetector() {
        return faceDetector;
    }

    public void setFaceDetector(String path) {
        faceDetector = new CascadeClassifier(path);
    }

    public FaceRecognizer getRecognizer() {
        return eigenFaceRecognizer;
    }

    public void setEiginFaceRecognizer() {
        eigenFaceRecognizer =  EigenFaceRecognizer.create();
        //recognizer =  FisherFaceRecognizer.create();
        //recognizer =  LBPHFaceRecognizer.create(2,9,9,9,1);
        eigenFaceRecognizer.read(EIGEN_FACES_CLASSIFIER);
        //recognizer.setThreshold(2000);
    }

    public void setEiginFaceTrainer(MatVector photos, Mat labels) {
        eigenFaceRecognizer =  EigenFaceRecognizer.create();

        eigenFaceRecognizer.train(photos, labels);
        eigenFaceRecognizer.save(EIGEN_FACES_CLASSIFIER);
    }

    public void setLBPHFaceTrainer(MatVector photos, Mat labels) {
        lbphRecognizer =  LBPHFaceRecognizer.create(2,9,9,9,1);

        lbphRecognizer.train(photos, labels);
        lbphRecognizer.save(LBPH_FACES_CLASSIFIER);
    }

    public void setFisherFaceTrainer(MatVector photos, Mat labels) {
        fisherFaceRecognizer =  FisherFaceRecognizer.create();

        fisherFaceRecognizer.train(photos, labels);
        fisherFaceRecognizer.save(FISHER_FACES_CLASSIFIER);
    }

    public Map<Mat, Rect> detectFaces(BufferedImage image) throws IOException {

        Map<Mat, Rect> detectFaces = new HashMap<>();

        Mat rgbaMat = this.BufferedImage2Mat(image);
        Mat greyMat = new Mat();
        cvtColor(rgbaMat, greyMat, CV_BGR2GRAY);
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(greyMat, faces);
        System.out.println("Faces detectadas: " + faces.size());

        for(int i = 0 ;i < faces.size(); i++) {
            Rect mainFace;
            mainFace = faces.get(i);
            Mat detectFace = new Mat(greyMat, mainFace);
            resize(detectFace, detectFace, new Size(160, 160));
            detectFaces.put(detectFace, mainFace);
            rectangle(rgbaMat, mainFace, new Scalar(0,255,0,0));
        }

        this.rgbaMat = rgbaMat;

        return detectFaces;
    }

    public List<Mat> detectFacesList(BufferedImage image) throws IOException {

        List<Mat> detectFaces = new ArrayList<>();

        Mat rgbaMat = this.BufferedImage2Mat(image);
        Mat greyMat = new Mat();
        cvtColor(rgbaMat, greyMat, CV_BGR2GRAY);
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(greyMat, faces);
        System.out.println("Faces detectadas: " + faces.size());

        for(int i = 0 ;i < faces.size(); i++) {
            Rect mainFace;
            mainFace = faces.get(i);
            Mat detectFace = new Mat(greyMat, mainFace);
            resize(detectFace, detectFace, new Size(160, 160));
            detectFaces.add(detectFace);
            rectangle(rgbaMat, mainFace, new Scalar(0,255,0,0));
        }

        this.rgbaMat = rgbaMat;

        return detectFaces;
    }
    public List<Mat> detectFacesWithGray(BufferedImage image) throws IOException {

        List<Mat> detectFaces = new ArrayList<>();

        Mat greyMat = this.BufferedImage2Mat(image);
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(greyMat, faces);
        System.out.println("Faces detectadas: " + faces.size());

        for(int i = 0 ;i < faces.size(); i++) {
            Rect mainFace;
            mainFace = faces.get(i);
            Mat detectFace = new Mat(greyMat, mainFace);
            resize(detectFace, detectFace, new Size(160, 160));
            detectFaces.add(detectFace);
        }

        return detectFaces;
    }


    public boolean trainClassifier(File[] files) throws Exception{

        MatVector photos = new MatVector(files.length);
        Mat labels = new Mat(files.length, 1, CV_32SC1);
        IntBuffer labelBuffer = labels.createBuffer();
        int counter = 0;
        for (File image : files) {
            Mat photo = imread(image.getAbsolutePath(), IMREAD_GRAYSCALE);
            int classe = Integer.parseInt(image.getName().split("\\.")[1]);
            resize(photo, photo, new Size(IMG_SIZE, IMG_SIZE));
            photos.put(counter, photo);
            labelBuffer.put(counter, classe);
            counter++;
        }

        //TODO: Separar classificadores
        this.setEiginFaceTrainer(photos, labels);
        this.setLBPHFaceTrainer(photos, labels);
        //this.setFisherFaceTrainer(photos, labels);
        return true;

    }

    public Integer recognizeFaces(Mat face) {

        IntPointer label = new IntPointer(1);
        DoublePointer confiability = new DoublePointer(1);
        eigenFaceRecognizer.predict(face, label, confiability);
        int predict = label.get(0);

        if(predict == -1)
            return null;

        return predict;
    }

    public static Mat BufferedImage2Mat(BufferedImage image) {
        OpenCVFrameConverter.ToMat cv = new OpenCVFrameConverter.ToMat();
        return cv.convertToMat(new Java2DFrameConverter().convert(image));
    }
}
