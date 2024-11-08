package org.spring.springaicourse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
    private static final int CHUNK_SIZE = 300;

    @Value("classpath:/fy24-preliminary-results.pdf")
    private Resource pdfResource;

    private VectorStore vectorStore;

    public IngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) {

        try {
            var pdfReader = new PagePdfDocumentReader(pdfResource);
            List<Document> documents = pdfReader.get();

            List<Document> documentList = ingestDocuments(documents);

            vectorStore.accept(documentList);
            log.info("VectorStore loaded with increased chunk size data!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Document> ingestDocuments(List<Document> documents) {
        return documents.stream()
                .map(this::ingestDocument)
                .toList();
    }

    private Document ingestDocument(Document documentText) {

        String processedText = preProcessText(documentText.getContent());

        List<String> chunks = adaptiveTextSplitter(processedText);
        return Document.builder().withContent(String.join("\n", chunks)).build();
    }

    public String preProcessText(String rawText) {
        // Clean the text (e.g., removing unnecessary line breaks, special characters)
        rawText = rawText.replace("\n", " ");
        rawText = rawText.replaceAll("[^\\x20-\\x7e]", "");
        return rawText;
    }

    public List<String> adaptiveTextSplitter(String text) {
        // Split by sentences first
        String[] sentences = text.split("(?<=\\.)\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() < CHUNK_SIZE) {
                currentChunk.append(" ").append(sentence);
            } else {
                chunks.add(currentChunk.toString());
                currentChunk.setLength(0);
                currentChunk.append(sentence); // Start a new chunk with the current sentence
            }
        }
        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString()); // Add remaining content as a chunk
        }
        return chunks;
    }

}
