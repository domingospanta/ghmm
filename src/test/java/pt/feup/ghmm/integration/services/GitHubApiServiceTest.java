package pt.feup.ghmm.integration.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.feup.ghmm.integration.dtos.AllContentDto;
import pt.feup.ghmm.integration.dtos.ContentDto;
import pt.feup.ghmm.integration.dtos.MainRepositoryDto;
import pt.feup.ghmm.integration.dtos.SearchResultDto;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureWebClient
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {GitHubApiService.class})
class GitHubApiServiceTest {

    @Autowired
    GitHubApiService gitHubApiService;

    @Test
    void getMainRepositoryData(){
        MainRepositoryDto repositoryData = gitHubApiService.getMainRepositoryData("", "tes");
        assertNull(repositoryData);
        repositoryData = gitHubApiService.getMainRepositoryData(null, "");
        assertNull(repositoryData);
        repositoryData = gitHubApiService.getMainRepositoryData("zammad", "zammad");
        assertNotNull(repositoryData);
    }

    @Test
    void getLanguagesData() {
        HashMap<String, Integer> map = gitHubApiService.getLanguagesData("zammad", "zammad");
        assertNotNull(map);
    }

    @Test
    void getRootContentsData() {
        ContentDto[] contentDtos = gitHubApiService.getRootContentsData("zammad", "zammad");
        assertNotNull(contentDtos);
    }

    @Test
    void getAllContentsData() {
        AllContentDto allContentDto = gitHubApiService.getAllContentsData("zammad", "zammad", "develop");
        assertNotNull(allContentDto);
    }

    @Test
    void searchRepository() {
        SearchResultDto searchResultDto = gitHubApiService.searchRepository("zammad", "zammad", "language:Dockerfile");
        assertNotNull(searchResultDto);
    }
}