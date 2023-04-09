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

    List<RepoExampleMetrics> findAll();

    @Query(value = "SELECT m FROM RepoExampleMetrics m " +
            "WHERE m.repoExample.microservice =:microservice " +
            "AND m.repoExample.microserviceSet =:microserviceSet " +
            " ORDER BY  m.size")
    List<RepoExampleMetrics> findAllByMsAndMsSetOrderBySize(boolean microservice, boolean microserviceSet);

    @Query(value = "SELECT m FROM RepoExampleMetrics m " +
            "WHERE m.repoExample.microservice =:microservice " +
            "AND m.repoExample.microserviceSet =:microserviceSet " +
            " ORDER BY  m.files")
    List<RepoExampleMetrics> findAllByMsAndMsSetOrderByFilesAsc(boolean microservice, boolean microserviceSet);

    @Query(value = "SELECT m FROM RepoExampleMetrics m " +
            "WHERE m.repoExample.microservice =:microservice " +
            "AND m.repoExample.microserviceSet =:microserviceSet " +
            " ORDER BY  m.allContentsNumber")
    List<RepoExampleMetrics> findAllByMsAndMsSetOrderByAllContentsNumberAsc(boolean microservice, boolean microserviceSet);

    Page<RepoExampleMetrics> findByRepoExampleUrlContainingIgnoreCase(String keyword, Pageable paging);


    @Query(value = "SELECT m FROM RepoExampleMetrics m " +
            "WHERE m.databaseConnection = false " +
            "AND m.dockerfile = false " +
            "AND m.logsService = false " +
            "AND m.messaging = false " +
            "AND m.microserviceMention = false " +
            "AND m.restful = false " +
            "AND m.soap = false ")
    List<RepoExampleMetrics> findAllMissProcessedMetrics();

    long countAllByRepoExampleMicroservice(boolean microservice);

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

    List<RepoExampleMetrics> findAllByRepoExampleId(Long id);
}
