package com.ifba.Facerecognizer.person.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@AllArgsConstructor
@Builder
@Data
public class FileDetectFace {
    private String filename;
    private String message;
    //Todo: criar enum
    private String status;
}
