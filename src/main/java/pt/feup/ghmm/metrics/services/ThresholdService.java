package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.Threshold;
import pt.feup.ghmm.metrics.repositories.LanguageRepository;
import pt.feup.ghmm.metrics.repositories.ThresholdRepository;

@AllArgsConstructor
@Service
public class ThresholdService {
    private ThresholdRepository repository;

    public Threshold findOrCreateByMetric(String metric, boolean microservice) {
        if(metric == null){
            return null;
        }
        Threshold threshold = repository.findByMetricAndMicroservice(metric, microservice);
        if(threshold != null) return threshold;

        threshold = Threshold.builder()
                    .metric(metric)
                    .microservice(microservice)
                    .build();
        repository.save(threshold);
        return threshold;
    }

    public void update(Threshold threshold){
        repository.save(threshold);
    }
}
