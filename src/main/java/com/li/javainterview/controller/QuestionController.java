package com.li.javainterview.controller;

import com.li.javainterview.service.QuestionService;
import com.li.javainterview.model.QuestionAnswer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
public class QuestionController {

    private final QuestionService service;

    public QuestionController(QuestionService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String categories(Model model,
                             @CookieValue(value = "categories", required = false) String categoriesCookie,
                             HttpSession session) {
        model.addAttribute("categories", service.categories());
        if (categoriesCookie != null) {
            List<String> selected = parseCategories(categoriesCookie);
            model.addAttribute("selectedCategories", selected);
            session.setAttribute("categories", selected);
        }
        session.removeAttribute("history");
        session.removeAttribute("index");
        return "index";
    }

    @GetMapping("/question")
    public String randomQuestion(@RequestParam(name = "category", required = false) List<String> categories,
                                 @RequestParam(name = "nav", required = false) String nav,
                                 @CookieValue(value = "categories", required = false) String categoriesCookie,
                                 HttpServletResponse response,
                                 HttpSession session,
                                 Model model) {
        if (categories != null) {
            String raw = String.join(",", categories);
            String encoded = URLEncoder.encode(raw, StandardCharsets.UTF_8);
            Cookie c = new Cookie("categories", encoded);
            c.setPath("/");
            c.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(c);
            session.setAttribute("categories", categories);
            session.removeAttribute("history");
            session.removeAttribute("index");
        } else {
            Object fromSession = session.getAttribute("categories");
            if (fromSession == null && categoriesCookie != null) {
                categories = parseCategories(categoriesCookie);
                session.setAttribute("categories", categories);
            } else {
                categories = (List<String>) fromSession;
            }
        }

        List<QuestionAnswer> history = (List<QuestionAnswer>) session.getAttribute("history");
        Integer index = (Integer) session.getAttribute("index");
        if (history == null) {
            history = new ArrayList<>();
            index = -1;
        }

        QuestionAnswer qa = null;
        if ("back".equals(nav)) {
            if (index != null && index > 0) {
                index--;
                qa = history.get(index);
            }
        } else {
            if (index != null && history.size() > index + 1) {
                history = history.subList(0, index + 1);
            }
            Optional<QuestionAnswer> opt = service.getRandomQuestion(categories);
            if (opt.isPresent()) {
                qa = opt.get();
                history.add(qa);
                index = index == null ? 0 : index + 1;
            }
        }

        session.setAttribute("history", history);
        session.setAttribute("index", index);

        if (qa != null) {
            model.addAttribute("qa", qa);
        }
        model.addAttribute("categories", categories);
        return "question";
    }

    private List<String> parseCategories(String value) {
        String decoded;
        try {
            decoded = URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            decoded = "";
        }
        return Arrays.stream(decoded.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
