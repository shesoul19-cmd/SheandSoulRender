package com.sheandsoul.v1update.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.PCOSAssesmentRequest;
import com.sheandsoul.v1update.entities.PCOSAssesment;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MyUserDetailService;
import com.sheandsoul.v1update.services.PcosService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/pcos")
public class PcosController {

    private final PcosService pcosService;
    private final MyUserDetailService myUserDetailsService;

    public PcosController(PcosService pcosService, MyUserDetailService myUserDetailsService) {
        this.pcosService = pcosService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @PostMapping("/assess")
    public ResponseEntity<?> createAssessment(@Valid @RequestBody PCOSAssesmentRequest request, Authentication authentication) {
        try {
            User currentUser = myUserDetailsService.findUserByEmail(authentication.getName());
            PCOSAssesment result = pcosService.assessAndSave(currentUser.getId(), request);

            return ResponseEntity.ok(Map.of(
                "message", "PCOS assessment completed successfully.",
                "riskLevel", result.getRiskLevel()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
