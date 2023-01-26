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

    public Page<RepoExampleMetrics> findAll(Pageable paging) {
        return repository.findAll(paging);
    }

    @Async
    public CompletableFuture<List<RepoExampleMetrics>> generateMetrics(ProcessExecution processExecution, List<RepoExample> repoExamples){
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

    public long countByMicroserviceMentionTrue(){
        return repository.countByMicroserviceMentionTrue();
    }

    public long countByMicroserviceMentionFalse(){
        return repository.countByMicroserviceMentionFalse();
    }

    public long countByDatabaseConnectionTrue(){
        return repository.countByDatabaseConnectionTrue();
    }

    public long countByDatabaseConnectionFalse(){
        return repository.countByDatabaseConnectionFalse();
    }

    public long countByDockerfileTrue(){
        return repository.countByDockerfileTrue();
    }

    public long countByDockerfileFalse(){
        return repository.countByDockerfileFalse();
    }

    public long countByRestfulTrue(){
        return repository.countByRestfulTrue();
    }

    public long countByRestfulFalse(){
        return repository.countByRestfulFalse();
    }

    public long countByMessagingTrue(){
        return repository.countByMessagingTrue();
    }

    public long countByMessagingFalse(){
        return repository.countByMessagingFalse();
    }

    public long countBySoapTrue(){
        return repository.countBySoapTrue();
    }

    public long countBySoapFalse(){
        return repository.countBySoapFalse();
    }

    public long countByLogsServiceTrue(){
        return repository.countByLogsServiceTrue();
    }

    public long countByLogsServiceFalse(){
        return repository.countByLogsServiceFalse();
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
                    .files(getFilesCount(repoExample))
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

    private long getFilesCount(RepoExample repoExample) {
        ContentDto[] contents = gitHubApiService.getRootContentsData(repoExample.getOwner(), repoExample.getName());
        return contents != null ? contents.length : 0;
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

    public RepoExampleMetrics findByRepoExample(RepoExample repoExample) {
        return repository.findByRepoExample(repoExample);
    }

    public long countAll() {
        return repository.count();
    }

    public MetricsStatisticsDto getMetricsStatistics() {
        float totalCount = countAll();
        if(totalCount == 0) return MetricsStatisticsDto.builder().build();
        long countByMicroserviceMentionTrue = countByMicroserviceMentionTrue();
        long countByDatabaseConnectionTrue = countByDatabaseConnectionTrue();
        long countByDockerfileTrue = countByDockerfileTrue();
        long countByRestfulTrue = countByRestfulTrue();
        long countByMessagingTrue = countByMessagingTrue();
        long countBySoapTrue = countBySoapTrue();
        long countByLogsServiceTrue = countByLogsServiceTrue();
        return MetricsStatisticsDto.builder()
                .totalMetrics((long) totalCount)
                .maxRepoFiles(findMaxRepoFiles())
                .minRepoFiles(findMinRepoFiles())
                .averageRepoFiles(findAverageRepoFiles())
                .maxRepoAllContentsNumber(findMaxRepoAllContentsNumber())
                .averageRepoAllContentsNumber(findAverageRepoAllContentsNumber())
                .maxSize(findMaxSize())
                .minSize(findMinSize())
                .averageSize(findAverageSize())
                .countByMicroserviceMentionTrue(countByMicroserviceMentionTrue)
                .countByMicroserviceMentionFalse(countByMicroserviceMentionFalse())
                .countByDatabaseConnectionTrue(countByDatabaseConnectionTrue)
                .countByDatabaseConnectionFalse(countByDatabaseConnectionFalse())
                .microserviceMentionPercentage(getPercentage(countByMicroserviceMentionTrue, totalCount))
                .countByDockerfileTrue(countByDockerfileTrue)
                .countByDockerfileFalse(countByDockerfileFalse())
                .dockerfilePercentage(getPercentage(countByDockerfileTrue, totalCount))
                .countByRestfulTrue(countByRestfulTrue)
                .countByRestfulFalse(countByRestfulFalse())
                .restfulPercentage(getPercentage(countByRestfulTrue , totalCount))
                .countByMessagingTrue(countByMessagingTrue)
                .countByMessagingFalse(countByMessagingFalse())
                .messagingPercentage(getPercentage(countByMessagingTrue , totalCount))
                .countBySoapTrue(countBySoapTrue)
                .countBySoapFalse(countBySoapFalse())
                .soapPercentage(getPercentage(countBySoapTrue, totalCount))
                .countByLogsServiceTrue(countByLogsServiceTrue)
                .countByLogsServiceFalse(countByLogsServiceFalse())
                .logsServicePercentage(getPercentage(countByLogsServiceTrue , totalCount))
                .build();
    }

    private float getPercentage(long numerator, float denominator) {
        return Math.round((numerator / denominator) * 100);
    }
}
