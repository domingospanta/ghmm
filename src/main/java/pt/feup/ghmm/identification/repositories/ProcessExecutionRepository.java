package pt.feup.ghmm.identification.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.identification.models.ProcessExecution;

@Repository
public interface ProcessExecutionRepository extends CrudRepository<ProcessExecution, Long> {
}
