package com.li.javainterview.service;

import com.li.javainterview.model.QuestionAnswer;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionService {
    private final Map<String, List<QuestionAnswer>> questionsByCategory = new LinkedHashMap<>();
    private final Random random = new Random();
    private final ResourceLoader resourceLoader;

    public QuestionService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private static final Pattern LINK_PATTERN = Pattern.compile("\\+ \\[(.+?)\\]\\((.+?)\\)");

    @PostConstruct
    public void init() throws IOException {
        parseReadme();
    }

    private void parseReadme() throws IOException {
        Resource res = resourceLoader.getResource("classpath:data/README.md");
        if (!res.exists()) {
            return;
        }
        List<String> lines;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            lines = br.lines().toList();
        }
        List<String> categories = new ArrayList<>();
        boolean inCategoryList = false;
        for (String l : lines) {
            String line = l.trim();
            if (line.startsWith("# Вопросы для собеседования")) {
                inCategoryList = true;
                continue;
            }
            if (inCategoryList) {
                if (line.startsWith("+ [")) {
                    Matcher m = LINK_PATTERN.matcher(line);
                    if (m.find()) {
                        categories.add(m.group(1));
                    }
                } else if (line.startsWith("[done]")) {
                    inCategoryList = false;
                }
                continue;
            }
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("## ")) {
                String header = line.substring(3).trim();
                if (categories.contains(header)) {
                    String category = header;
                    List<QuestionAnswer> list = new ArrayList<>();
                    for (int j = i + 1; j < lines.size(); j++) {
                        String qLine = lines.get(j).trim();
                        if (qLine.startsWith("+ [")) {
                            Matcher m = LINK_PATTERN.matcher(qLine);
                            if (m.find()) {
                                String question = m.group(1);
                                String link = m.group(2);
                                String fileName = link.split("#", 2)[0];
                                String answer = parseQuestionFile(fileName, question);
                                list.add(new QuestionAnswer(category, question, answer));
                            }
                        } else if (qLine.startsWith("## ") || qLine.startsWith("[к оглавлению") || qLine.isEmpty()) {
                            break;
                        }
                    }
                    if (!list.isEmpty()) {
                        questionsByCategory.put(category, list);
                    }
                }
            }
        }
    }

    private String parseQuestionFile(String fileName, String question) {
        Resource res = resourceLoader.getResource("classpath:data/" + fileName);
        if (!res.exists()) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = br.lines().toList();
            String header = "## " + question;
            boolean capture = false;
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                if (!capture && line.trim().equals(header)) {
                    capture = true;
                    continue;
                }
                if (capture) {
                    if (line.startsWith("## ")) {
                        break;
                    }
                    if (line.startsWith("[к оглавлению")) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
            }
            return sb.toString().trim();
        } catch (IOException e) {
            return "";
        }
    }

    public Set<String> categories() {
        return questionsByCategory.keySet();
    }

    public Optional<QuestionAnswer> getRandomQuestion(String category) {
        List<QuestionAnswer> list = questionsByCategory.get(category);
        if (list == null || list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(random.nextInt(list.size())));
    }

    public Optional<QuestionAnswer> getRandomQuestion(List<String> categories) {
        List<String> allowed = categories.stream()
                .filter(questionsByCategory::containsKey)
                .toList();
        if (allowed.isEmpty()) {
            return Optional.empty();
        }
        String category = allowed.get(random.nextInt(allowed.size()));
        return getRandomQuestion(category);
    }
}
