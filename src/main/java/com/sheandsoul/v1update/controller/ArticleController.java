package com.sheandsoul.v1update.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @PostMapping("/post")
    public Article createArticle(@RequestBody Article article){
        return articleRepository.save(article);
    }

    @GetMapping("/get")
    @Cacheable("articles")
    public List<Article> getArticles(){
        return articleRepository.findAll();
    }
     // ✅ START FIX: Add this new method to fetch a single article by its ID
    @GetMapping("/{id}")
    @Cacheable(value = "article", key = "#id")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        // Find the article in the database by its ID
        Optional<Article> articleOptional = articleRepository.findById(id);
        
        // If the article is found, return it with a 200 OK status.
        // If not, return a 404 Not Found status.
        return articleOptional.map(ResponseEntity::ok)
                              .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
