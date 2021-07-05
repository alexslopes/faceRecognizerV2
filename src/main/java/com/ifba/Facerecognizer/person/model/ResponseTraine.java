package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@AllArgsConstructor
@Builder
public class ResponseTraine {
    Integer totalFace;
    Integer totalFacetraineSucess;
    Integer totalErroNoface;
    Integer totalErroMAnyfaces;
    List<FileDetectFace> fileDetectFaceList;
}
