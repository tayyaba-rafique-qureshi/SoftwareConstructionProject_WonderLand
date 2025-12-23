// Wonderland/src/main/java/com/wonderland/controllers/QuizController.java
package com.wonderland.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wonderland.models.QuizAnswers;
import com.wonderland.models.Toy;
import com.wonderland.services.ToyService;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private ToyService toyService;

    // Fetch categories dynamically based on age
    @GetMapping("/categories")
    public List<String> getCategories(@RequestParam String ageGroup) {
        return toyService.getCategoriesByAgeGroup(ageGroup);
    }

    @PostMapping("/submit")
    public List<Toy> submitQuiz(@RequestBody QuizAnswers answers) {
        return toyService.getQuizResults(answers);
    }
}