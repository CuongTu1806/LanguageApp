package com.example.appNN.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "home";
    }
    
    @GetMapping("/learning/{languageCode}")
    public String learningDashboard(@PathVariable String languageCode, Model model) {
        String languageName;
        String languageIcon;
        
        switch (languageCode.toLowerCase()) {
            case "cn":
                languageName = "Tiếng Trung";
                languageIcon = "🇨🇳";
                break;
            case "en":
                languageName = "Tiếng Anh";
                languageIcon = "🇬🇧";
                break;
            default:
                languageName = "Ngoại Ngữ";
                languageIcon = "🌍";
        }
        
        model.addAttribute("languageCode", languageCode);
        model.addAttribute("languageName", languageName);
        model.addAttribute("languageIcon", languageIcon);
        
        return "learning_dashboard";
    }
}
