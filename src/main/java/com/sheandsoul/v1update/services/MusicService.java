package com.sheandsoul.v1update.services;

import com.sheandsoul.v1update.dto.MusicDto;
import com.sheandsoul.v1update.entities.Music;
import com.sheandsoul.v1update.mapper.MusicMapper;
import com.sheandsoul.v1update.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicService {

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private MusicMapper musicMapper;

    public List<MusicDto> getAllMusic() {
        return musicMapper.toDtoList(musicRepository.findAll());
    }

    public MusicDto addMusic(MusicDto musicDto) {
        Music music = musicMapper.toEntity(musicDto);
        return musicMapper.toDto(musicRepository.save(music));
    }
}
