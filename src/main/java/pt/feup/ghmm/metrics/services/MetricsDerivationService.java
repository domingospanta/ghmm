package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.*;

@AllArgsConstructor
@Service
public class MetricsDerivationService {

    private static final Logger logger = LoggerFactory.getLogger(MetricsDerivationService.class);

    private RepoExampleMetricsRepository repository;

    public void runMetricsDerivation() {
        List<RepoExampleMetrics> metrics = repository.findAllByRepoExampleMicroserviceOrderBySizeAsc(true);
        runSystemAggregationForWeightNormalization(metrics, true);
        runWeightRatioAggregation(metrics);
        runThresholdDerivation(metrics);

        metrics = repository.findAllByRepoExampleMicroserviceOrderBySizeAsc(false);
        runSystemAggregationForWeightNormalization(metrics, false); // step 4
        runWeightRatioAggregation(metrics); // step 5
        runThresholdDerivation(metrics); // step 6
    }

    public void runSystemAggregation() {

    }

    private void runSystemAggregationForWeightNormalization(List<RepoExampleMetrics> metrics, boolean microservice) {
        double sumOfSize = repository.findSumSize(microservice);
        double sumOfFiles = repository.findSumRepoFiles(microservice);
        double sumOfAllContents = repository.findSumRepoAllContentsNumber(microservice);
        for(RepoExampleMetrics metric: metrics){
            double sizeWeight = metric.getSize() / sumOfSize;
            double filesWeight = metric.getFiles() / sumOfFiles;
            double allContentsWeight = metric.getAllContentsNumber() / sumOfAllContents;
            metric.setSizeWeight(sizeWeight);
            metric.setFilesWeight(filesWeight);
            metric.setAllContentsNumberWeight(allContentsWeight);
        }
    }

    private void runWeightRatioAggregation(List<RepoExampleMetrics> metrics) {
        double sizeAggregatedWeight = 0;
        double filesAggregatedWeight = 0;
        double allContentsAggregatedWeight = 0;
        for(RepoExampleMetrics metric: metrics){
            sizeAggregatedWeight += metric.getSizeWeight();
            filesAggregatedWeight += metric.getFilesAggregatedWeight();
            allContentsAggregatedWeight += metric.getAllContentsAggregatedNumberWeight();
            metric.setSizeAggregatedWeight(sizeAggregatedWeight);
            metric.setFilesAggregatedWeight(filesAggregatedWeight);
            metric.setAllContentsAggregatedNumberWeight(allContentsAggregatedWeight);
        }
    }

    private void runThresholdDerivation(List<RepoExampleMetrics> metrics) {

    }
}
