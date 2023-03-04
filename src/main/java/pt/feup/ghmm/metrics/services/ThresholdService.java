package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.Threshold;
import pt.feup.ghmm.metrics.repositories.ThresholdRepository;

@AllArgsConstructor
@Service
public class ThresholdService {
    private ThresholdRepository repository;

    public Threshold findOrCreateByMetric(String metric, boolean microservice, boolean microserviceSet) {
        if(metric == null){
            return null;
        }
        Threshold threshold = repository.findByMetricAndMicroserviceAndMicroserviceSet(metric, microservice, microserviceSet);
        if(threshold != null) return threshold;

        threshold = Threshold.builder()
                    .metric(metric)
                    .microservice(microservice)
                    .microserviceSet(microserviceSet)
                    .build();
        repository.save(threshold);
        return threshold;
    }

    public Threshold findByMetric(String metric, boolean microserviceSet){
        return repository.findByMetricAndMicroserviceAndMicroserviceSet(metric, true, microserviceSet);
    }

    public void update(Threshold threshold){
        repository.save(threshold);
    }
}
