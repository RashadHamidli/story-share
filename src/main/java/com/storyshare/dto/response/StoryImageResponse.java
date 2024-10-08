package com.storyshare.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class StoryImageResponse {
    private UUID id;
    private String url;
    private Boolean isMain;
}
