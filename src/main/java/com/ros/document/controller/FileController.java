package com.ros.document.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.gson.Gson;
import com.ros.document.model.Document;
import com.ros.document.payload.UploadFileResponse;
import com.ros.document.service.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@CrossOrigin("*")
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private FileStorageService fileStorageService;

	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
		String fileName = fileStorageService.storeFile(file);

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();

		String documentType = file.getContentType();

		long documentSize = file.getSize();

		Document document = fileStorageService.saveDocument(fileName, fileDownloadUri, documentType, documentSize);

		// System.out.println("the generated URL IS------- " +
		// fileStorageService.getUrl(document.getDocumentId()));

		return new UploadFileResponse(fileName, fileDownloadUri, documentType, documentSize);
	}

	/*
	 * @GetMapping("/geturl") public String getURL(@RequestParam("id") UUID id) {
	 * return fileStorageService.getUrl(id); }
	 */

	@PostMapping("/uploadMultipleFiles")
	public List<UploadFileResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
		return Arrays.asList(files).stream().map(file -> uploadFile(file)).collect(Collectors.toList());
	}

	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
	
	@PostMapping("/simpleJson")
	public ResponseEntity<?> getSimpleJson(@RequestBody Object obj) {
		Gson Objectconverter = new Gson();
		String jsonString =Objectconverter.toJson(obj);
		Map<String, Object> flattenJson = JsonFlattener.flattenAsMap(jsonString);
		return new ResponseEntity(flattenJson, HttpStatus.OK);
	}

}
