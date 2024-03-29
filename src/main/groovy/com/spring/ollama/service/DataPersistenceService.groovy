package com.spring.ollama.service
import groovy.io.FileType
import groovy.util.logging.Slf4j
import org.springframework.ai.document.Document
import org.springframework.ai.reader.TextReader
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.FileUrlResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.util.Assert

@Service
@Slf4j
class DataPersistenceService {

  final VectorStore vectorStore

  static final List<String> INCLUDED_FILES = ['.groovy', '.yaml', '.gradle']

  static List<Resource> loadCodeResource(String pathToData = '.') {
    List<File> codeOrConfig = new ArrayList<>()
    new File(pathToData).eachFileRecurse(FileType.FILES) { File file ->
      if (INCLUDED_FILES.any { file.name.endsWith(it) }) {
        codeOrConfig << file
      }
    }
    codeOrConfig.collect {new FileUrlResource(it.path) }
  }

  static List<Document> splitIntoDocuments(Resource resource) {
    try {
      TextReader reader = new TextReader(resource)
      TokenTextSplitter textSplitter = new TokenTextSplitter()
      textSplitter.apply(reader.get())
    } catch (RuntimeException e) {
      log.error("There was an exception while splitting resource with name ${resource.filename}")
      []
    }
  }

  @Autowired
  DataPersistenceService(VectorStore vectorStore) {
    Assert.notNull(vectorStore, "VectorStore must not be null.")
    this.vectorStore = vectorStore
  }

  void loadRagData() {
    List<Resource> code = loadCodeResource()
    Integer vectorRecordsSize = 0
    code.each {
      List<Document> documents = splitIntoDocuments(it)
      log.info("Saving ${documents.size()} embedded documents in vector store for file ${it.filename} . It may take some time...")
      this.vectorStore.accept(documents)
      vectorRecordsSize += documents.size()
    }
    log.info("Saved $vectorRecordsSize embedded documents against ${code.size()} code files")
  }
}
