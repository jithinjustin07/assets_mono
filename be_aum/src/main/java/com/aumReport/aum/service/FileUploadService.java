package com.aumReport.aum.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    
    void blackDiamondUpload(MultipartFile file) throws IOException;
    
}
