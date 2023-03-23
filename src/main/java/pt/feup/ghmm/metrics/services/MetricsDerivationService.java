package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.core.dtos.ClassificationDto;
import pt.feup.ghmm.metrics.models.*;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedMetricsRepository;

import java.util.*;

import static pt.feup.ghmm.core.utils.Constants.*;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

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

    public void calculateScoresAndSetClassification(String processType) {
        List<? extends CodeRepoMetrics> codeRepoMetricsList;
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            codeRepoMetricsList = repoExampleMetricsRepository.findAll();
        } else {
            codeRepoMetricsList = repoMinedMetricsRepository.findAll();
        }
        for(CodeRepoMetrics metrics: codeRepoMetricsList){
            CodeRepo codeRepo;
            if(metrics instanceof RepoExampleMetrics exampleMetrics){
                codeRepo = exampleMetrics.getRepoExample();
            } else {
                codeRepo = ((RepoMinedMetrics)metrics).getRepoMined();
            }
            boolean hasMsSetIndicator = metrics.getProgrammingLanguages() > 1 && metrics.getDatabaseServices() > 1;
            boolean hasMonolithIndicator = metrics.getProgrammingLanguages() <= 2 && hasFrontendLanguages(metrics.getLanguages());
            StringBuilder report = new StringBuilder();
            double score = calculateScore(metrics, hasMsSetIndicator, report);
            codeRepo.setMessage(report.toString());
            ClassificationDto classificationDto = getCodeRepoClassification(score, hasMsSetIndicator, hasMonolithIndicator);
            codeRepo.setClassification(classificationDto.getClassification());
            codeRepo.setScore(classificationDto.getScore());
            codeRepoService.save(codeRepo);
        }
    }

    private ClassificationDto getCodeRepoClassification(double score, boolean hasMsSetIndicator, boolean hasMonolithIndicator) {
        if (score > MS_CLASSIFICATION_SCORE){
            if(hasMsSetIndicator){
                return ClassificationDto.builder()
                        .classification(MICROSERVICE_SET)
                        .score(score)
                        .build();
            }
            if(hasMonolithIndicator){
                score -= 1;
                if (score > MS_CLASSIFICATION_SCORE){
                    return ClassificationDto.builder()
                            .classification(MICROSERVICE_SET)
                            .score(score)
                            .build();
                }
                return ClassificationDto.builder()
                        .classification(MONOLITH)
                        .score(score)
                        .build();
            }
            return ClassificationDto.builder()
                    .classification(MICROSERVICE)
                    .score(score)
                    .build();
        }
        return ClassificationDto.builder()
                .classification(MONOLITH)
                .score(score)
                .build();
    }

    private boolean hasFrontendLanguages(Set<Language> languages) {
        for (Language language: languages){
            if("HTML".equalsIgnoreCase(language.getName()) || "CSS".equalsIgnoreCase(language.getName())){
                return true;
            }
        }
        return false;
    }

    private double calculateScore(CodeRepoMetrics codeRepoMetrics, boolean hasMsSetIndicator, StringBuilder report) {
        double score = 0;
        report.append("Missing: ");
        score += getNumericalScore(codeRepoMetrics.getSize(), thresholdService.findByMetric(SIZE, hasMsSetIndicator).getThresholdValue(), report, SIZE);
        score += getNumericalScore(codeRepoMetrics.getFiles(), thresholdService.findByMetric(FILES, hasMsSetIndicator).getThresholdValue(), report, FILES);
        score += getNumericalScore(codeRepoMetrics.getAllContentsNumber(), thresholdService.findByMetric(ALL_CONTENTS, hasMsSetIndicator).getThresholdValue(), report, ALL_CONTENTS);
        score += getBooleanScore(codeRepoMetrics.isDockerfile(), report, DOCKERFILE);
        score += getBooleanScore(codeRepoMetrics.isLogsService(), report, LOG_SERVICE);
        score += getBooleanScore(codeRepoMetrics.isDatabaseConnection(), report, DATABASE);
        score += getBooleanScore(codeRepoMetrics.isMessaging(), report, MESSAGING);
        score += getBooleanScore(codeRepoMetrics.isRestful(), report, REST);
        score += getBooleanScore(codeRepoMetrics.isMicroserviceMention(), report, MS_MENTION);
        score += getBooleanScore(!codeRepoMetrics.isSoap(), report, SOAP);
        if(MAX_CLASSIFICATION_SCORE == score){
            report.setLength(0);
            report.append("All metrics found!");
        }
        return score;
    }

    private double getBooleanScore(boolean metricValue, StringBuilder report, String metric) {
        if(metricValue){
            return 1;
        }
        report.append(" ").append(metric);
        return 0;
    }

    private double getNumericalScore(long metricValue, double thresholdValue, StringBuilder report, String metric) {
        if(metricValue <= thresholdValue){
            return 1;
        }
        report.append(" ").append(metric);
        return 0;
    }
}
