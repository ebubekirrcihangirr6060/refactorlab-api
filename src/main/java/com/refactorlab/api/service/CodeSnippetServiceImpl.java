package com.refactorlab.api.service;

import com.refactorlab.api.dao.AnalysisResultDAO;
import com.refactorlab.api.dao.CodeSnippetDAO;
import com.refactorlab.api.entity.CodeSnippet;
import com.refactorlab.api.rest.CodeSnippetNotFoundException;
import com.refactorlab.api.rest.InvalidRequestException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CodeSnippetServiceImpl implements CodeSnippetService {

    // Kod içine gömülü istem enjeksiyonu (prompt injection) kalıpları; \b: "contact as" gibi normal kod yakalanmaz
    private static final Pattern PROMPT_INJECTION = Pattern.compile(
            "\\b(ignore (all )?previous instructions|disregard the above|you are now an? |system prompt|act as an? )\\b",
            Pattern.CASE_INSENSITIVE);

    private final CodeSnippetDAO codeSnippetDAO;
    private final AnalysisResultDAO analysisResultDAO;
    private final JsonMapper jsonMapper;
    private final Validator validator;
    private final int maxCodeLength;

    // Tüm bağımlılıklar constructor ile; özel ayar @Value ile constructor parametresine alınır
    public CodeSnippetServiceImpl(CodeSnippetDAO theCodeSnippetDAO,
                                  AnalysisResultDAO theAnalysisResultDAO,
                                  JsonMapper theJsonMapper,
                                  Validator theValidator,
                                  @Value("${refactorlab.analysis.max-code-length}") int theMaxCodeLength) {
        codeSnippetDAO = theCodeSnippetDAO;
        analysisResultDAO = theAnalysisResultDAO;
        jsonMapper = theJsonMapper;
        validator = theValidator;
        maxCodeLength = theMaxCodeLength;
    }

    @Override
    public List<CodeSnippet> findAll() {
        return codeSnippetDAO.findAll();
    }

    @Override
    public List<CodeSnippet> findByLanguage(String theLanguage) {
        return codeSnippetDAO.findByLanguage(theLanguage.toUpperCase(Locale.ROOT));
    }

    @Override
    public CodeSnippet findById(int theId) {
        CodeSnippet theSnippet = codeSnippetDAO.findById(theId);
        if (theSnippet == null) {
            throw new CodeSnippetNotFoundException("Code snippet id not found - " + theId);
        }
        return theSnippet;
    }

    @Override
    @Transactional
    public CodeSnippet save(CodeSnippet theSnippet, String theOwnerUsername) {
        validateSourceCode(theSnippet.getSourceCode());
        theSnippet.setOwnerUsername(theOwnerUsername);
        theSnippet.setCreatedAt(LocalDateTime.now());
        codeSnippetDAO.save(theSnippet);           // persist -> üretilen id nesneye yazılır
        return theSnippet;
    }

    // PUT: tam güncelleme (tüm alanlar gövdeden gelir)
    @Override
    @Transactional
    public CodeSnippet update(int theId, CodeSnippet theSnippet) {
        CodeSnippet theDbSnippet = findById(theId);  // yoksa 404
        validateSourceCode(theSnippet.getSourceCode());
        theSnippet.setId(theId);                     // id URL'den gelir
        theSnippet.setOwnerUsername(theDbSnippet.getOwnerUsername());
        theSnippet.setCreatedAt(theDbSnippet.getCreatedAt());
        return codeSnippetDAO.update(theSnippet);
    }

    // PATCH: sadece gönderilen alanlar değişir, diğerleri korunur
    @Override
    @Transactional
    public CodeSnippet patch(int theId, Map<String, Object> thePatchPayload) {
        if (thePatchPayload.containsKey("id")) {
            throw new InvalidRequestException("Code snippet id not allowed in request body - " + theId);
        }
        if (thePatchPayload.containsKey("ownerUsername") || thePatchPayload.containsKey("createdAt")) {
            throw new InvalidRequestException("ownerUsername and createdAt cannot be changed");
        }
        CodeSnippet theDbSnippet = findById(theId);
        CodeSnippet thePatchedSnippet = jsonMapper.updateValue(theDbSnippet, thePatchPayload);
        validateEntity(thePatchedSnippet);
        validateSourceCode(thePatchedSnippet.getSourceCode());
        return codeSnippetDAO.update(thePatchedSnippet);
    }

    // Önce bağlı analizler, sonra snippet silinir: ikisi tek transaction içinde
    @Override
    @Transactional
    public void deleteById(int theId) {
        findById(theId);  // yoksa 404
        analysisResultDAO.deleteBySnippetId(theId);
        codeSnippetDAO.deleteById(theId);
    }

    // İş kuralı: kod boyutu sınırı (@Value ayarı) ve prompt injection kontrolü
    private void validateSourceCode(String theSourceCode) {
        if (theSourceCode == null) {
            return;
        }
        if (theSourceCode.length() > maxCodeLength) {
            throw new InvalidRequestException("sourceCode is too long - max " + maxCodeLength + " characters");
        }
        Matcher theMatcher = PROMPT_INJECTION.matcher(theSourceCode);
        if (theMatcher.find()) {
            throw new InvalidRequestException("sourceCode contains a suspicious instruction: '" + theMatcher.group().trim() + "'");
        }
    }

    // PATCH sonrası Bean Validation kurallarını elle uygular
    private void validateEntity(CodeSnippet theSnippet) {
        Set<ConstraintViolation<CodeSnippet>> theViolations = validator.validate(theSnippet);
        if (!theViolations.isEmpty()) {
            String theMessage = theViolations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new InvalidRequestException("Validation failed - " + theMessage);
        }
    }
}
