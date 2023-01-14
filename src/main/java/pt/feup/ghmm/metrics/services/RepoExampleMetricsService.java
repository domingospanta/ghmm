package pt.feup.ghmm.metrics.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import pt.feup.ghmm.core.dtos.AllContentDto;
import pt.feup.ghmm.core.dtos.ContentDto;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.dtos.SearchResultDto;
import pt.feup.ghmm.core.services.GitHubApiService;
import pt.feup.ghmm.metrics.models.Language;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.repositories.RepoExampleMetricsRepository;

import java.util.*;

@AllArgsConstructor
@Service
public class RepoExampleMetricsService {
    private RepoExampleMetricsRepository repository;

    private LanguageService languageService;

    private RepoExampleService repoExampleService;

    private GitHubApiService gitHubApiService;

    public List<RepoExampleMetrics> generateMetrics(List<RepoExample> repoExamples){
        if(CollectionUtils.isEmpty(repoExamples)) return new ArrayList<>();
        List<RepoExampleMetrics> metrics = new ArrayList<>();
        repoExamples.forEach(repoExample -> {
            RepoExampleMetrics repoMetrics = fetchMetrics(repoExample);
            metrics.add(repoMetrics);
        });
        return metrics;
    }

    public boolean saveAll(List<RepoExampleMetrics> metrics){
        try {
            repository.saveAll(metrics);
        }catch (Exception exception){
            return false;
        }
        return true;
    }

    private RepoExampleMetrics fetchMetrics(RepoExample repoExample) {
        MainRepositoryDto mainRepositoryDto = gitHubApiService.getMainRepositoryData(repoExample.getOwner(), repoExample.getName());
        return RepoExampleMetrics.builder()
                .repoExample(repoExample)
                .size(mainRepositoryDto.getSize())
                .defaultBranch(mainRepositoryDto.getDefaultBranch())
                .defaultLang(getLanguage(mainRepositoryDto.getLanguage()))
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
    }

    private Set<Language> getLanguages(RepoExample repoExample) {
        HashMap<String, Integer> map = gitHubApiService.getLanguagesData(repoExample.getOwner(), repoExample.getName());
        return getLanguageListFromMap(map);
    }

    private Set<Language> getLanguageListFromMap(HashMap<String, Integer> map) {
        Set<Language> languages = new HashSet<>();
        for(String language: map.keySet()){
            languages.add(languageService.findByName(language));
        }
        return languages;
    }

    private Language getLanguage(String lang) {
        return languageService.findByName(lang);
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
        List<String> queryFragments = Arrays.asList("database", "language:sql", "oracle", "mysql", "SQL Server", "redis", "IBM BD2", "Elasticsearch", "SQLite",
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
        List<String> queryFragments = Arrays.asList("logstash", "Datadog" + "Syslog-ng", "Rsyslog", "rsyslog", "Logagent", "Graylog", "Splunk", "Fluentd", "Logtail");
        for(String queryFragment: queryFragments){
            SearchResultDto searchResultDto = gitHubApiService.searchRepository(repoExample.getOwner(), repoExample.getName(), queryFragment);
            if(searchResultDto.getTotalCount() > 0){
                return true;
            }
        }
        return false;
    }
}
