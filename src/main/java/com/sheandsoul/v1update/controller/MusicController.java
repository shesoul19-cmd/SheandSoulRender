package com.sheandsoul.v1update.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sheandsoul.v1update.dto.MusicDto;
import com.sheandsoul.v1update.services.MusicService;

@RestController
@RequestMapping("/api/v1/music")
public class MusicController {

    @Autowired
    private MusicService musicService;


    @GetMapping("/get")
    @Cacheable("musicList")
    public List<MusicDto> getAllMusic() {
        return musicService.getAllMusic();
    }

    @PostMapping("/post")
    public MusicDto addMusic(@RequestBody MusicDto musicDto) {
        return musicService.addMusic(musicDto);
    }
}
