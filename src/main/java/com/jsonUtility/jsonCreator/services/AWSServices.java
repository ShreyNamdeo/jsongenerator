package com.jsonUtility.jsonCreator.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.jsonUtility.jsonCreator.model.AmazonService;
import com.jsonUtility.jsonCreator.repositories.AmazonServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

/**
 * Created by shrey on 3/27/2019.
 */
@Service
public class AWSServices {
    private String bucketName = "excelfiles-jsongenerator";
    private String endpointUrl = "https://"+bucketName+".s3.amazonaws.com";

    @Autowired
    private AmazonServiceRepository amazonServiceRepository;

    private AmazonService getCredsForS3(String serviceName){
        return amazonServiceRepository.findByServiceName(serviceName);
    }

    private AmazonS3 s3client;

    @PostConstruct
    private void initializeAmazon() {
        AmazonService service = getCredsForS3("aws-s3");
        AWSCredentials credentials = new BasicAWSCredentials(service.getAccessKey(), service.getSecretKey());
        this.s3client = new AmazonS3Client(credentials);
        this.s3client.setRegion(Region.getRegion(Regions.US_EAST_2));
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public Boolean isFileWithNameExist(String filename){
        return s3client.doesObjectExist(bucketName,filename);
    }

    private String generateFileName(MultipartFile multiPart) {
        return multiPart.getOriginalFilename();
    }

    private void uploadFileTos3bucket(String fileName, File file) {
        s3client.putObject(new PutObjectRequest(bucketName, fileName, file));
    }

    public String uploadFile(MultipartFile multipartFile, String contentType) {
        String fileName = "";
        try {
            File file = convertMultiPartToFile(multipartFile);
            fileName = generateFileName(multipartFile);
            uploadFileTos3bucket(fileName, file);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    public URL generatePresignedUrl(String key, HttpMethod method, String contentType) {
        GeneratePresignedUrlRequest createUrl = new GeneratePresignedUrlRequest(bucketName, key, method);
        Date fifteenMinutesFromNow = Date.from(LocalDateTime.now(Clock.systemUTC()).plusMinutes(15).toInstant(UTC));
        //createUrl.setExpiration(fifteenMinutesFromNow);
        createUrl.setResponseHeaders(new ResponseHeaderOverrides().withContentType(contentType));
        s3client.setRegion(Region.getRegion(Regions.US_EAST_2));
        return s3client.generatePresignedUrl(createUrl);
    }

    public void deleteByKey(String key){
        s3client.deleteObject(new DeleteObjectRequest(bucketName,key));
    }

    public File getFileByKey(String key){
        File file = new File(key);
        try{
            S3Object s3Object = s3client.getObject(bucketName,key);
            InputStream in = s3Object.getObjectContent();
            byte[] buf = new byte[1024];
            OutputStream out = new FileOutputStream(file);
            int count;
            while( (count = in.read(buf)) != -1)
            {
                if( Thread.interrupted() )
                {
                    throw new InterruptedException();
                }
                out.write(buf, 0, count);
            }
            out.close();
            in.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return file;
    }

    public InputStream getFileInputStreamByKey(String key) {
        S3Object s3Object = s3client.getObject(bucketName,key);
        return s3Object.getObjectContent();
    }
}
