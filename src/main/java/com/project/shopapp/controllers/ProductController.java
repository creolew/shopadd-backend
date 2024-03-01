package com.project.shopapp.controllers;

import com.project.shopapp.dtos.ProductDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    @PostMapping(value = "test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> testAPI(@Valid @ModelAttribute ProductDTO productDto){
        System.out.println("Huy test");
        return ResponseEntity.ok("test API" +productDto.getDescription());
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(@Valid @ModelAttribute ProductDTO productDto,
//                                           @RequestPart("file")MultipartFile file,
                                           BindingResult result
                                           ){
        try {
            if (result.hasErrors()) {
                List<String> errorMessages = result.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest().body(errorMessages);
            }
            List<MultipartFile> files = productDto.getFiles();
            files = files == null ? new ArrayList<MultipartFile>() : files;
            for(MultipartFile file : files){
                    if(file.getSize() == 0){
                        continue;
                    }

                    if(file.getSize() > 10 * 1024 * 1024) { //kich thuoc > 10MB
                        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .body("FIle is too large! Maximum size is 10MB");
                    }
                    //Kiem tra kich thuoc file va dinh dang

                    String contentType = file.getContentType();
                    if(contentType == null || !contentType.startsWith("image/")){
                        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .body("FIle must be an image");
                    }
                    //Luu file va cap nhat thumbnail trong DTO
                    String filename = storeFile(file);
                    //Luu vao product trong DB
                    //Luu vao bang product_images
            }

            return ResponseEntity.ok("Product create successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private String storeFile(MultipartFile file) throws IOException{
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        //Them UUID vao truoc ten file de dam bao ten file la duy nhat
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        //Duong dan den thu muc ma ban muon luu file
        java.nio.file.Path uploadDir = Paths.get("uploads");
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        //Duong dan day du den file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(), uniqueFilename);
        //Sao chep file vao thu muc dich
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    @GetMapping("")
    public ResponseEntity<String> getProducts(@RequestParam("page") int page, @RequestParam("limit") int limit){
        return ResponseEntity.ok("getProducts here");
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id") String productId){
        return ResponseEntity.ok("Product with ID: " + productId);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id) {
        return ResponseEntity.ok("Delete Product");
    }

}
