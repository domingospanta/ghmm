package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.models.Threshold;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.*;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

    private RepoExampleMetricsRepository repository;

    private ThresholdService thresholdService;

    private final String SIZE = "size";
    private final String FILES = "files";
    private final String ALL_CONTENTS = "allContents";

    public void runMetricsDerivation() {
        runMetricsDerivation(true);
        runMetricsDerivation(false);
    }

    private void runMetricsDerivation(boolean microservice) {
        List<RepoExampleMetrics> metrics = repository.findAllByRepoExampleMicroserviceOrderBySizeAsc(microservice);
        double sizeTotal = metrics.stream().map(RepoExampleMetrics::getSize).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, SIZE, sizeTotal);
        runWeightRatioAggregation(metrics, SIZE);
        runThresholdDerivation(metrics, SIZE, microservice);

        metrics = repository.findAllByRepoExampleMicroserviceOrderByFilesAsc(microservice);
        double filesTotal = metrics.stream().map(RepoExampleMetrics::getFiles).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, FILES, filesTotal);
        runWeightRatioAggregation(metrics, FILES);
        runThresholdDerivation(metrics, FILES, microservice);

        metrics = repository.findAllByRepoExampleMicroserviceOrderByAllContentsNumberAsc(microservice);
        double filesAllContents = metrics.stream().map(RepoExampleMetrics::getAllContentsNumber).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, ALL_CONTENTS, filesAllContents);
        runWeightRatioAggregation(metrics, ALL_CONTENTS);
        runThresholdDerivation(metrics, ALL_CONTENTS, microservice);
    }

    private void runSystemAggregationForWeightNormalization(List<RepoExampleMetrics> metrics, String attribute, double sum) {
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE -> {
                    double sizeWeight = metric.getSize() / sum;
                    metric.setSizeWeight(sizeWeight);
                }
                case FILES -> {
                    double filesWeight = metric.getFiles() / sum;
                    metric.setFilesWeight(filesWeight);
                }
                case ALL_CONTENTS -> {
                    double allContentsWeight = metric.getAllContentsNumber() / sum;
                    metric.setAllContentsNumberWeight(allContentsWeight);
                }
            }
        }
        repository.saveAll(metrics);
    }

    private void runWeightRatioAggregation(List<RepoExampleMetrics> metrics, String attribute) {
        double aggregatedWeight = 0;
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE -> {
                    aggregatedWeight += metric.getSizeWeight();
                    metric.setSizeAggregatedWeight(aggregatedWeight);
                }
                case FILES -> {
                    aggregatedWeight += metric.getFilesWeight();
                    metric.setFilesAggregatedWeight(aggregatedWeight);
                }
                case ALL_CONTENTS -> {
                    aggregatedWeight += metric.getAllContentsNumberWeight();
                    metric.setAllContentsAggregatedNumberWeight(aggregatedWeight);
                }
            }
        }
        repository.saveAll(metrics);
    }

    private void runThresholdDerivation(List<RepoExampleMetrics> metrics, String attribute, boolean microservice) {
        Threshold threshold = thresholdService.findOrCreateByMetric(attribute, microservice);
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE -> {
                    if(isLowRiskClassification(metric.getSizeAggregatedWeight())){
                        threshold.setThresholdValue(metric.getSizeAggregatedWeight());
                    }
                }
                case FILES -> {
                    if(isLowRiskClassification(metric.getFilesAggregatedWeight())){
                        threshold.setThresholdValue(metric.getFilesAggregatedWeight());
                    }
                }
                case ALL_CONTENTS -> {
                    if(isLowRiskClassification(metric.getAllContentsAggregatedNumberWeight())){
                        threshold.setThresholdValue(metric.getAllContentsAggregatedNumberWeight());
                    }
                }
            }
        }
        thresholdService.update(threshold);
    }

    private boolean isLowRiskClassification(double value) {
        float MAX_LOW_RISK_CLASSIFICATION = 0.7f;
        return value  < MAX_LOW_RISK_CLASSIFICATION;
    }
}
