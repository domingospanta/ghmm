package pt.feup.ghmm.core.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import pt.feup.ghmm.metrics.models.RepoExample;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CSVHelperTest {

    @Test
    void hasCSVFormat() {

        assertFalse(CSVHelper.hasCSVFormat(null));

        MockMultipartFile file = new MockMultipartFile("test.json", "", "", "".getBytes());
        assertFalse(CSVHelper.hasCSVFormat(file));

        file = new MockMultipartFile("test.csv", "", "text/csv", "".getBytes());
        assertTrue(CSVHelper.hasCSVFormat(file));
    }

    @Test
    void csvToRepoExamples() throws IOException {

        List<RepoExample> repoExamples = CSVHelper.csvToRepoExamples(null);
        assertEquals( 0, repoExamples.size());

        MockMultipartFile file = new MockMultipartFile("test.json", "", "text/csv", ("" +
                "owner,name,url,microservice\n" +
                ",instana.robot.shop,https://github.com/instana/robot-shop,TRUE").getBytes());

        repoExamples = CSVHelper.csvToRepoExamples(file.getInputStream());
        assertEquals( 1, repoExamples.size());

        RepoExample repoExample = repoExamples.get(0);
        assertEquals( "instana", repoExample.getOwner());
        assertEquals( "instana.robot.shop", repoExample.getName());
        assertEquals( "https://github.com/instana/robot-shop", repoExample.getUrl());
        assertTrue( repoExample.isMicroservice());
    }

    @Test
    void isUrlValid() {
        String url = "";
        boolean isGitHubUrlValid = CSVHelper.isUrlValid(url);
        assertFalse(isGitHubUrlValid);

        url = "https://test.com/instana/robot-shop";
        isGitHubUrlValid = CSVHelper.isUrlValid(url);

        assertFalse(isGitHubUrlValid);

        url = "https://github.com/instana/robot-shop";
        isGitHubUrlValid = CSVHelper.isUrlValid(url);
        assertTrue(isGitHubUrlValid);
    }

    @Test
    void getOwnerFromUrl() {
        String url = "";
        String ownerFromUrl = CSVHelper.getOwnerFromUrl(url);
        assertEquals("", ownerFromUrl);

        url = "https://github.com/instana/robot-shop";
        ownerFromUrl = CSVHelper.getOwnerFromUrl(url);
        assertEquals("instana", ownerFromUrl);
    }

    @Test
    void getNameFromUrl() {
        String url = "";
        String nameFromUrl = CSVHelper.getRepositoryNameFromUrl(url);
        assertEquals("", nameFromUrl);

        url = "https://github.com/instana/robot-shop";
        nameFromUrl = CSVHelper.getRepositoryNameFromUrl(url);
        assertEquals("robot-shop", nameFromUrl);
    }
}