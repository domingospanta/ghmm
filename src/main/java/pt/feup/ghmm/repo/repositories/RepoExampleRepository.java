package pt.feup.ghmm.repo.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.repo.models.RepoExample;

import java.util.List;

@Repository
@RepositoryRestResource(collectionResourceRel = "examples", path = "examples")
public interface RepoExampleRepository extends CrudRepository<RepoExample, Long>, PagingAndSortingRepository<RepoExample, Long> {
    Page<RepoExample> findByUrlContainingIgnoreCase(String url, Pageable pageable);
    List<RepoExample> findByProcessedFalse();
    long countAllByProcessedTrueAndProcessingErrorFalse();
    long countAllByProcessedTrueAndProcessingErrorTrue();
    long countAllByProcessedFalse();
}