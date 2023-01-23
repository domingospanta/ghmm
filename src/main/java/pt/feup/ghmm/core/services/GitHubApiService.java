package pt.feup.ghmm.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;
import pt.feup.ghmm.core.dtos.AllContentDto;
import pt.feup.ghmm.core.dtos.ContentDto;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.dtos.SearchResultDto;
import pt.feup.ghmm.metrics.services.RepoExampleMetricsService;

import java.util.HashMap;

@Service
public class GitHubApiService {

  private static final Logger logger = LoggerFactory.getLogger(GitHubApiService.class);

  private final RestTemplate restTemplate;

  private static int requestCount;

  @Value("${github.api.url}")
  private String END_POINT;

  public GitHubApiService(@Value("${github.api.user}") String user,
                          @Value("${github.api.password}") String password,
                          @Value("${github.api.url}") String url,
                          RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.basicAuthentication(user, password).build();
    END_POINT = url;
    requestCount = 0;
  }

  @Retryable(backoff = @Backoff(delay = 30000))
  public MainRepositoryDto getMainRepositoryData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository;
    logRequest(url);
    return restTemplate.getForObject(url, MainRepositoryDto.class);
  }

  @Retryable(backoff = @Backoff(delay = 30000))
  public HashMap<String, Integer> getLanguagesData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/languages";
    logRequest(url);
    return restTemplate.getForObject(url, HashMap.class);
  }

  @Retryable(backoff = @Backoff(delay = 30000))
  public ContentDto[] getRootContentsData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/contents";
    logRequest(url);
    return restTemplate.getForObject(url, ContentDto[].class);
  }

  @Retryable(backoff = @Backoff(delay = 30000))
  public AllContentDto getAllContentsData(String owner, String repository, String branch){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository) || StringUtils.isEmpty(branch)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/git/trees/" + branch + "?recursive=1";
    logRequest(url);
    return restTemplate.getForObject(url, AllContentDto.class);
  }

  @Retryable(backoff = @Backoff(delay = 30000))
  public SearchResultDto searchRepository(String owner, String repository, String queryFragment){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository) || StringUtils.isEmpty(queryFragment)) return null;
    String url = END_POINT + "search/code?q="+ queryFragment + "+repo:"+ owner + "/" + repository;
    logRequest(url);
    return restTemplate.getForObject(url, SearchResultDto.class);
  }

  private void logRequest(String url) {
    requestCount++;
    logger.info("Making request: " + url + "\n Request count:" + requestCount);
  }
}
