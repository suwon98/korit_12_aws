package com.example.awss3springboot;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class AppController {

  private BoardRepository boardRepository;
  private S3Operations s3Operations;

  @Value("${spring.cloud.aws.s3.bucket}")
  private String bucketName;

  public AppController(
      BoardRepository boardRepository,
      S3Operations s3Operations
  ) {
    this.boardRepository = boardRepository;
    this.s3Operations = s3Operations;
  }

  @GetMapping("health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok().body("Success Health Check");
  }

  @GetMapping("boards")
  public ResponseEntity<List<Board>> getBoards() {
    List<Board> boards = boardRepository.findAll();
    return ResponseEntity.ok().body(boards);
  }

  @PostMapping("boards")
  public ResponseEntity<Board> createBoard(
      @RequestPart("image") MultipartFile imageFile,
      @RequestPart("title") String title,
      @RequestPart("content") String content
  ) {
    String originalFilename = imageFile.getOriginalFilename();
    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
    String filename = Instant.now().getEpochSecond() + fileExtension;

    String imageUrl;
    try (InputStream inputStream = imageFile.getInputStream()) {
      S3Resource s3Resource = s3Operations.upload(bucketName, filename, inputStream,
          ObjectMetadata.builder().contentType(imageFile.getContentType()).build());
      imageUrl = s3Resource.getURL().toString();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Board board = new Board(title, content, imageUrl);
    boardRepository.save(board);
    return ResponseEntity.ok().body(board);
  }
}