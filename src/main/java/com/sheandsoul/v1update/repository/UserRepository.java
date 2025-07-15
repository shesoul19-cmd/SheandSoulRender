package com.sheandsoul.v1update.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.User;
import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findById(Long id);
    
    
    

}
