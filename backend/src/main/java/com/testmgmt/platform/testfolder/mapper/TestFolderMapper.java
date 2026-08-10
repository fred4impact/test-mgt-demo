package com.testmgmt.platform.testfolder.mapper;

import com.testmgmt.platform.testfolder.dto.TestFolderDto;
import com.testmgmt.platform.testfolder.entity.TestFolder;

public final class TestFolderMapper {

    private TestFolderMapper() {}

    public static TestFolderDto toDto(TestFolder folder) {
        return new TestFolderDto(
                folder.getId(), folder.getProjectId(), folder.getParentId(), folder.getName(), folder.getCreatedAt());
    }
}
