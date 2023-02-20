package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.Service;

@Repository
public interface ServiceRepository extends CrudRepository<Service, Long> {
    Service findByName(String name);
}
