package pt.feup.ghmm.identification.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.identification.models.Service;

@Repository
public interface ServiceRepository extends CrudRepository<Service, Long> {
    Service findByName(String name);
}
