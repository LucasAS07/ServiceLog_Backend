package io.lrsystem.ServiceLog.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class CustomError {

    private Instant timestamp;
    private Integer status;
    private String error;
    private String path;

}
