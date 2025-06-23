package com.li.javainterview.controller;

import com.li.javainterview.service.QuestionService;
import com.li.javainterview.model.QuestionAnswer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String categories(Model model) {
        model.addAttribute("categories", service.categories());
        return "index";
    }

    @GetMapping("/question")
    public String randomQuestion(@RequestParam(name = "category") List<String> categories, Model model) {
        Optional<QuestionAnswer> qa = service.getRandomQuestion(categories);
        qa.ifPresent(q -> model.addAttribute("qa", q));
        model.addAttribute("categories", categories);
        return "question";
    }
}
