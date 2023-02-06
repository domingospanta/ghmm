package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;
import pt.feup.ghmm.core.dtos.AllContentDto;
import pt.feup.ghmm.core.dtos.ContentDto;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.dtos.SearchResultDto;
import pt.feup.ghmm.core.services.GitHubApiService;
import pt.feup.ghmm.metrics.dtos.MetricsStatisticsDto;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.ProcessExecution;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.ProcessExecutionRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@Service
public class RepoExampleMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(RepoExampleMetricsService.class);

    private RepoExampleMetricsRepository repository;

    private ProcessExecutionRepository processExecutionRepository;

    private LanguageService languageService;

    private GitHubApiService gitHubApiService;

    private RepoExampleService repoExampleService;

    MetricsDerivationService metricsDerivationService;

    public Page<RepoExampleMetrics> findAll(Pageable paging) {
        return repository.findAll(paging);
    }

    @Async
    public CompletableFuture<List<RepoExampleMetrics>> runMetricsExtraction(ProcessExecution processExecution, List<RepoExample> repoExamples){
        if(CollectionUtils.isEmpty(repoExamples)){
            saveProcessExecution(processExecution, repoExamples.size(), 0, "Execution interrupted: nothing to process.", false, false);
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        saveProcessExecution(processExecution, repoExamples.size(), 0, "Starting execution.", true, false);
        List<RepoExampleMetrics> metrics = new ArrayList<>();
        try {
            for(int i = 0; i < repoExamples.size(); i++){
                RepoExample repoExample = repoExamples.get(i);
                RepoExampleMetrics repoMetrics = fetchMetrics(repoExample);
                saveStatusRepoExampleMetrics(repoMetrics, metrics, repoExample);
                saveProcessExecution(processExecution, repoExamples.size(), i+1, "Progressing execution.", true, false);
            }
            metricsDerivationService.runMetricsDerivation();
            saveProcessExecution(processExecution, repoExamples.size(), 0, "Finished execution successfully.", false, false);
        } catch (HttpClientErrorException.Forbidden exception){
            saveProcessExecution(processExecution, repoExamples.size(), 0, "Execution interrupted due to Git Rest API client error.", false, true);
        }
        return CompletableFuture.completedFuture(metrics);
    }

    private void saveStatusRepoExampleMetrics(RepoExampleMetrics repoMetrics, List<RepoExampleMetrics> metrics, RepoExample repoExample) {
        try {
            if (repoMetrics != null) {
                metrics.add(repoMetrics);
                repository.save(repoMetrics);
                repoExample.setProcessed(true);
                repoExampleService.save(repoExample);
            }
        } catch (Exception exception) {
            logger.error("Error saving metrics por repo: " + repoExample.getUrl(), exception);
        }
    }

    private ProcessExecution saveProcessExecution(ProcessExecution processExecution, int totalItems, int processedItems, String message, boolean running, boolean error) {
        logger.info("Processed " +  processedItems + " of " + totalItems);
        processExecution.setTotalItems(totalItems);
        processExecution.setProcessedItems(processedItems);
        processExecution.setMessage(message);
        processExecution.setFinished(!running);
        processExecution.setRunning(running);
        processExecution.setError(error);
        processExecutionRepository.save(processExecution);
        return processExecution;
    }

    public ProcessExecution createProcessExecution() {
        ProcessExecution processExecution = ProcessExecution.builder()
                .running(true)
                .processType("Metrics")
                .build();
        processExecutionRepository.save(processExecution);
        return processExecution;
    }

    public long findMaxRepoFiles(){
        return repository.findMaxRepoFiles();
    }

    public long findMinRepoFiles(){
        return repository.findMinRepoFiles();
    }

    public long findAverageRepoFiles(){
        return repository.findAverageRepoFiles();
    }

    public long findMaxRepoAllContentsNumber(){
        return repository.findMaxRepoAllContentsNumber();
    }

    public long findMinRepoAllContentsNumber(){
        return repository.findMinRepoAllContentsNumber();
    }

    public long findAverageRepoAllContentsNumber(){
        return repository.findAverageRepoAllContentsNumber();
    }

    public long findMaxSize(){
        return repository.findMaxSize();
    }

    public long findMinSize(){
        return repository.findMinSize();
    }

    public long findAverageSize(){
        return repository.findAverageSize();
    }

    public long countByMicroserviceMentionTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByMicroserviceMentionTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByMicroserviceMentionFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByMicroserviceMentionFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByDatabaseConnectionTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByDatabaseConnectionTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByDatabaseConnectionFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByDatabaseConnectionFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByDockerfileTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByDockerfileTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByDockerfileFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByDockerfileFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByRestfulTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByRestfulTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByRestfulFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByRestfulFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByMessagingTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByMessagingTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByMessagingFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByMessagingFalseAndRepoExampleMicroservice(microservice);
    }

    public long countBySoapTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countBySoapTrueAndRepoExampleMicroservice(microservice);
    }

    public long countBySoapFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countBySoapFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByLogsServiceTrueAndRepoExampleMicroservice(boolean microservice){
        return repository.countByLogsServiceTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByLogsServiceFalseAndRepoExampleMicroservice(boolean microservice){
        return repository.countByLogsServiceFalseAndRepoExampleMicroservice(microservice);
    }

    private RepoExampleMetrics fetchMetrics(RepoExample repoExample) throws HttpClientErrorException.Forbidden{
        try {
            MainRepositoryDto mainRepositoryDto = gitHubApiService.getMainRepositoryData(repoExample.getOwner(), repoExample.getName());
            if(mainRepositoryDto == null) return null;
            repoExample = repoExampleService.update(repoExample, mainRepositoryDto);

            return RepoExampleMetrics.builder()
                    .repoExample(repoExample)
                    .size(mainRepositoryDto.getSize())
                    .defaultBranch(mainRepositoryDto.getDefaultBranch())
                    .defaultLang(getOrCreateLanguage(mainRepositoryDto.getLanguage()))
                    .languages(getLanguages(repoExample))
                    .files(getFilesCount(repoExample, mainRepositoryDto.getDefaultBranch()))
                    .allContentsNumber(getContentsNumber(repoExample, mainRepositoryDto.getDefaultBranch()))
                    .microserviceMention(hasMicroserviceMention(repoExample))
                    .databaseConnection(hasDatabaseConnection(repoExample))
                    .dockerfile(hasDockerfile(repoExample))
                    .restful(hasRestful(repoExample, mainRepositoryDto.getLanguage()))
                    .soap(hasSoap(repoExample, mainRepositoryDto.getLanguage()))
                    .messaging(hasMessaging(repoExample))
                    .logsService(hasLogsService(repoExample))
                    .build();
        } catch (HttpClientErrorException.Forbidden exception){
            logger.error("Error processing repo: " + repoExample.getUrl(), exception);
            throw exception;
        } catch (HttpClientErrorException.NotFound exception){
            logger.error("Error processing repo: " + repoExample.getUrl(), exception);
            repoExample.setProcessingError(true);
            repoExample.setProcessed(true);
            repoExample.setMessage(exception.getMessage());
            repoExampleService.save(repoExample);
        }
        catch (Exception exception){
            logger.error("Error processing repo: " + repoExample.getUrl());
        }
        return null;
    }

    private Set<Language> getLanguages(RepoExample repoExample) {
        HashMap<String, Integer> map = gitHubApiService.getLanguagesData(repoExample.getOwner(), repoExample.getName());
        return getLanguageListFromMap(map);
    }

    private Set<Language> getLanguageListFromMap(HashMap<String, Integer> map) {
        Set<Language> languages = new HashSet<>();
        for(String language: map.keySet()){
            languages.add(languageService.findOrCreateByName(language));
        }
        return languages;
    }

    private Language getOrCreateLanguage(String lang) {
        return languageService.findOrCreateByName(lang);
    }

    private long getFilesCount(RepoExample repoExample, String defaultBranch) {
        AllContentDto allContentDto = gitHubApiService.getAllContentsData(repoExample.getOwner(), repoExample.getName(), defaultBranch);
        int filesCount = 0;
        final String FILES_TYPE = "blob";
        if(!CollectionUtils.isEmpty(allContentDto.getTree())){
            for(ContentDto contentDto: allContentDto.getTree()){
                if(FILES_TYPE.equals(contentDto.getType())){
                    filesCount++;
                }
            }
        }
        return filesCount;
    }

    private long getContentsNumber(RepoExample repoExample, String defaultBranch) {
        AllContentDto allContentDto = gitHubApiService.getAllContentsData(repoExample.getOwner(), repoExample.getName(), defaultBranch);
        return allContentDto != null && !CollectionUtils.isEmpty(allContentDto.getTree()) ? allContentDto.getTree().size() : 0;
    }

    private boolean hasMicroserviceMention(RepoExample repoExample) {
        List<String> queryFragments = Arrays.asList("microservice", "micro-service");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasDatabaseConnection(RepoExample repoExample) {
        List<String> queryFragments = Arrays.asList("database", "language:sql", "oracle", "mysql", "SQL Server", "SQLite",
                "postgres", "cassandra", "mongodb");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasDockerfile(RepoExample repoExample) {
        String queryFragment = "language:Dockerfile";
        SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
        return searchResultDto != null && searchResultDto.getTotalCount() > 0;
    }

    private boolean hasRestful(RepoExample repoExample, String lang) {
        List<String> queryFragments = Arrays.asList("http+language:" + lang, "https+language:" + lang);
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasSoap(RepoExample repoExample, String lang) {
        String queryFragment = "soap+language:" + lang;
        SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
        return searchResultDto != null && searchResultDto.getTotalCount() > 0;
    }

    private boolean hasMessaging(RepoExample repoExample) {
        List<String> queryFragments = Arrays.asList("kafka", "RabbitMQ" + "producer", "consumer");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasLogsService(RepoExample repoExample) {
        List<String> queryFragments = Arrays.asList("logstash", "Datadog", "Syslog-ng", "Rsyslog", "rsyslog", "Logagent", "Graylog", "Fluentd");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    public ProcessExecution getProcessExecutionById(Long id) {
        Optional<ProcessExecution> processExecution = processExecutionRepository.findById(id);
        return processExecution.orElse(null);
    }

    public Page<RepoExampleMetrics> findByRepoExamples(String keyword, Pageable paging) {
        return repository.findByRepoExampleUrlContainingIgnoreCase(keyword, paging);
    }

    public long countAllByMicroservice(boolean microservice) {
        return repository.countAllByRepoExampleMicroservice(microservice);
    }

    public MetricsStatisticsDto getMetricsStatistics(boolean microservice) {
        float totalCount = countAllByMicroservice(microservice);
        if(totalCount == 0) return MetricsStatisticsDto.builder().build();
        long countByMicroserviceMentionTrue = countByMicroserviceMentionTrueAndRepoExampleMicroservice(microservice);
        long countByDatabaseConnectionTrue = countByDatabaseConnectionTrueAndRepoExampleMicroservice(microservice);
        long countByDockerfileTrue = countByDockerfileTrueAndRepoExampleMicroservice(microservice);
        long countByRestfulTrue = countByRestfulTrueAndRepoExampleMicroservice(microservice);
        long countByMessagingTrue = countByMessagingTrueAndRepoExampleMicroservice(microservice);
        long countBySoapTrue = countBySoapTrueAndRepoExampleMicroservice(microservice);
        long countByLogsServiceTrue = countByLogsServiceTrueAndRepoExampleMicroservice(microservice);
        return MetricsStatisticsDto.builder()
                .totalMetrics((long) totalCount)
                .maxRepoFiles(findMaxRepoFiles())
                .minRepoFiles(findMinRepoFiles())
                .averageRepoFiles(findAverageRepoFiles())
                .maxRepoAllContentsNumber(findMaxRepoAllContentsNumber())
                .minRepoAllContentsNumber(findMinRepoAllContentsNumber())
                .averageRepoAllContentsNumber(findAverageRepoAllContentsNumber())
                .maxSize(findMaxSize())
                .minSize(findMinSize())
                .averageSize(findAverageSize())
                .countByMicroserviceMentionTrue(countByMicroserviceMentionTrue)
                .countByMicroserviceMentionFalse(countByMicroserviceMentionFalseAndRepoExampleMicroservice(microservice))
                .microserviceMentionPercentage(getPercentage(countByMicroserviceMentionTrue, totalCount))
                .countByDatabaseConnectionTrue(countByDatabaseConnectionTrue)
                .countByDatabaseConnectionFalse(countByDatabaseConnectionFalseAndRepoExampleMicroservice(microservice))
                .databaseConnectionPercentage(getPercentage(countByDatabaseConnectionTrue, totalCount))
                .countByDockerfileTrue(countByDockerfileTrue)
                .countByDockerfileFalse(countByDockerfileFalseAndRepoExampleMicroservice(microservice))
                .dockerfilePercentage(getPercentage(countByDockerfileTrue, totalCount))
                .countByRestfulTrue(countByRestfulTrue)
                .countByRestfulFalse(countByRestfulFalseAndRepoExampleMicroservice(microservice))
                .restfulPercentage(getPercentage(countByRestfulTrue , totalCount))
                .countByMessagingTrue(countByMessagingTrue)
                .countByMessagingFalse(countByMessagingFalseAndRepoExampleMicroservice(microservice))
                .messagingPercentage(getPercentage(countByMessagingTrue , totalCount))
                .countBySoapTrue(countBySoapTrue)
                .countBySoapFalse(countBySoapFalseAndRepoExampleMicroservice(microservice))
                .soapPercentage(getPercentage(countBySoapTrue, totalCount))
                .countByLogsServiceTrue(countByLogsServiceTrue)
                .countByLogsServiceFalse(countByLogsServiceFalseAndRepoExampleMicroservice(microservice))
                .logsServicePercentage(getPercentage(countByLogsServiceTrue , totalCount))
                .build();
    }

    private float getPercentage(long numerator, float denominator) {
        return Math.round((numerator / denominator) * 100);
    }
}
