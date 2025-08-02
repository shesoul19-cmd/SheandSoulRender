package com.sheandsoul.v1update.mapper;

import com.sheandsoul.v1update.dto.MusicDto;
import com.sheandsoul.v1update.entities.Music;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MusicMapper {

    public MusicDto toDto(Music music) {
        return new MusicDto(music.getId(), music.getTitle(), music.getUrl());
    }

    public Music toEntity(MusicDto musicDto) {
        return new Music(musicDto.getId(), musicDto.getTitle(), musicDto.getUrl());
    }

    public List<MusicDto> toDtoList(List<Music> musicList) {
        return musicList.stream().map(this::toDto).collect(Collectors.toList());
    }
}
