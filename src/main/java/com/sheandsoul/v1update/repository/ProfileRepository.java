package com.sheandsoul.v1update.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.Profile.UserType;

import java.util.List;
import java.util.Optional;



public interface ProfileRepository extends JpaRepository<Profile, Long> {

    boolean existsByReferralCode(String referralCode);

    Optional<Profile> findByReferralCode(String referralCode);
    Optional<Profile> findByUserId(Long userId);

    List<Profile> findByUserType(UserType user);
}
