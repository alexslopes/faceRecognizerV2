package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
public class ResponseTraine {
    Integer totalFace;
    Integer totalFacetraineSucess;
    Integer totalErroNoface;
    Integer totalErroMAnyfaces;
    List<FileDetectFace> fileDetectFaceList;
}
