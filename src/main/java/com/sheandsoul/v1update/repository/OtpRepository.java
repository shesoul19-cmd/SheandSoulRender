package com.sheandsoul.v1update.repository;

import com.sheandsoul.v1update.entities.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query("SELECT o FROM Otp o WHERE o.email = :email AND o.used = false AND o.expiresAt > :now ORDER BY o.createdAt DESC")
    List<Otp> findValidOtpsByEmail(@Param("email") String email, @Param("now") LocalDateTime now);

    @Query("SELECT o FROM Otp o WHERE o.email = :email AND o.otpCode = :otpCode AND o.used = false AND o.expiresAt > :now")
    Optional<Otp> findValidOtpByEmailAndCode(@Param("email") String email, @Param("otpCode") String otpCode, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Otp o SET o.used = true WHERE o.email = :email")
    void markAllOtpsAsUsed(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.email = :email")
    void deleteAllOtpsByEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM Otp o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") LocalDateTime now);
} 