package com.sheandsoul.v1update.controller;

import com.sheandsoul.v1update.dto.MusicDto;
import com.sheandsoul.v1update.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @GetMapping("/get")
    public List<MusicDto> getAllMusic() {
        return musicService.getAllMusic();
    }

    @PostMapping("/post")
    public MusicDto addMusic(@RequestBody MusicDto musicDto) {
        return musicService.addMusic(musicDto);
    }
}
