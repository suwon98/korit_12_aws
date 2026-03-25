package com.example.rdsspringbootsample;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

  private BoardRepository boardRepository;

  public AppController(BoardRepository boardRepository) {
    this.boardRepository = boardRepository;
  }

  @GetMapping("health")
  public ResponseEntity<String> healthCheck() {
    return ResponseEntity.ok().body("Success Health Check");
  }

  @GetMapping("boards")
  public ResponseEntity<List<Board>> getBoards() {
    // 더미 데이터 생성을 위한 게시글 저장 로직
    Board board = new Board("게시글 제목", "게시글 내용");
    boardRepository.save(board);

    // 게시글 조회
    List<Board> boards = boardRepository.findAll();
    return ResponseEntity.ok().body(boards);
  }
}