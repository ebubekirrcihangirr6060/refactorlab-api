package com.refactorlab.api.rest;

import com.refactorlab.api.entity.RefactoringRule;
import com.refactorlab.api.service.RefactoringRuleService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@Tag(name = "Refactoring Rules", description = "Kurumsal refactoring kuralları (yazma işlemleri yalnızca REVIEWER)")
public class RefactoringRuleRestController {

    private final RefactoringRuleService refactoringRuleService;

    public RefactoringRuleRestController(RefactoringRuleService theRefactoringRuleService) {
        refactoringRuleService = theRefactoringRuleService;
    }

    @Operation(summary = "Tüm kuralları listele (opsiyonel ?category=Bloaters filtresi)")
    @GetMapping
    public List<RefactoringRule> findAll(@RequestParam(required = false) String category) {
        if (category != null && !category.isBlank()) {
            return refactoringRuleService.findByCategory(category);
        }
        return refactoringRuleService.findAll();
    }

    @Operation(summary = "Kimliğe göre tek kural getir")
    @GetMapping("/{ruleId}")
    public RefactoringRule getRule(@PathVariable int ruleId) {
        return refactoringRuleService.findById(ruleId);
    }

    @Operation(summary = "Yeni kural ekle")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RefactoringRule addRule(@Valid @RequestBody RefactoringRule theRule) {
        theRule.setId(0);
        return refactoringRuleService.save(theRule);
    }

    @Operation(summary = "Kuralı tamamen güncelle")
    @PutMapping("/{ruleId}")
    public RefactoringRule updateRule(@PathVariable int ruleId, @Valid @RequestBody RefactoringRule theRule) {
        return refactoringRuleService.update(ruleId, theRule);
    }

    @Operation(summary = "Kuralı kısmen güncelle")
    @PatchMapping("/{ruleId}")
    public RefactoringRule patchRule(@PathVariable int ruleId, @RequestBody Map<String, Object> thePatchPayload) {
        return refactoringRuleService.patch(ruleId, thePatchPayload);
    }

    @Operation(summary = "Kuralı sil")
    @DeleteMapping("/{ruleId}")
    public Map<String, String> deleteRule(@PathVariable int ruleId) {
        refactoringRuleService.deleteById(ruleId);
        return Map.of("message", "Deleted refactoring rule id - " + ruleId);
    }
}
