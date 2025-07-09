package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.Profile.UserType;

public record ProfileResponse(
    Long profileId,
    Long userId,
    String name,
    String email,
    UserType userType,
    String referralCode
) {

}
