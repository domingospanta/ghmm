package pt.feup.ghmm.metrics.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.repositories.LanguageRepository;

import java.util.List;

@Service
public class LanguageService {

    private final LanguageRepository repository;

    private final List<String> programmingLanguages;

    public LanguageService(LanguageRepository repository, @Value("${programming.languages}") String programmingLanguages) {
        this.repository = repository;
        this.programmingLanguages = List.of(programmingLanguages.split(","));
    }

    public List<String> getProgrammingLanguages(){
        return programmingLanguages;
    }

    public Language findOrCreateByName(String name) {
        if(name == null){
            name = "undefined";
        }
        Language language = repository.findByName(name);
        if(language != null) return language;

        language = Language.builder()
                    .name(name)
                    .programmingLanguage(programmingLanguages.contains(name))
                    .build();
        repository.save(language);

        return language;
    }
}
