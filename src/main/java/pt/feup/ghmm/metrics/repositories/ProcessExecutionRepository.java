package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.ProcessExecution;

@Repository
public interface ProcessExecutionRepository extends CrudRepository<ProcessExecution, Long> {
}
