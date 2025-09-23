package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.Profile;
import com.sheandsoul.v1update.entities.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UserProfileDto {
    private Long userId;
    private String email;
    private String name;
    private String nickname;
    private String userType;
    private Integer age;
    private Float height;
    private Float weight;
    private Integer periodLength;
    private Integer cycleLength;
    private LocalDate lastPeriodStartDate;

    public static UserProfileDto from(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getId());
        dto.setEmail(user.getEmail());

        Profile profile = user.getProfile();
        if (profile != null) {
            dto.setName(profile.getName());
            dto.setNickname(profile.getNickName());
            dto.setUserType(profile.getUserType() != null ? profile.getUserType().name() : null);
            dto.setAge(profile.getAge());
            dto.setHeight(profile.getHeight() != null ? profile.getHeight().floatValue() : null);
            dto.setWeight(profile.getWeight() != null ? profile.getWeight().floatValue() : null);
            dto.setPeriodLength(profile.getPeriodLength());
            dto.setCycleLength(profile.getCycleLength());
            dto.setLastPeriodStartDate(profile.getLastPeriodStartDate());
        }
        return dto;
    }
}