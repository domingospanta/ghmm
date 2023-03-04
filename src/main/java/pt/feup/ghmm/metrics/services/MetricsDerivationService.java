package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.models.Threshold;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;

import java.util.*;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

    private RepoExampleMetricsRepository repository;

    private ThresholdService thresholdService;

    private RepoExampleRepository repoExampleRepository;

    private final String SIZE = "size";
    private final String FILES = "files";
    private final String ALL_CONTENTS = "allContents";

    public void runMetricsDerivation() {
        runMetricsDerivation(true, true);
        runMetricsDerivation(true, false);
        runMetricsDerivation(false, false);
        calculateRepoMetricsScores();
    }

    private void runMetricsDerivation(boolean microservice, boolean microserviceSet) {
        List<RepoExampleMetrics> metrics = repository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderBySizeAsc(microservice, microserviceSet);
        double sizeTotal = metrics.stream().map(RepoExampleMetrics::getSize).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, SIZE, sizeTotal);
        runWeightRatioAggregation(metrics, SIZE);
        runThresholdDerivation(metrics, SIZE, microservice, microserviceSet);

        metrics = repository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderByFilesAsc(microservice, microserviceSet);
        double filesTotal = metrics.stream().map(RepoExampleMetrics::getFiles).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, FILES, filesTotal);
        runWeightRatioAggregation(metrics, FILES);
        runThresholdDerivation(metrics, FILES, microservice, microserviceSet);

        metrics = repository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderByAllContentsNumberAsc(microservice, microserviceSet);
        double filesAllContents = metrics.stream().map(RepoExampleMetrics::getAllContentsNumber).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, ALL_CONTENTS, filesAllContents);
        runWeightRatioAggregation(metrics, ALL_CONTENTS);
        runThresholdDerivation(metrics, ALL_CONTENTS, microservice, microserviceSet);
    }

    private void runSystemAggregationForWeightNormalization(List<RepoExampleMetrics> metrics, String attribute, double sum) {
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE:
                    double sizeWeight = metric.getSize() / sum;
                    metric.setSizeWeight(sizeWeight);
                case FILES:
                    double filesWeight = metric.getFiles() / sum;
                    metric.setFilesWeight(filesWeight);
                case ALL_CONTENTS:
                    double allContentsWeight = metric.getAllContentsNumber() / sum;
                    metric.setAllContentsNumberWeight(allContentsWeight);
            }
        }
        repository.saveAll(metrics);
    }

    private void runWeightRatioAggregation(List<RepoExampleMetrics> metrics, String attribute) {
        double aggregatedWeight = 0;
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE:
                    aggregatedWeight += metric.getSizeWeight();
                    metric.setSizeAggregatedWeight(aggregatedWeight);
                case FILES:
                    aggregatedWeight += metric.getFilesWeight();
                    metric.setFilesAggregatedWeight(aggregatedWeight);
                case ALL_CONTENTS:
                    aggregatedWeight += metric.getAllContentsNumberWeight();
                    metric.setAllContentsAggregatedNumberWeight(aggregatedWeight);
            }
        }
        repository.saveAll(metrics);
    }

    private void runThresholdDerivation(List<RepoExampleMetrics> metrics, String attribute, boolean microservice, boolean microserviceSet) {
        Threshold threshold = thresholdService.findOrCreateByMetric(attribute, microservice, microserviceSet);
        for(RepoExampleMetrics metric: metrics){
            switch (attribute) {
                case SIZE:
                    if(isLowRiskClassification(metric.getSizeAggregatedWeight())){
                        threshold.setThresholdValue(metric.getSize());
                }
                case FILES:
                    if(isLowRiskClassification(metric.getFilesAggregatedWeight())){
                        threshold.setThresholdValue(metric.getFiles());
                    }
                case ALL_CONTENTS:
                    if(isLowRiskClassification(metric.getAllContentsAggregatedNumberWeight())){
                        threshold.setThresholdValue(metric.getAllContentsNumber());
                    }
            }
        }
        thresholdService.update(threshold);
    }

    private boolean isLowRiskClassification(double value) {
        float MAX_LOW_RISK_CLASSIFICATION = 0.7f;
        return value  < MAX_LOW_RISK_CLASSIFICATION;
    }

    private void calculateRepoMetricsScores() {
        List<RepoExampleMetrics> repoExampleMetricsList = repository.findAll();
        for(RepoExampleMetrics metrics: repoExampleMetricsList){
            RepoExample repoExample = metrics.getRepoExample();
            double score = calculateScore(metrics);
            repoExample.setScore(score);
            if (score > 7.0){
                if(metrics.getProgrammingLanguages() > 1 && metrics.getDatabaseServices() > 1){
                    repoExample.setClassification("MICROSERVICE_SET");
                } else if(metrics.getProgrammingLanguages() > 1 && hasFrontendLanguages(metrics.getLanguages())){
                    repoExample.setScore(repoExample.getScore() - 1);
                    if(repoExample.getScore() > 7.0) {
                        repoExample.setClassification("MICROSERVICE");
                    } else {
                        repoExample.setClassification("MONOLITH");
                    }
                }
                else {
                    repoExample.setClassification("MICROSERVICE");
                }
            } else {
                repoExample.setClassification("MONOLITH");
            }
            repoExampleRepository.save(repoExample);
        }
    }

    private boolean hasFrontendLanguages(Set<Language> languages) {
        for (Language language: languages){
            if("HTML".equalsIgnoreCase(language.getName()) || "CSS".equalsIgnoreCase(language.getName())){
                return true;
            }
        }
        return false;
    }

    private double calculateScore(RepoExampleMetrics repoExampleMetrics) {
        double score = 0;
        boolean msSet = repoExampleMetrics.getRepoExample().isMicroserviceSet();
        score += getNumericalScore(repoExampleMetrics.getSize(), thresholdService.findByMetric(SIZE, msSet).getThresholdValue());
        score += getNumericalScore(repoExampleMetrics.getFiles(), thresholdService.findByMetric(FILES, msSet).getThresholdValue());
        score += getNumericalScore(repoExampleMetrics.getAllContentsNumber(), thresholdService.findByMetric(ALL_CONTENTS, msSet).getThresholdValue());
        score += getBooleanScore(repoExampleMetrics.isDockerfile());
        score += getBooleanScore(repoExampleMetrics.isLogsService());
        score += getBooleanScore(repoExampleMetrics.isDatabaseConnection());
        score += getBooleanScore(repoExampleMetrics.isMessaging());
        score += getBooleanScore(repoExampleMetrics.isRestful());
        score += getBooleanScore(repoExampleMetrics.isMicroserviceMention());
        score += getBooleanScore(!repoExampleMetrics.isSoap());
        return score;
    }

    private double getBooleanScore(boolean metric) {
        return metric ? 1 : 0;
    }

    private double getNumericalScore(long metric, double thresholdValue) {
        return metric <= thresholdValue ? 1 : 0;
    }
}
