package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoMined;
import pt.feup.ghmm.metrics.models.RepoExample;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "repomined", path = "repomined")
public interface RepoMinedRepository extends CrudRepository<RepoMined, Long>, PagingAndSortingRepository<RepoMined, Long> {
    Page<RepoMined> findByUrlContainingIgnoreCase(String url, Pageable pageable);
    List<RepoMined> findByProcessedFalse();
    long countAllByProcessedTrueAndProcessingErrorFalse();
    long countAllByProcessedTrueAndProcessingErrorTrue();
    long countAllByProcessedFalse();
}
