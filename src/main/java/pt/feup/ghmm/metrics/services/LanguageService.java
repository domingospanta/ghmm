package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.RepoExampleDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.repositories.LanguageRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class LanguageService {
    private LanguageRepository repository;

    public Language findOrCreateByName(String name) {
        Language language = repository.findByName(name);
        if(language != null) return language;

        language = Language.builder()
                    .name(name)
                    .build();
        repository.save(language);

        return language;
    }
}
