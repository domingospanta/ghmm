package pt.feup.ghmm.core.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;
import pt.feup.ghmm.core.dtos.AllContentDto;
import pt.feup.ghmm.core.dtos.ContentDto;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.dtos.SearchResultDto;

import java.util.HashMap;

@Service
public class GitHubApiService {

  private final RestTemplate restTemplate;

  @Value("${github.api.url}")
  private String END_POINT;

  public GitHubApiService(@Value("${github.api.user}") String user,
                          @Value("${github.api.password}") String password,
                          @Value("${github.api.url}") String url,
                          RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.basicAuthentication(user, password).build();
    END_POINT = url;
  }

  public MainRepositoryDto getMainRepositoryData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository;
    return restTemplate.getForObject(url, MainRepositoryDto.class);
  }

  public HashMap<String, Integer> getLanguagesData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/languages";
    return restTemplate.getForObject(url, HashMap.class);
  }

  public ContentDto[] getRootContentsData(String owner, String repository){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/contents";
    return restTemplate.getForObject(url, ContentDto[].class);
  }

  public AllContentDto getAllContentsData(String owner, String repository, String branch){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository) || StringUtils.isEmpty(branch)) return null;
    String url = END_POINT + "repos/" + owner + "/" + repository + "/git/trees/" + branch + "?recursive=1";
    return restTemplate.getForObject(url, AllContentDto.class);
  }

  public SearchResultDto searchRepository(String owner, String repository, String queryFragment){
    if(StringUtils.isEmpty(owner) || StringUtils.isEmpty(repository) || StringUtils.isEmpty(queryFragment)) return null;
    String url = END_POINT + "search/code?q="+ queryFragment + "+repo:"+ owner + "/" + repository;
    return restTemplate.getForObject(url, SearchResultDto.class);
  }
}
