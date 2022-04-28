package com.ros.document.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ros.document.exception.FileStorageException;
import com.ros.document.exception.MyFileNotFoundException;
import com.ros.document.model.Document;

import com.ros.document.property.FileStorageProperties;
import com.ros.document.repository.DocumentRepository;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

	private final Path fileStorageLocation;

	@Autowired
	public FileStorageService(FileStorageProperties fileStorageProperties) {
		this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

		try {
			Files.createDirectories(this.fileStorageLocation);
		} catch (Exception ex) {
			throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
					ex);
		}
	}

	public String storeFile(MultipartFile file) {
		// Normalize file name
		String fileName = StringUtils.cleanPath(file.getOriginalFilename());

		try {
			// Check if the file's name contains invalid characters
			if (fileName.contains("..")) {
				throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
			}

			// Copy file to the target location (Replacing existing file with the same name)
			Path targetLocation = this.fileStorageLocation.resolve(fileName);
			Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

			return fileName;
		} catch (IOException ex) {
			throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
		}
	}

	@Autowired
	DocumentRepository documentRepo;

	public Document saveDocument(String fileName, String documentURL, String documentType, long documentSize) {
		Document document = new Document();

		document.setDocumentName(fileName);

		URI documentUri = null;
		try {
			documentUri = new URI(documentURL);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			log.error("Inavlid URL format");

		}

		document.setDocumentUrl(documentUri);

		document.setDocumentType(documentType);
		document.setDocumentSize(documentSize);
		document.addMetaData();

		return documentRepo.save(document);
	}

	/*
	 * public String getUrl(UUID id) { Optional<Document> document =
	 * documentRepo.findById(id);
	 * 
	 * URI uri = document.get().getDocumentUrl(); return uri.toString(); }
	 */

	public Resource loadFileAsResource(String fileName) {
		try {
			Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());
			if (resource.exists()) {
				return resource;
			} else {
				throw new MyFileNotFoundException("File not found " + fileName);
			}
		} catch (MalformedURLException ex) {
			throw new MyFileNotFoundException("File not found " + fileName, ex);
		}
	}
}
