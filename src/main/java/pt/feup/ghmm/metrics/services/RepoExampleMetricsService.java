package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.RepoExampleDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Service
public class RepoExampleMetricsService {
    private RepoExampleMetricsRepository repository;

    private RepoExampleService repoExampleService;

    public void generateMetrics(){
        Iterable<RepoExample> repoExamples = repoExampleService.findAll();
        repoExamples.forEach(repoExample -> {
            RepoExampleMetrics metrics = fetchMetrics(repoExample);
            repository.save(metrics);
        });
    }

    private RepoExampleMetrics fetchMetrics(RepoExample repoExample) {
        return new RepoExampleMetrics();
    }
}
