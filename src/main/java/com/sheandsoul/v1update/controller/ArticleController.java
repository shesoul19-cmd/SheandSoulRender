package com.sheandsoul.v1update.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.entities.Article;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.entities.UserServiceType;
import com.sheandsoul.v1update.repository.ArticleRepository;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.repository.ProfileRepository;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final MyUserDetailService userDetailsService;
    private final ProfileRepository profileRepository;

    public ArticleController(ArticleRepository articleRepository, MyUserDetailService userDetailsService, ProfileRepository profileRepository){
        this.articleRepository = articleRepository;
        this.userDetailsService = userDetailsService;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/post")
    public Article createArticle(@RequestBody Article article){
        return articleRepository.save(article);
    }

    @GetMapping("/get")
    public List<Article> getArticles(Authentication authentication){
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        return profileRepository.findByUserId(currentUser.getId())
            .map(profile -> articleRepository.findByServiceType(profile.getPreferredServiceType()))
            .orElseThrow(() -> new RuntimeException("User profile not found"));
    }

}
