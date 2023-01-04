package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.RepoExample;

@Repository
@RepositoryRestResource(collectionResourceRel = "examples", path = "examples")
public interface RepoExampleRepository extends CrudRepository<RepoExample, Long>, PagingAndSortingRepository<RepoExample, Long> {
}
