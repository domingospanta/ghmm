package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.*;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedMetricsRepository;

import java.util.*;

import static pt.feup.ghmm.core.utils.Constants.EXAMPLE_PROCESS_TYPE;
import static pt.feup.ghmm.core.utils.Constants.MS_CLASSIFICATION_SCORE;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

    private RepoExampleMetricsRepository repoExampleMetricsRepository;

    private RepoMinedMetricsRepository repoMinedMetricsRepository;

    private ThresholdService thresholdService;

    private CodeRepoService codeRepoService;

    private final String SIZE = "size";
    private final String FILES = "files";
    private final String ALL_CONTENTS = "allContents";

    public void runMetricsDerivationForRepoExamples() {
        runMetricsDerivationForRepoExamples(true, true);
        runMetricsDerivationForRepoExamples(true, false);
        runMetricsDerivationForRepoExamples(false, false);
    }

    private void runMetricsDerivationForRepoExamples(boolean microservice, boolean microserviceSet) {
        List<RepoExampleMetrics> metrics = repoExampleMetricsRepository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderBySizeAsc(microservice, microserviceSet);
        double sizeTotal = metrics.stream().map(RepoExampleMetrics::getSize).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, SIZE, sizeTotal);
        runWeightRatioAggregation(metrics, SIZE);
        runThresholdDerivation(metrics, SIZE, microservice, microserviceSet);

        metrics = repoExampleMetricsRepository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderByFilesAsc(microservice, microserviceSet);
        double filesTotal = metrics.stream().map(RepoExampleMetrics::getFiles).reduce(0L, Long::sum);
        runSystemAggregationForWeightNormalization(metrics, FILES, filesTotal);
        runWeightRatioAggregation(metrics, FILES);
        runThresholdDerivation(metrics, FILES, microservice, microserviceSet);

        metrics = repoExampleMetricsRepository.findAllByRepoExampleMicroserviceAndRepoExampleMicroserviceSetOrderByAllContentsNumberAsc(microservice, microserviceSet);
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
        repoExampleMetricsRepository.saveAll(metrics);
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

    public void calculateMetricsScores(String processType) {
        List<? extends CodeRepoMetrics> repoExampleMetricsList;
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            repoExampleMetricsList = repoExampleMetricsRepository.findAll();
        } else {
            repoExampleMetricsList = repoMinedMetricsRepository.findAll();
        }
        for(CodeRepoMetrics metrics: repoExampleMetricsList){
            CodeRepo codeRepo;
            if(metrics instanceof RepoExampleMetrics exampleMetrics){
                codeRepo = exampleMetrics.getRepoExample();
            } else {
                codeRepo = ((RepoMinedMetrics)metrics).getRepoMined();
            }
            boolean hasMsSetIndicator = metrics.getProgrammingLanguages() > 1 && metrics.getDatabaseServices() > 1;
            boolean hasMonolithIndicator = metrics.getProgrammingLanguages() > 1 && hasFrontendLanguages(metrics.getLanguages());
            double score = calculateScore(metrics, hasMsSetIndicator);
            codeRepo.setScore(score);
            if (score > MS_CLASSIFICATION_SCORE){
                if(hasMsSetIndicator){
                    codeRepo.setClassification("MICROSERVICE_SET");
                } else if(hasMonolithIndicator){
                    codeRepo.setScore(codeRepo.getScore() - 2);
                    codeRepo.setClassification("MONOLITH");
                }
                else {
                    codeRepo.setClassification("MICROSERVICE");
                }
            } else {
                codeRepo.setClassification("MONOLITH");
            }
            codeRepoService.save(codeRepo);
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

    private double calculateScore(CodeRepoMetrics codeRepoMetrics, boolean hasMsSetIndicator) {
        double score = 0;
        score += getNumericalScore(codeRepoMetrics.getSize(), thresholdService.findByMetric(SIZE, hasMsSetIndicator).getThresholdValue());
        score += getNumericalScore(codeRepoMetrics.getFiles(), thresholdService.findByMetric(FILES, hasMsSetIndicator).getThresholdValue());
        score += getNumericalScore(codeRepoMetrics.getAllContentsNumber(), thresholdService.findByMetric(ALL_CONTENTS, hasMsSetIndicator).getThresholdValue());
        score += getBooleanScore(codeRepoMetrics.isDockerfile());
        score += getBooleanScore(codeRepoMetrics.isLogsService());
        score += getBooleanScore(codeRepoMetrics.isDatabaseConnection());
        score += getBooleanScore(codeRepoMetrics.isMessaging());
        score += getBooleanScore(codeRepoMetrics.isRestful());
        score += getBooleanScore(codeRepoMetrics.isMicroserviceMention());
        score += getBooleanScore(!codeRepoMetrics.isSoap());
        return score;
    }

    private double getBooleanScore(boolean metric) {
        return metric ? 1 : 0;
    }

    private double getNumericalScore(long metric, double thresholdValue) {
        return metric <= thresholdValue ? 1 : 0;
    }
}
