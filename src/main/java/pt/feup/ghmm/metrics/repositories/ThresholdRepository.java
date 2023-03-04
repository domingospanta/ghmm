package pt.feup.ghmm.metrics.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.Threshold;

import java.util.Optional;

@Repository
public interface ThresholdRepository extends CrudRepository<Threshold, Long> {

    Threshold findByMetricAndMicroservice(String metric, boolean microservice);

    Optional<Threshold> findByMetric(String metric);
}