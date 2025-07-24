package com.sheandsoul.v1update.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {


}
