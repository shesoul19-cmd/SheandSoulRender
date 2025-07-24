package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.UserServiceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileServiceDto {
    private UserServiceType preferredServiceType;

    }
