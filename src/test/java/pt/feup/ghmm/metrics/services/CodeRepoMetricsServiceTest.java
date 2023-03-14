package pt.feup.ghmm.metrics.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.feup.ghmm.core.services.GitHubApiService;
import pt.feup.ghmm.metrics.models.CodeRepoMetrics;
import pt.feup.ghmm.metrics.models.ProcessExecution;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.ProcessExecutionRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureWebClient
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CodeRepoMetricsService.class, GitHubApiService.class})
class CodeRepoMetricsServiceTest {

    @MockBean
    private RepoExampleMetricsRepository repository;

    @MockBean
    private LanguageService languageService;

    @MockBean
    private CodeRepoService codeRepoService;

    @MockBean
    ProcessExecutionRepository processExecutionRepository;

    @MockBean
    ServiceService serviceService;

    @MockBean
    MetricsDerivationService metricsDerivationService;

    @Autowired
    private GitHubApiService gitHubApiService;

    @Autowired
    private CodeRepoMetricsService codeRepoMetricsService;

    @Test
    void generateMetrics() throws ExecutionException, InterruptedException {
        List<CodeRepoMetrics> codeRepoMetrics = codeRepoMetricsService.runMetricsExtraction(null, null).get();
        assertEquals(codeRepoMetrics.size(), 0);

        List<RepoExample> repoExamples = new ArrayList<>();
        codeRepoMetrics = codeRepoMetricsService.runMetricsExtraction(ProcessExecution.builder().build(), repoExamples).get();
        assertEquals(codeRepoMetrics.size(), 0);

        RepoExample repoExample = RepoExample.builder()
                .owner("zammad")
                .name("zammad")
                .url("https://github.com/zammad/zammad")
                .build();
        repoExamples.add(repoExample);

        codeRepoMetrics = codeRepoMetricsService.runMetricsExtraction(ProcessExecution.builder()
                .running(true)
                .processType("Metrics")
                .build(), repoExamples).get();
        assertEquals(codeRepoMetrics.size(), 1);

        CodeRepoMetrics metrics = codeRepoMetrics.get(0);
        assertNotNull(metrics.getDefaultBranch());
        assertNotEquals(0, metrics.getSize());
        assertNotEquals(0, metrics.getAllContentsNumber());
        assertNotEquals(0, metrics.getFiles());
    }
}