package com.sheandsoul.v1update.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.entities.Article;
import com.sheandsoul.v1update.repository.ArticleRepository;
@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleRepository articleRepository;

    public ArticleController(ArticleRepository articleRepository){
        this.articleRepository = articleRepository;
    }

    @PostMapping
    public Article createArticle(@RequestBody Article article){
        return articleRepository.save(article);
    }

    @GetMapping
    public List<Article> getArticles(){
        return articleRepository.findAll();
    }

}
