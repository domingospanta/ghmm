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
import org.thymeleaf.util.MapUtils;
import pt.feup.ghmm.core.dtos.*;
import pt.feup.ghmm.core.services.GitHubApiService;
import pt.feup.ghmm.metrics.dtos.MetricsStatisticsDto;
import pt.feup.ghmm.metrics.dtos.RepoMetricsDto;
import pt.feup.ghmm.metrics.enums.ServiceType;
import pt.feup.ghmm.metrics.models.*;
import pt.feup.ghmm.metrics.repositories.ProcessExecutionRepository;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedMetricsRepository;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static pt.feup.ghmm.core.utils.Constants.EXAMPLE_PROCESS_TYPE;

@AllArgsConstructor
@Service
public class CodeRepoMetricsService {

    private static final Logger logger = LoggerFactory.getLogger(CodeRepoMetricsService.class);

    private RepoExampleMetricsRepository repoExampleMetricsRepository;

    private RepoMinedMetricsRepository repoMinedMetricsRepository;

    private ProcessExecutionRepository processExecutionRepository;

    private LanguageService languageService;

    private ServiceService serviceService;

    private GitHubApiService gitHubApiService;

    private CodeRepoService codeRepoService;

    private MetricsDerivationService metricsDerivationService;

    public Page<? extends CodeRepoMetrics> findAll(Pageable paging, String processType) {
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)) return repoExampleMetricsRepository.findAll(paging);
        return repoMinedMetricsRepository.findAll(paging);
    }

    @Async
    public CompletableFuture<List<CodeRepoMetrics>> runMetricsExtraction(ProcessExecution processExecution, List<? extends CodeRepo> codeRepos){
        if(CollectionUtils.isEmpty(codeRepos)){
            saveProcessExecution(processExecution, 0, 0, "Execution interrupted: nothing to process.", false, false);
            executeMetricsOperations(processExecution.getProcessType());
            return CompletableFuture.completedFuture(new ArrayList<>());
        }
        saveProcessExecution(processExecution, codeRepos.size(), 0, "Starting execution.", true, false);
        List<CodeRepoMetrics> metrics = new ArrayList<>();
        try {
            for(int i = 0; i < codeRepos.size(); i++){
                CodeRepo codeRepo = codeRepos.get(i);
                CodeRepoMetrics codeRepoMetrics = fetchMetrics(codeRepo);
                saveStatusRepoExampleMetrics(codeRepoMetrics, metrics, codeRepo);
                saveProcessExecution(processExecution, codeRepos.size(), i+1, "Progressing execution.", true, false);
            }
            executeMetricsOperations(processExecution.getProcessType());
            saveProcessExecution(processExecution, codeRepos.size(), 0, "Finished execution successfully.", false, false);
        } catch (HttpClientErrorException.Forbidden exception){
            saveProcessExecution(processExecution, codeRepos.size(), 0, "Execution interrupted due to Git Rest API client error.", false, true);
        }
        return CompletableFuture.completedFuture(metrics);
    }

    private void executeMetricsOperations(String processType) {
        cleanUpMetrics(processType);
        countServicesAndLanguages(processType);
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            metricsDerivationService.runMetricsDerivationForRepoExamples();
        }
        metricsDerivationService.calculateScoresAndSetClassification(processType);
    }

    private void cleanUpMetrics(String processType) {
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            List<RepoExampleMetrics> metricsList = repoExampleMetricsRepository.findAllMissProcessedMetrics();
            if(CollectionUtils.isEmpty(metricsList)) return;
            logger.info("cleanUpMetrics removing " + metricsList.size() + " miss processed metrics!");
            repoExampleMetricsRepository.deleteAll(metricsList);
        } else {
            List<RepoMinedMetrics> metricsList  = repoMinedMetricsRepository.findAllMissProcessedMetrics();
            if(CollectionUtils.isEmpty(metricsList)) return;
            logger.info("cleanUpMetrics removing " + metricsList.size() + " miss processed metrics!");
            repoMinedMetricsRepository.deleteAll(metricsList);
        }
    }


    private void countServicesAndLanguages(String processType) {
        List<? extends CodeRepoMetrics> repoExampleMetricsList;
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            repoExampleMetricsList = repoExampleMetricsRepository.findAll();
        }else {
            repoExampleMetricsList = repoMinedMetricsRepository.findAll();
        }
        for(CodeRepoMetrics codeRepoMetrics: repoExampleMetricsList){
            Set<pt.feup.ghmm.metrics.models.Service> services = codeRepoMetrics.getServices();
            services = serviceService.updateType(services);
            long programmingLanguagesCount = getProgrammingLanguagesCount(codeRepoMetrics.getLanguages());
            long databaseServices = getDatabaseServices(services, codeRepoMetrics.isDatabaseConnection());
            long messagingServices = getMessagingServices(services, codeRepoMetrics.isMessaging());
            long logServices = getLogsServices(services, codeRepoMetrics.isLogsService());
            codeRepoMetrics.setProgrammingLanguages(programmingLanguagesCount);
            codeRepoMetrics.setDatabaseServices(databaseServices);
            codeRepoMetrics.setMessagingServices(messagingServices);
            codeRepoMetrics.setLogServices(logServices);
            if(codeRepoMetrics instanceof RepoExampleMetrics exampleMetrics){
                RepoExample repoExample = exampleMetrics.getRepoExample();
                if(repoExample.isMicroservice() &&
                        programmingLanguagesCount > 1 && databaseServices > 1){
                    repoExample.setMicroserviceSet(true);
                }else {
                    repoExample.setMicroserviceSet(false);
                }
                codeRepoService.save(repoExample);
            }
            updateDatabaseFlag(codeRepoMetrics, databaseServices);
            updateMessagingFlag(codeRepoMetrics, messagingServices);
            updateLogsFlag(codeRepoMetrics, logServices);
            save(codeRepoMetrics);
        }
    }

    private void updateLogsFlag(CodeRepoMetrics metrics, long logServices) {
        if(logServices > 0 && !metrics.isLogsService()){
            metrics.setLogsService(true);
        }
    }

    private void updateMessagingFlag(CodeRepoMetrics metrics, long messagingServices) {
        if(messagingServices > 0 && !metrics.isMessaging()){
            metrics.setMessaging(true);
        }
    }

    private void updateDatabaseFlag(CodeRepoMetrics metrics, long databaseServices) {
        if(databaseServices > 0 && !metrics.isDatabaseConnection()){
            metrics.setDatabaseConnection(true);
        }
    }

    private void saveStatusRepoExampleMetrics(CodeRepoMetrics codeRepoMetrics, List<CodeRepoMetrics> metrics, CodeRepo codeRepo) {
        try {
            if (codeRepoMetrics != null) {
                removeOldMetrics(codeRepoMetrics);
                metrics.add(codeRepoMetrics);
                save(codeRepoMetrics);
                codeRepo.setProcessed(true);
                codeRepoService.save(codeRepo);
            }
        } catch (Exception exception) {
            logger.error("Error saving metrics por repo: " + codeRepo.getUrl(), exception);
        }
    }

    private void save(CodeRepoMetrics codeRepoMetrics) {
        if(codeRepoMetrics instanceof RepoExampleMetrics exampleMetrics){
            repoExampleMetricsRepository.save(exampleMetrics);
        } else {
            repoMinedMetricsRepository.save((RepoMinedMetrics) codeRepoMetrics);
        }
    }

    private void removeOldMetrics(CodeRepoMetrics codeRepoMetrics) {
        if(codeRepoMetrics instanceof RepoExampleMetrics exampleMetrics){
            List<RepoExampleMetrics> existingMetrics = repoExampleMetricsRepository.findAllByRepoExampleId(exampleMetrics.getRepoExample().getId());
            if(CollectionUtils.isEmpty(existingMetrics)) return;
            for(RepoExampleMetrics exampleMetric: existingMetrics){
                exampleMetric.setRepoExample(null);
                repoExampleMetricsRepository.save(exampleMetric);
                repoExampleMetricsRepository.delete(exampleMetric);
            }
        } else {
            RepoMinedMetrics repoMinedMetrics = (RepoMinedMetrics) codeRepoMetrics;
            List<RepoMinedMetrics> existingMetrics = repoMinedMetricsRepository.findAllByRepoMinedId(repoMinedMetrics.getRepoMined().getId());
            if(CollectionUtils.isEmpty(existingMetrics)) return;
            for(RepoMinedMetrics minedMetrics: existingMetrics){
                minedMetrics.setRepoMined(null);
                repoMinedMetricsRepository.save(minedMetrics);
                repoMinedMetricsRepository.delete(minedMetrics);
            }
        }

    }

    private ProcessExecution saveProcessExecution(ProcessExecution processExecution, int totalItems, int processedItems, String message, boolean running, boolean error) {
        if(processExecution == null) return null;
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

    public ProcessExecution createProcessExecution(String processType) {
        ProcessExecution processExecution = ProcessExecution.builder()
                .running(true)
                .processType(processType)
                .build();
        processExecutionRepository.save(processExecution);
        return processExecution;
    }

    private CodeRepoMetrics fetchMetrics(CodeRepo codeRepo) throws HttpClientErrorException.Forbidden{
        try {
            MainRepositoryDto mainRepositoryDto = gitHubApiService.getMainRepositoryData(codeRepo.getOwner(), codeRepo.getName());
            if(mainRepositoryDto == null) return null;
            codeRepo = codeRepoService.update(codeRepo, mainRepositoryDto);
            Set<Language> languages = getLanguages(codeRepo);
            long programmingLanguagesCount = getProgrammingLanguagesCount(languages);
            long dockerfiles = getDockerfiles(codeRepo);
            boolean databaseConnection = hasDatabaseConnection(codeRepo);
            Set<pt.feup.ghmm.metrics.models.Service> services = getServices(codeRepo);
            long databaseServices = getDatabaseServices(services, databaseConnection);
            boolean messaging = hasMessaging(codeRepo);
            long messagingServices = getMessagingServices(services, messaging);
            boolean logs = hasLogsService(codeRepo);
            long logServices = getLogsServices(services, logs);

            CodeRepoMetrics codeRepoMetrics = getCodeRepoMetricsInstance(codeRepo);
            codeRepoMetrics.setSize(mainRepositoryDto.getSize());
            codeRepoMetrics.setDefaultBranch(mainRepositoryDto.getDefaultBranch());
            codeRepoMetrics.setDefaultLang(getOrCreateLanguage(mainRepositoryDto.getLanguage()));
            codeRepoMetrics.setLanguages(languages);
            codeRepoMetrics.setProgrammingLanguages(programmingLanguagesCount);
            codeRepoMetrics.setFiles(getFilesCount(codeRepo, mainRepositoryDto.getDefaultBranch()));
            codeRepoMetrics.setAllContentsNumber(getContentsNumber(codeRepo, mainRepositoryDto.getDefaultBranch()));
            codeRepoMetrics.setMicroserviceMention(hasMicroserviceMention(codeRepo));
            codeRepoMetrics.setDatabaseConnection(databaseConnection);
            codeRepoMetrics.setDatabaseServices(databaseServices);
            codeRepoMetrics.setDockerfile(isGreaterThanZero(dockerfiles));
            codeRepoMetrics.setDockerfiles(dockerfiles);
            codeRepoMetrics.setRestful(hasRestful(codeRepo));
            codeRepoMetrics.setSoap(hasSoap(codeRepo, mainRepositoryDto.getLanguage()));
            codeRepoMetrics.setMessaging(messaging);
            codeRepoMetrics.setMessagingServices(messagingServices);
            codeRepoMetrics.setLogsService(logs);
            codeRepoMetrics.setLogServices(logServices);
            codeRepoMetrics.setServices(services);
            return codeRepoMetrics;
        } catch (HttpClientErrorException.Forbidden exception){
            logger.error("Error processing repo: " + codeRepo.getUrl(), exception);
            throw exception;
        } catch (HttpClientErrorException.NotFound exception){
            logger.error("Error processing repo: " + codeRepo.getUrl(), exception);
            codeRepo.setProcessingError(true);
            codeRepo.setProcessed(true);
            codeRepo.setMessage(exception.getMessage());
            codeRepoService.save(codeRepo);
        }
        catch (Exception exception){
            logger.error("Error processing repo: " + codeRepo.getUrl());
        }
        return null;
    }

    private CodeRepoMetrics getCodeRepoMetricsInstance(CodeRepo codeRepo) {
        if(codeRepo instanceof RepoExample example){
            return RepoExampleMetrics.builder()
                    .repoExample(example)
                    .build();
        }
        return RepoMinedMetrics.builder()
                .repoMined((RepoMined) codeRepo)
                .build();
    }

    private long getDatabaseServices(Set<pt.feup.ghmm.metrics.models.Service> services, boolean databaseConnection) {
        if(CollectionUtils.isEmpty(services)) return databaseConnection ? 1 : 0;
        return services.stream().filter(service -> ServiceType.DATABASE.equals(service.getServiceType())).count();
    }

    private long getMessagingServices(Set<pt.feup.ghmm.metrics.models.Service> services, boolean messaging) {
        if(CollectionUtils.isEmpty(services)) return messaging ? 1 : 0;
        return services.stream().filter(service -> ServiceType.MESSAGING.equals(service.getServiceType())).count();
    }

    private long getLogsServices(Set<pt.feup.ghmm.metrics.models.Service> services, boolean logs) {
        if(CollectionUtils.isEmpty(services)) return logs ? 1 : 0;
        return services.stream().filter(service -> ServiceType.LOGS.equals(service.getServiceType())).count();
    }

    private Set<pt.feup.ghmm.metrics.models.Service> getServices(CodeRepo codeRepo) {
        Map<String, ServiceDto> allServices = getAllServices(codeRepo);
        return getServiceListFromMap(allServices);
    }

    private Map<String, ServiceDto> getAllServices(CodeRepo codeRepo) {
        String queryFragment = "services:+language:YAML";
        Map<String, ServiceDto> allServices = new HashMap<>();
        try {
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            for(ItemDto itemDto: searchResultDto.getItems()){
                if(itemDto.getPath().contains("{")) continue;
                DockerComposeDto dockerComposeDto = gitHubApiService.getDockerComposeFileContent(codeRepo.getOwner(), codeRepo.getName(), itemDto.getPath());
                if(dockerComposeDto != null && !MapUtils.isEmpty(dockerComposeDto.getServices())){
                    allServices.putAll(dockerComposeDto.getServices());
                }
            }
        } catch (Exception exception){
            logger.error("exception found while trying to recover services for " + codeRepo.getUrl(), exception);
        }
        return allServices;
    }

    private Set<Language> getLanguages(CodeRepo codeRepo) {
        HashMap<String, Integer> map = gitHubApiService.getLanguagesData(codeRepo.getOwner(), codeRepo.getName());
        return getLanguageListFromMap(map);
    }

    private long getProgrammingLanguagesCount(Set<Language> languages) {
        if(CollectionUtils.isEmpty(languages)) return 0;
        return languages.stream().filter(Language::isProgrammingLanguage).count();
    }

    private Set<pt.feup.ghmm.metrics.models.Service> getServiceListFromMap(Map<String, ServiceDto> map) {
        Set<pt.feup.ghmm.metrics.models.Service> services = new HashSet<>();
        for(Map.Entry<String, ServiceDto> entry: map.entrySet()){
            pt.feup.ghmm.metrics.models.Service service = serviceService.findOrCreateByName(entry.getKey(), entry.getValue().getImage());
            services.add(service);
        }
        return services;
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

    private long getFilesCount(CodeRepo codeRepo, String defaultBranch) {
        AllContentDto allContentDto = gitHubApiService.getAllContentsData(codeRepo.getOwner(), codeRepo.getName(), defaultBranch);
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

    private long getContentsNumber(CodeRepo codeRepo, String defaultBranch) {
        AllContentDto allContentDto = gitHubApiService.getAllContentsData(codeRepo.getOwner(), codeRepo.getName(), defaultBranch);
        return allContentDto != null && !CollectionUtils.isEmpty(allContentDto.getTree()) ? allContentDto.getTree().size() : 0;
    }

    private boolean hasMicroserviceMention(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList( );
        for(String queryFragment: queryFragments){
            if(codeRepo.getName().contains(queryFragment)) return true;
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasDatabaseConnection(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList("database", "language:sql", "oracle", "mysql", "SQL Server", "SQLite",
                "postgres", "cassandra", "mongodb", "extension:db");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private long getDockerfiles(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList("docker","language:Dockerfile","docker-compose", "language:Makefile");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return searchResultDto.getTotalCount();
            }
        }
        return 0;
    }

    private boolean isGreaterThanZero(long totalCount) {
        return totalCount > 0;
    }

    private boolean hasRestful(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList("http","https");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasSoap(CodeRepo codeRepo, String lang) {
        String queryFragment = "soap+language:" + lang;
        SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
        return searchResultDto != null && searchResultDto.getTotalCount() > 0;
    }

    private boolean hasMessaging(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList("kafka", "RabbitMQ", "producer", "consumer", "amqp");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }

    private boolean hasLogsService(CodeRepo codeRepo) {
        List<String> queryFragments = Arrays.asList("logstash", "Datadog", "Syslog-ng", "Rsyslog", "rsyslog", "Logagent", "Graylog", "Fluentd");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(codeRepo.getOwner(), codeRepo.getName(), queryFragment);
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

    public Page<? extends CodeRepoMetrics> findAllByProcessType(String keyword, Pageable paging, String processType) {
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)) return repoExampleMetricsRepository.findByRepoExampleUrlContainingIgnoreCase(keyword, paging);

        return repoMinedMetricsRepository.findByRepoMinedUrlContainingIgnoreCase(keyword, paging);
    }

    public long countAllByMicroservice(boolean microservice) {
        return repoExampleMetricsRepository.countAllByRepoExampleMicroservice(microservice);
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


    public long findMaxRepoFiles(){
        return repoExampleMetricsRepository.findMaxRepoFiles();
    }

    public long findMinRepoFiles(){
        return repoExampleMetricsRepository.findMinRepoFiles();
    }

    public long findAverageRepoFiles(){
        return repoExampleMetricsRepository.findAverageRepoFiles();
    }

    public long findMaxRepoAllContentsNumber(){
        return repoExampleMetricsRepository.findMaxRepoAllContentsNumber();
    }

    public long findMinRepoAllContentsNumber(){
        return repoExampleMetricsRepository.findMinRepoAllContentsNumber();
    }

    public long findAverageRepoAllContentsNumber(){
        return repoExampleMetricsRepository.findAverageRepoAllContentsNumber();
    }

    public long findMaxSize(){
        return repoExampleMetricsRepository.findMaxSize();
    }

    public long findMinSize(){
        return repoExampleMetricsRepository.findMinSize();
    }

    public long findAverageSize(){
        return repoExampleMetricsRepository.findAverageSize();
    }

    public long countByMicroserviceMentionTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByMicroserviceMentionTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByMicroserviceMentionFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByMicroserviceMentionFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByDatabaseConnectionTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByDatabaseConnectionTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByDatabaseConnectionFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByDatabaseConnectionFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByDockerfileTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByDockerfileTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByDockerfileFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByDockerfileFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByRestfulTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByRestfulTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByRestfulFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByRestfulFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByMessagingTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByMessagingTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByMessagingFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByMessagingFalseAndRepoExampleMicroservice(microservice);
    }

    public long countBySoapTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countBySoapTrueAndRepoExampleMicroservice(microservice);
    }

    public long countBySoapFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countBySoapFalseAndRepoExampleMicroservice(microservice);
    }

    public long countByLogsServiceTrueAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByLogsServiceTrueAndRepoExampleMicroservice(microservice);
    }

    public long countByLogsServiceFalseAndRepoExampleMicroservice(boolean microservice){
        return repoExampleMetricsRepository.countByLogsServiceFalseAndRepoExampleMicroservice(microservice);
    }

    public RepoMetricsDto getMetricsForCodeRepos(boolean example) {
        long processedWithoutErrorTotal = codeRepoService.countAllByProcessedTrueAndProcessingErrorFalse(example);
        long processedWithErrorTotal = codeRepoService.countAllByProcessedTrueAndProcessingErrorTrue(example);
        long unprocessed = codeRepoService.countAllByProcessedFalse(example);

        return RepoMetricsDto.builder()
                .total(codeRepoService.countAll(example))
                .processedWithoutError(processedWithoutErrorTotal)
                .processedWithError(processedWithErrorTotal)
                .unprocessed(unprocessed)
                .build();
    }
}
