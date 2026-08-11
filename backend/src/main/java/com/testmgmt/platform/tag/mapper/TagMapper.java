package com.testmgmt.platform.tag.mapper;

import com.testmgmt.platform.tag.dto.TagDto;
import com.testmgmt.platform.tag.entity.Tag;

public final class TagMapper {

    private TagMapper() {}

    public static TagDto toDto(Tag tag) {
        return new TagDto(tag.getId(), tag.getProjectId(), tag.getName(), tag.getCreatedAt());
    }
}
