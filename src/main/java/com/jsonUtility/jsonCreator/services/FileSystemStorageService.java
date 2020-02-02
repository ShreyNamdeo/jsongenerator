package com.jsonUtility.jsonCreator.services;

import com.jsonUtility.jsonCreator.model.FileVersion;
import com.jsonUtility.jsonCreator.repositories.FileVersionsRepository;
import com.jsonUtility.jsonCreator.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;

import static com.amazonaws.HttpMethod.GET;

@Service
public class FileSystemStorageService {
	
	private Path uploadLocation;

	AWSServices awsServices;

	@Autowired
	FileSystemStorageService(AWSServices awsServices){
		this.awsServices = awsServices;
	}

	@Autowired
	FileVersionsRepository fileVersionsRepository;

	/*@PostConstruct
	public void init() {
		this.uploadLocation = Paths.get(Constants.UPLOAD_LOCATION);
		try {
			Files.createDirectories(uploadLocation);
		} catch (IOException e) {
			throw new RuntimeException("Could not initialize storage", e);
		}
	}*/
	
	public void store(MultipartFile file) {
		String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		try {
			if (file.isEmpty()) {
				throw new RuntimeException("Failed to store empty file " + filename);
			}
			
			// This is a security check
			if (filename.contains("..")) {
				throw new RuntimeException("Cannot store file with relative path outside current directory " + filename);
			}
			awsServices.deleteByKey(filename);//Deleting previous version if exist.
			awsServices.uploadFile(file,".xlsx");
			Optional<FileVersion> fileVersion = fileVersionsRepository.findByFileName(filename);
			if (!fileVersion.isPresent()){
				fileVersionsRepository.save(new FileVersion(filename, 1, new Date()));
			}else{
				FileVersion fileVersion1 = fileVersion.get();
				fileVersion1.setUpdatedDate(new Date());
				int oldVersion = fileVersion1.getFileVersion();
				fileVersion1.setFileVersion(oldVersion+1);
				fileVersionsRepository.save(fileVersion1);
			}
			try (InputStream inputStream = file.getInputStream()) {
				//Files.copy(inputStream, this.uploadLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to store file " + filename, e);
		}
	}

	public Resource loadAsResource(String filename) {
		try {
			Path file = uploadLocation.resolve(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new RuntimeException("Could not read file: " + filename);
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException("Could not read file: " + filename, e);
		}
	}

	public List<Path> listSourceFiles(Path dir) throws IOException {
		List<Path> result = new ArrayList<>();
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{xlsx}")) {
			for (Path entry : stream) {
				result.add(entry);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	@Deprecated
	public Path getUploadLocation() {
		return uploadLocation;
	}

	@Transactional
	public Boolean deleteResource(String filename) {
		try {
			if (awsServices.isFileWithNameExist(filename)) {
				//File file1 = resource.getFile();
				awsServices.deleteByKey(filename);
				fileVersionsRepository.deleteByFileName(filename);
				return true;
			} else {
				throw new RuntimeException("Could not delete file: " + filename);
			}
		} catch (Exception e){
			throw  new RuntimeException("File not deleted , please try again " + filename, e);
		}
	}

	public List<FileVersion> getAllStoredFiles() {
		return fileVersionsRepository.findAll();
	}
}

