package com.example.backend.controller;

import com.example.backend.dto.TrainingForm;
import com.example.backend.service.TrainingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class TrainingController {
    private final TrainingService  trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @GetMapping("/training")
    public String trainingPage(Principal principal, Model model) {
        model.addAttribute("username", principal.getName());
        model.addAttribute("trainingForm",  new TrainingForm());
        model.addAttribute("sampleCOunt", trainingService.CountSamples(principal.getName()));
        model.addAttribute("requiredSamples", trainingService.getRequiredSamples());
        return "training";
    }
}
