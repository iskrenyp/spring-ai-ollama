package com.spring.ollama.resource
import com.spring.ollama.service.DataPersistenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/data")
class DataController {

  final DataPersistenceService dataPersistenceService

  final JdbcTemplate jdbcTemplate

  @Autowired
  DataController(DataPersistenceService dataPersistenceService, JdbcTemplate jdbcTemplate) {
    this.dataPersistenceService = dataPersistenceService
    this.jdbcTemplate = jdbcTemplate
  }

  @PostMapping("/load")
  ResponseEntity<String> load() {
    try {
      this.dataPersistenceService.loadRagData()
      ResponseEntity.ok("Data loaded successfully!")
    } catch (Exception e) {
      ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An error occurred while loading data: " + e.getMessage())
    }
  }

  @GetMapping("/count")
  int count() {
    String sql = "SELECT COUNT(*) FROM vector_store"
    jdbcTemplate.queryForObject(sql, Integer.class)
  }

  @PostMapping("/delete")
  void delete() {
    String sql = "DELETE FROM vector_store"
    jdbcTemplate.update(sql)
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<String> handleException(Exception e) {
    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body("An error occurred in the controller: " + e.getMessage())
  }
}
