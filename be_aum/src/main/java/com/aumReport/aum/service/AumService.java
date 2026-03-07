package com.aumReport.aum.service;

import com.aumReport.aum.dto.DataResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AumService {

    List<DataResponse> getData();
    String uploadData(String data);
}
