package com.li.javainterview.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class QuestionAnswer {
    private String category;
    private String question;
    private String answer;
}
