package com.refactorlab.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Kurumsal refactoring kuralı: hangi kod kokusuna hangi teknik uygulanır (Reviewer yönetir)
@Entity
@Table(name = "refactoring_rule")
public class RefactoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @NotBlank(message = "smellName is required")
    @Size(max = 100, message = "smellName must be at most 100 characters")
    @Column(name = "smell_name", nullable = false, unique = true, length = 100)
    private String smellName;

    @NotBlank(message = "category is required")
    @Size(max = 50, message = "category must be at most 50 characters")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @NotBlank(message = "technique is required")
    @Size(max = 100, message = "technique must be at most 100 characters")
    @Column(name = "technique", nullable = false, length = 100)
    private String technique;

    @Size(max = 1000, message = "description must be at most 1000 characters")
    @Column(name = "description", length = 1000)
    private String description;

    public RefactoringRule() {
    }

    public RefactoringRule(String smellName, String category, String technique, String description) {
        this.smellName = smellName;
        this.category = category;
        this.technique = technique;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSmellName() {
        return smellName;
    }

    public void setSmellName(String smellName) {
        this.smellName = smellName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTechnique() {
        return technique;
    }

    public void setTechnique(String technique) {
        this.technique = technique;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "RefactoringRule{id=" + id + ", smellName='" + smellName + "', technique='" + technique + "'}";
    }
}
