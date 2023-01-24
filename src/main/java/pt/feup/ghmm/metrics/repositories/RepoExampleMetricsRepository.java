package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;

@Repository
public interface RepoExampleMetricsRepository extends CrudRepository<RepoExampleMetrics, Long>, PagingAndSortingRepository<RepoExampleMetrics, Long> {
    Page<RepoExampleMetrics> findByRepoExampleUrlContainingIgnoreCase(String keyword, Pageable paging);

    @Query(value = "SELECT max(files) FROM RepoExampleMetrics")
    long findMaxRepoFiles();

    @Query(value = "SELECT min(files) FROM RepoExampleMetrics")
    long findMinRepoFiles();

    @Query(value = "SELECT avg(files) FROM RepoExampleMetrics")
    long findAverageRepoFiles();

    @Query(value = "SELECT max(allContentsNumber) FROM RepoExampleMetrics")
    long findMaxRepoAllContentsNumber();

    @Query(value = "SELECT min(allContentsNumber) FROM RepoExampleMetrics")
    long findMinRepoAllContentsNumber();

    @Query(value = "SELECT avg(allContentsNumber) FROM RepoExampleMetrics")
    long findAverageRepoAllContentsNumber();

    @Query(value = "SELECT max(metric.size) FROM RepoExampleMetrics as metric")
    long findMaxSize();

    @Query(value = "SELECT min(metric.size) FROM RepoExampleMetrics as metric")
    long findMinSize();

    @Query(value = "SELECT avg(metric.size) FROM RepoExampleMetrics as metric")
    long findAverageSize();

    long countByMicroserviceMentionTrue();

    long countByMicroserviceMentionFalse();

    long countByDatabaseConnectionTrue();

    long countByDatabaseConnectionFalse();

    long countByDockerfileTrue();

    long countByDockerfileFalse();

    long countByRestfulTrue();

    long countByRestfulFalse();

    long countByMessagingTrue();

    long countByMessagingFalse();

    long countBySoapTrue();

    long countBySoapFalse();

    long countByLogsServiceTrue();

    long countByLogsServiceFalse();
}
