package io.lrsystem.ServiceLog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class CustomError {

    private Instant tiemstamp;
    private Integer status;
    private String error;
    private String path;


}
