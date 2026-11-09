package com.refactorlab.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

// Bir kod parçası için yapılmış analizin sonucu (revizyon geçmişi)
@Entity
@Table(name = "analysis_result")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    // Bir snippet'in birden çok analizi olabilir
    @ManyToOne
    @JoinColumn(name = "snippet_id", nullable = false)
    private CodeSnippet snippet;

    @Column(name = "engine_name", length = 50)
    private String engineName;

    @Column(name = "detected_smells", length = 1000)
    private String detectedSmells;

    @Column(name = "suggested_techniques", length = 1000)
    private String suggestedTechniques;

    @Column(name = "modernization_hint", columnDefinition = "TEXT")
    private String modernizationHint;

    @Column(name = "complexity_before", length = 20)
    private String complexityBefore;

    @Column(name = "complexity_after", length = 20)
    private String complexityAfter;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AnalysisResult() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public CodeSnippet getSnippet() {
        return snippet;
    }

    public void setSnippet(CodeSnippet snippet) {
        this.snippet = snippet;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public String getDetectedSmells() {
        return detectedSmells;
    }

    public void setDetectedSmells(String detectedSmells) {
        this.detectedSmells = detectedSmells;
    }

    public String getSuggestedTechniques() {
        return suggestedTechniques;
    }

    public void setSuggestedTechniques(String suggestedTechniques) {
        this.suggestedTechniques = suggestedTechniques;
    }

    public String getModernizationHint() {
        return modernizationHint;
    }

    public void setModernizationHint(String modernizationHint) {
        this.modernizationHint = modernizationHint;
    }

    public String getComplexityBefore() {
        return complexityBefore;
    }

    public void setComplexityBefore(String complexityBefore) {
        this.complexityBefore = complexityBefore;
    }

    public String getComplexityAfter() {
        return complexityAfter;
    }

    public void setComplexityAfter(String complexityAfter) {
        this.complexityAfter = complexityAfter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AnalysisResult{id=" + id + ", engineName='" + engineName + "', detectedSmells='" + detectedSmells + "'}";
    }
}
