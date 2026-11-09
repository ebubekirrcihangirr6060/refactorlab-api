package com.refactorlab.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

// Analiz için gönderilen legacy kod / SQL parçası (ana CRUD kaynağı)
@Entity
@Table(name = "code_snippet")
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MySQL AUTO_INCREMENT
    @Column(name = "id")
    private int id;

    @NotBlank(message = "title is required")
    @Size(max = 150, message = "title must be at most 150 characters")
    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @NotBlank(message = "language is required")
    @Pattern(regexp = "JAVA|SQL|JPQL|KOTLIN|CSHARP|PYTHON", message = "language must be one of JAVA, SQL, JPQL, KOTLIN, CSHARP, PYTHON")
    @Column(name = "language", nullable = false, length = 20)
    private String language;

    @NotBlank(message = "sourceCode is required")
    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "complexityLevel must be LOW, MEDIUM or HIGH")
    @Column(name = "complexity_level", length = 10)
    private String complexityLevel;

    @Size(max = 50, message = "bottleneckType must be at most 50 characters")
    @Column(name = "bottleneck_type", length = 50)
    private String bottleneckType;

    @Column(name = "owner_username", length = 50)
    private String ownerUsername;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // JPA için parametresiz constructor
    public CodeSnippet() {
    }

    public CodeSnippet(String title, String language, String sourceCode, String complexityLevel, String bottleneckType) {
        this.title = title;
        this.language = language;
        this.sourceCode = sourceCode;
        this.complexityLevel = complexityLevel;
        this.bottleneckType = bottleneckType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public String getBottleneckType() {
        return bottleneckType;
    }

    public void setBottleneckType(String bottleneckType) {
        this.bottleneckType = bottleneckType;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CodeSnippet{id=" + id + ", title='" + title + "', language='" + language + "'}";
    }
}
