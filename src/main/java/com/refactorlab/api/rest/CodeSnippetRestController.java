package com.refactorlab.api.rest;

import com.refactorlab.api.entity.AnalysisResult;
import com.refactorlab.api.entity.CodeSnippet;
import com.refactorlab.api.service.AnalysisService;
import com.refactorlab.api.service.CodeSnippetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Map;

// HTTP istek/cevaplarını yönetir; veritabanına doğrudan erişmez, Service'i çağırır
@RestController
@RequestMapping("/api/snippets")
@Tag(name = "Code Snippets", description = "Analiz edilecek legacy kod / SQL parçaları")
public class CodeSnippetRestController {

    private final CodeSnippetService codeSnippetService;
    private final AnalysisService analysisService;

    public CodeSnippetRestController(CodeSnippetService theCodeSnippetService, AnalysisService theAnalysisService) {
        codeSnippetService = theCodeSnippetService;
        analysisService = theAnalysisService;
    }

    @Operation(summary = "Tüm kod parçalarını listele (opsiyonel ?language=SQL filtresi)")
    @GetMapping
    public List<CodeSnippet> findAll(@RequestParam(required = false) String language) {
        if (language != null && !language.isBlank()) {
            return codeSnippetService.findByLanguage(language);
        }
        return codeSnippetService.findAll();
    }

    @Operation(summary = "Kimliğe göre tek kod parçası getir")
    @GetMapping("/{snippetId}")
    public CodeSnippet getSnippet(@PathVariable int snippetId) {
        return codeSnippetService.findById(snippetId);
    }

    @Operation(summary = "Yeni kod parçası ekle (gövdedeki id yok sayılır)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CodeSnippet addSnippet(@Valid @RequestBody CodeSnippet theSnippet, Principal thePrincipal) {
        theSnippet.setId(0); // istemci id gönderse bile mevcut kayıt güncellenmez
        return codeSnippetService.save(theSnippet, thePrincipal.getName());
    }

    @Operation(summary = "Kod parçasını tamamen güncelle")
    @PutMapping("/{snippetId}")
    public CodeSnippet updateSnippet(@PathVariable int snippetId, @Valid @RequestBody CodeSnippet theSnippet) {
        return codeSnippetService.update(snippetId, theSnippet);
    }

    @Operation(summary = "Kod parçasını kısmen güncelle (sadece gönderilen alanlar, id değiştirilemez)")
    @PatchMapping("/{snippetId}")
    public CodeSnippet patchSnippet(@PathVariable int snippetId, @RequestBody Map<String, Object> thePatchPayload) {
        return codeSnippetService.patch(snippetId, thePatchPayload);
    }

    @Operation(summary = "Kod parçasını (ve analiz geçmişini) sil")
    @DeleteMapping("/{snippetId}")
    public Map<String, String> deleteSnippet(@PathVariable int snippetId) {
        codeSnippetService.deleteById(snippetId);
        return Map.of("message", "Deleted code snippet id - " + snippetId);
    }

    @Operation(summary = "Kod parçasını analiz et: kod kokuları, refactoring tekniği, Big-O tahmini")
    @PostMapping("/{snippetId}/analyses")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisResult analyzeSnippet(@PathVariable int snippetId) {
        return analysisService.analyzeSnippet(snippetId);
    }

    @Operation(summary = "Kod parçasının analiz (revizyon) geçmişi")
    @GetMapping("/{snippetId}/analyses")
    public List<AnalysisResult> getAnalyses(@PathVariable int snippetId) {
        return analysisService.findBySnippetId(snippetId);
    }
}
