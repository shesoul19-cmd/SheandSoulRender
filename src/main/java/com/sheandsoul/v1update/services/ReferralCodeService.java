package com.sheandsoul.v1update.services;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Service;

@Service
public class ReferralCodeService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    public String generateCode(Long id) {
        byte[] randomBytes = new byte[4];
        SECURE_RANDOM.nextBytes(randomBytes);
        
        String idPart = Long.toHexString(id);
        String randomPart = BASE64_ENCODER.encodeToString(randomBytes);
        String combined = idPart + randomPart;
        long hash = 17;
        for (char c : combined.toCharArray()) {
            hash = 31 * hash + c;
        }
        String base36 = Long.toString(Math.abs(hash), 36).toUpperCase();
        return (base36 + "ABCDEF").substring(0, 6);
    }

}
