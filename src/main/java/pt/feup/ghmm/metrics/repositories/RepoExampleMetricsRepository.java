package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;

import java.util.List;

@Repository
public interface RepoExampleMetricsRepository extends CrudRepository<RepoExampleMetrics, Long>, PagingAndSortingRepository<RepoExampleMetrics, Long> {

    List<RepoExampleMetrics> findAllByRepoExampleMicroserviceOrderBySizeAsc(boolean microservice);

    Page<RepoExampleMetrics> findByRepoExampleUrlContainingIgnoreCase(String keyword, Pageable paging);

    long countAllByRepoExampleMicroservice(boolean microservice);

    @Query(value = "SELECT max(files) FROM RepoExampleMetrics")
    long findMaxRepoFiles();

    @Query(value = "SELECT min(files) FROM RepoExampleMetrics")
    long findMinRepoFiles();

    @Query(value = "SELECT avg(files) FROM RepoExampleMetrics")
    long findAverageRepoFiles();

    @Query(value = "SELECT sum(files) FROM RepoExampleMetrics" +
            " WHERE RepoExampleMetrics.repoExample.microservice =: microservice")
    long findSumRepoFiles( boolean microservice);

    @Query(value = "SELECT max(allContentsNumber) FROM RepoExampleMetrics")
    long findMaxRepoAllContentsNumber();

    @Query(value = "SELECT min(allContentsNumber) FROM RepoExampleMetrics")
    long findMinRepoAllContentsNumber();

    @Query(value = "SELECT avg(allContentsNumber) FROM RepoExampleMetrics")
    long findAverageRepoAllContentsNumber();

    @Query(value = "SELECT sum(allContentsNumber) FROM RepoExampleMetrics" +
            " WHERE RepoExampleMetrics.repoExample.microservice =: microservice")
    long findSumRepoAllContentsNumber(boolean microservice);

    @Query(value = "SELECT max(metric.size) FROM RepoExampleMetrics as metric")
    long findMaxSize();

    @Query(value = "SELECT min(metric.size) FROM RepoExampleMetrics as metric")
    long findMinSize();

    @Query(value = "SELECT avg(metric.size) FROM RepoExampleMetrics as metric")
    long findAverageSize();

    @Query(value = "SELECT sum(metric.size) FROM RepoExampleMetrics as metric" +
            " WHERE RepoExampleMetrics.repoExample.microservice =: microservice")
    long findSumSize(boolean microservice);

    long countByMicroserviceMentionTrueAndRepoExampleMicroservice(boolean microservice);

    long countByMicroserviceMentionFalseAndRepoExampleMicroservice(boolean microservice);

    long countByDatabaseConnectionTrueAndRepoExampleMicroservice(boolean microservice);

    long countByDatabaseConnectionFalseAndRepoExampleMicroservice(boolean microservice);

    long countByDockerfileTrueAndRepoExampleMicroservice(boolean microservice);

    long countByDockerfileFalseAndRepoExampleMicroservice(boolean microservice);

    long countByRestfulTrueAndRepoExampleMicroservice(boolean microservice);

    long countByRestfulFalseAndRepoExampleMicroservice(boolean microservice);

    long countByMessagingTrueAndRepoExampleMicroservice(boolean microservice);

    long countByMessagingFalseAndRepoExampleMicroservice(boolean microservice);

    long countBySoapTrueAndRepoExampleMicroservice(boolean microservice);

    long countBySoapFalseAndRepoExampleMicroservice(boolean microservice);

    long countByLogsServiceTrueAndRepoExampleMicroservice(boolean microservice);

    long countByLogsServiceFalseAndRepoExampleMicroservice(boolean microservice);
}
