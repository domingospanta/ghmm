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
import pt.feup.ghmm.metrics.models.ProcessExecution;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureWebClient
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RepoExampleMetricsService.class, GitHubApiService.class})
class RepoExampleMetricsServiceTest {

    @MockBean
    private RepoExampleMetricsRepository repository;

    @MockBean
    private LanguageService languageService;

    @MockBean
    private RepoExampleService repoExampleService;

    @Autowired
    private GitHubApiService gitHubApiService;

    @Autowired
    private RepoExampleMetricsService repoExampleMetricsService;

    @Test
    void generateMetrics() throws ExecutionException, InterruptedException {
        List<RepoExampleMetrics> repoExampleMetrics = repoExampleMetricsService.generateMetrics(null, null).get();
        assertEquals(repoExampleMetrics.size(), 0);

        List<RepoExample> repoExamples = new ArrayList<>();
        repoExampleMetrics = repoExampleMetricsService.generateMetrics(ProcessExecution.builder().build(), repoExamples).get();
        assertEquals(repoExampleMetrics.size(), 0);

        RepoExample repoExample = RepoExample.builder()
                .owner("zammad")
                .name("zammad")
                .url("https://github.com/zammad/zammad")
                .build();
        repoExamples.add(repoExample);

        repoExampleMetrics = repoExampleMetricsService.generateMetrics(ProcessExecution.builder()
                .running(true)
                .processType("Metrics")
                .build(), repoExamples).get();
        assertEquals(repoExampleMetrics.size(), 1);

        RepoExampleMetrics metrics = repoExampleMetrics.get(0);
        assertNotNull(metrics.getRepoExample());
        assertNotNull(metrics.getDefaultBranch());
        assertNotEquals(0, metrics.getSize());
        assertNotEquals(0, metrics.getAllContentsNumber());
        assertNotEquals(0, metrics.getFiles());
    }
}