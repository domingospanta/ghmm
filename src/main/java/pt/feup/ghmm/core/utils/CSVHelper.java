package pt.feup.ghmm.core.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import io.micrometer.common.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.metrics.models.RepoExample;

public class CSVHelper {
    public static String TYPE = "text/csv";

    public static boolean hasCSVFormat(MultipartFile file) {

        if (file == null || !TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static List<RepoExample> csvToRepoExamples(InputStream is) {
        if(is == null) return new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            List<RepoExample> repos = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                RepoExample repoExample = RepoExample.builder()
                        .owner(csvRecord.get("owner"))
                        .appName(csvRecord.get("name"))
                        .url(csvRecord.get("url"))
                        .microservice(Boolean.parseBoolean(csvRecord.get("microservice")))
                        .build();
                boolean isUrlValid = isUrlValid(repoExample.getUrl());
                if(isUrlValid){
                    repoExample.setName(getRepositoryNameFromUrl(repoExample.getUrl()));
                    if(StringUtils.isEmpty(repoExample.getOwner())){
                        repoExample.setOwner(getOwnerFromUrl(repoExample.getUrl()));
                    }
                    if(StringUtils.isEmpty(repoExample.getAppName())){
                        repoExample.setAppName(getRepositoryNameFromUrl(repoExample.getUrl()));
                    }
                } else {
                    repoExample.setUrl(repoExample.getUrl() + " invalid URL!");
                    repoExample.setOwner(null);
                    repoExample.setName(null);
                    repoExample.setAppName(null);
                }
                repos.add(repoExample);
            }
            return repos;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    public static boolean isUrlValid(String url) {
        return StringUtils.isNotEmpty(url) && url.contains("github") && url.split("/").length == 5;
    }

    public static String getOwnerFromUrl(String url) {
        if (StringUtils.isEmpty(url)) return Strings.EMPTY;
        String[] urlArray = url.split("/");
        return urlArray[3];
    }

    public static String getRepositoryNameFromUrl(String url) {
        if (StringUtils.isEmpty(url)) return Strings.EMPTY;
        String[] urlArray = url.split("/");
        return urlArray[4];
    }


}

