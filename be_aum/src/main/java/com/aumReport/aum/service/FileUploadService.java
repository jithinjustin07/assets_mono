package com.aumReport.aum.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileUploadService {
    
    void blackDiamondUpload(MultipartFile file) throws IOException;

    void addeparUpload(MultipartFile file) throws IOException;

    void investmentTypeUpload(MultipartFile file) throws IOException;

    void relationshipManager(MultipartFile file) throws IOException;

    void addeparAdvisorUpdate(MultipartFile file) throws IOException;

    void bdAdvisorUpdate(MultipartFile file) throws IOException;

    void blackDiamondUploadV2(MultipartFile file) throws IOException;

    void addeparUploadV2(MultipartFile file) throws IOException;

}
