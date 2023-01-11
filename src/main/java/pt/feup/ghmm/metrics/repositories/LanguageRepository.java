package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.Language;

@Repository
public interface LanguageRepository extends CrudRepository<Language, Long> {
}
