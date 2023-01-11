package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;

@Repository
public interface RepoExampleMetricsRepository extends CrudRepository<RepoExampleMetrics, Long>, PagingAndSortingRepository<RepoExampleMetrics, Long> {
}
