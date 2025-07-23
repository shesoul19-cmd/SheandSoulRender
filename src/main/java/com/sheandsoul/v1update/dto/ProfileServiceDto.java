package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.UserServiceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileServiceDto {
    private UserServiceType preferredServiceType;

    public ProfileServiceDto(UserServiceType preferredServiceType) {
        this.preferredServiceType = preferredServiceType;
    }

    }
