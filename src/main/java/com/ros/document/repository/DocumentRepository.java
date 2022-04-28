package com.ros.document.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ros.document.model.Document;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

}
