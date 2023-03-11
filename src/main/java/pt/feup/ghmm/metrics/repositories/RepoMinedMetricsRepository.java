package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoMinedMetrics;
import pt.feup.ghmm.metrics.models.RepoMinedMetrics;

import java.util.List;

@Repository
public interface RepoMinedMetricsRepository extends CrudRepository<RepoMinedMetrics, Long>, PagingAndSortingRepository<RepoMinedMetrics, Long> {

    List<RepoMinedMetrics> findAll();

    Page<RepoMinedMetrics> findByRepoMinedUrlContainingIgnoreCase(String keyword, Pageable paging);

    @Query(value = "SELECT m FROM RepoMinedMetrics m " +
            "WHERE m.databaseConnection = false " +
            "AND m.dockerfile = false " +
            "AND m.logsService = false " +
            "AND m.messaging = false " +
            "AND m.microserviceMention = false " +
            "AND m.restful = false " +
            "AND m.soap = false ")
    List<RepoMinedMetrics> findAllMissProcessedMetrics();

    @Query(value = "SELECT max(files) FROM RepoMinedMetrics")
    long findMaxRepoFiles();

    @Query(value = "SELECT min(files) FROM RepoMinedMetrics")
    long findMinRepoFiles();

    @Query(value = "SELECT avg(files) FROM RepoMinedMetrics")
    long findAverageRepoFiles();

    @Query(value = "SELECT max(allContentsNumber) FROM RepoMinedMetrics")
    long findMaxRepoAllContentsNumber();

    @Query(value = "SELECT min(allContentsNumber) FROM RepoMinedMetrics")
    long findMinRepoAllContentsNumber();

    @Query(value = "SELECT avg(allContentsNumber) FROM RepoMinedMetrics")
    long findAverageRepoAllContentsNumber();

    @Query(value = "SELECT max(metric.size) FROM RepoMinedMetrics as metric")
    long findMaxSize();

    @Query(value = "SELECT min(metric.size) FROM RepoMinedMetrics as metric")
    long findMinSize();

    @Query(value = "SELECT avg(metric.size) FROM RepoMinedMetrics as metric")
    long findAverageSize();


    List<RepoMinedMetrics> findAllByRepoMinedId(Long id);
}
