package com.tothapplication.service.impl;

import com.tothapplication.config.FileStorageProperties;
import com.tothapplication.service.DocumentService;
import com.tothapplication.domain.Document;
import com.tothapplication.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Document}.
 */
@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    private final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;

    @Autowired
    FileStorageProperties fileStorageProperties;

    private final Path fileStorageLocation;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository) {

        this.documentRepository = documentRepository;

        this.fileStorageLocation = Paths.get("/Users/Thomas/Upload")
            .toAbsolutePath().normalize();

        log.info("Initialisation du repertoire");

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Save a document.
     *
     * @param document the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Document save(Document document) {
        log.debug("Request to save Document : {}", document);
        return documentRepository.save(document);
    }

    /**
     * Get all the documents.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Document> findAll(Pageable pageable) {
        log.debug("Request to get all Documents");
        return documentRepository.findAll(pageable);
    }

    /**
     * Get all the documents with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<Document> findAllWithEagerRelationships(Pageable pageable) {
        return documentRepository.findAllWithEagerRelationships(pageable);
    }
    

    /**
     * Get one document by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Document> findOne(Long id) {
        log.debug("Request to get Document : {}", id);
        return documentRepository.findOneWithEagerRelationships(id);
    }

    /**
     * Delete the document by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Document : {}", id);
        documentRepository.deleteById(id);
    }

    public String storeFile(Long id, MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new IOException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            log.info(targetLocation.toString());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            log.error("Could not store file " + fileName + ". Please try again!", ex);
            return null;
        }
    }

    public Resource loadFileAsResource(Long id) {
        String fileName="";
        try {
            fileName = documentRepository.findById(id).orElseThrow(()->new MalformedURLException("Document does not exists!")).getFilename();
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MalformedURLException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found " + fileName, ex);
            return null;
        }
    }
}