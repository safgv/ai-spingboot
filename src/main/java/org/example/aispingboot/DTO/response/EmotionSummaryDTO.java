package org.example.aispingboot.DTO.response;


import lombok.Data;


@Data
public class EmotionSummaryDTO {


    private String mainEmotion;


    private String trend;


    private String summary;

}