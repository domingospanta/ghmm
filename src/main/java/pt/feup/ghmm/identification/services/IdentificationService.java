package pt.feup.ghmm.identification.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.integration.dtos.*;
import pt.feup.ghmm.identification.models.Language;
import pt.feup.ghmm.metrics.models.CodeRepoMetrics;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.models.RepoMinedMetrics;
import pt.feup.ghmm.repo.models.CodeRepo;
import pt.feup.ghmm.repo.services.CodeRepoService;

import java.util.*;

import static pt.feup.ghmm.integration.utils.Constants.*;

@AllArgsConstructor
@Service
public class IdentificationService {

    private ThresholdService thresholdService;

    private CodeRepoService codeRepoService;

    public void calculateScoresAndSetClassification(List<? extends CodeRepoMetrics> codeRepoMetricsList) {
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
