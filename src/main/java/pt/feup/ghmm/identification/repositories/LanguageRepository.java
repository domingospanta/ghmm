package pt.feup.ghmm.identification.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.identification.models.Language;

@Repository
public interface LanguageRepository extends CrudRepository<Language, Long> {
    Language findByName(String name);
}
