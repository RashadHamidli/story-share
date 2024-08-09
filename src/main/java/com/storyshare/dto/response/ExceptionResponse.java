package com.storyshare.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ExceptionResponse {
    private String timestamp;
    private String message;
    private String code;
    private Integer status;
}

