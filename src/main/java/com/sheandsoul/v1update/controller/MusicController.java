package com.sheandsoul.v1update.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.MusicDto;
import com.sheandsoul.v1update.entities.User;
import com.sheandsoul.v1update.services.MusicService;
import com.sheandsoul.v1update.services.MyUserDetailService;

@RestController
@RequestMapping("/api/v1/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @Autowired
    private MyUserDetailService userDetailsService;

    @GetMapping("/get")
    public List<MusicDto> getAllMusic(Authentication authentication) {
        User currentUser = userDetailsService.findUserByEmail(authentication.getName());
        return musicService.getAllMusic();
    }

    @PostMapping("/post")
    public MusicDto addMusic(@RequestBody MusicDto musicDto) {
        return musicService.addMusic(musicDto);
    }
}
