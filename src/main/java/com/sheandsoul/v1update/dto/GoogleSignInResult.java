package com.sheandsoul.v1update.dto;

import com.sheandsoul.v1update.entities.User;

public record GoogleSignInResult(
  User user, boolean isNewUser
) {

}
