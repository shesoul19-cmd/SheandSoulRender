package com.sheandsoul.v1update.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.Article;
import com.sheandsoul.v1update.entities.UserServiceType;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByServiceType(UserServiceType serviceType);

}
