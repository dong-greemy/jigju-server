package com.jigju.server.location.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmdPathResult {
    private ArrayList<EmdTransferPathDto> emdsTransferRoutes;
    private int minTime;
    private int minPath;
}

