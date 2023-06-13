package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.identification.models.Threshold;
import pt.feup.ghmm.identification.services.ThresholdService;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedMetricsRepository;
import pt.feup.ghmm.repo.services.CodeRepoService;

import java.util.List;

import static pt.feup.ghmm.integration.utils.Constants.*;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDerivationService.class);

    private RepoExampleMetricsRepository repoExampleMetricsRepository;

    private RepoMinedMetricsRepository repoMinedMetricsRepository;

    private ThresholdService thresholdService;

    private CodeRepoService codeRepoService;

    public void runMetricsDerivationForRepoExamples() {
        runMetricsDerivationForRepoExamples(true, true);
        runMetricsDerivationForRepoExamples(true, false);
        runMetricsDerivationForRepoExamples(false, false);
    }

    private void runMetricsDerivationForRepoExamples(boolean microservice, boolean microserviceSet) {
        List<RepoExampleMetrics> metrics = repoExampleMetricsRepository.findAllByMsAndMsSetOrderBySize(microservice, microserviceSet);
        double sizeTotal = metrics.stream().map(RepoExampleMetrics::getSize).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, SIZE, sizeTotal);
        runWeightRatioAggregation(metrics, SIZE);
        runThresholdDerivation(metrics, SIZE, microservice, microserviceSet);

        metrics = repoExampleMetricsRepository.findAllByMsAndMsSetOrderByFilesAsc(microservice, microserviceSet);
        double filesTotal = metrics.stream().map(RepoExampleMetrics::getFiles).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, FILES, filesTotal);
        runWeightRatioAggregation(metrics, FILES);
        runThresholdDerivation(metrics, FILES, microservice, microserviceSet);

        metrics = repoExampleMetricsRepository.findAllByMsAndMsSetOrderByAllContentsNumberAsc(microservice, microserviceSet);
        double filesAllContents = metrics.stream().map(RepoExampleMetrics::getAllContentsNumber).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, ALL_CONTENTS, filesAllContents);
        runWeightRatioAggregation(metrics, ALL_CONTENTS);
        runThresholdDerivation(metrics, ALL_CONTENTS, microservice, microserviceSet);
    }

    private void runSystemAggregationForWeightNormalization(List<RepoExampleMetrics> metrics, String attribute, double sum) {
    logger.info("runSystemAggregationForWeightNormalization for " + attribute);
    for(RepoExampleMetrics metric: metrics){
            logger.info("Repo id: " + metric.getRepoExample().getId());
            switch (attribute) {
                case SIZE:
                    double sizeWeight = metric.getSize() / sum;
                    metric.setSizeWeight(sizeWeight);
                    break;
                case FILES:
                    double filesWeight = metric.getFiles() / sum;
                    metric.setFilesWeight(filesWeight);
                    break;
                case ALL_CONTENTS:
                    double allContentsWeight = metric.getAllContentsNumber() / sum;
                    metric.setAllContentsNumberWeight(allContentsWeight);
                    break;
            }
        }
        repoExampleMetricsRepository.saveAll(metrics);
    }

    private void runWeightRatioAggregation(List<RepoExampleMetrics> metrics, String attribute) {
        double aggregatedWeight = 0;
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE:
                    aggregatedWeight += metric.getSizeWeight();
                    metric.setSizeAggregatedWeight(aggregatedWeight);
                    break;
                case FILES:
                    aggregatedWeight += metric.getFilesWeight();
                    metric.setFilesAggregatedWeight(aggregatedWeight);
                    break;
                case ALL_CONTENTS:
                    aggregatedWeight += metric.getAllContentsNumberWeight();
                    metric.setAllContentsAggregatedNumberWeight(aggregatedWeight);
                    break;
            }
        }
        repoExampleMetricsRepository.saveAll(metrics);
    }

    private void runThresholdDerivation(List<RepoExampleMetrics> metrics, String attribute, boolean microservice, boolean microserviceSet) {
        Threshold threshold = thresholdService.findOrCreateByMetric(attribute, microservice, microserviceSet);
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE:
                    if(isLowRiskClassification(metric.getSizeAggregatedWeight())){
                        threshold.setThresholdValue(metric.getSize());
                    }
                    break;
                case FILES:
                    if(isLowRiskClassification(metric.getFilesAggregatedWeight())){
                        threshold.setThresholdValue(metric.getFiles());
                    }
                    break;
                case ALL_CONTENTS:
                    if(isLowRiskClassification(metric.getAllContentsAggregatedNumberWeight())){
                        threshold.setThresholdValue(metric.getAllContentsNumber());
                    }
                break;
            }
        }
        thresholdService.update(threshold);
    }

    private boolean isLowRiskClassification(double value) {
        float MAX_LOW_RISK_CLASSIFICATION = 0.7f;
        return value  < MAX_LOW_RISK_CLASSIFICATION;
    }
}
