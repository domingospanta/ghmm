package pt.feup.ghmm.core.utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.micrometer.common.util.StringUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.metrics.models.CodeRepo;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoMined;

public class CSVHelper {
    public static String TYPE = "text/csv";

    public static boolean hasCSVFormat(MultipartFile file) {

        if (file == null || !TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public static List<CodeRepo> csvToCodeRepos(InputStream is, boolean examples) {
        if(is == null) return new ArrayList<>();
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            List<CodeRepo> repos = new ArrayList<>();

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord csvRecord : csvRecords) {
                CodeRepo codeRepo = getCodeRepoInstance(examples, csvRecord);
                boolean isUrlValid = isUrlValid(codeRepo.getUrl());
                if(isUrlValid){
                    codeRepo.setName(getRepositoryNameFromUrl(codeRepo.getUrl()));
                    if(StringUtils.isEmpty(codeRepo.getOwner())){
                        codeRepo.setOwner(getOwnerFromUrl(codeRepo.getUrl()));
                    }
                    if(StringUtils.isEmpty(codeRepo.getAppName())){
                        codeRepo.setAppName(getRepositoryNameFromUrl(codeRepo.getUrl()));
                    }
                } else {
                    codeRepo.setUrl(codeRepo.getUrl() + " invalid URL!");
                    codeRepo.setOwner(null);
                    codeRepo.setName(null);
                    codeRepo.setAppName(null);
                }
                repos.add(codeRepo);
            }
            return repos;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }

    private static CodeRepo getCodeRepoInstance(boolean examples, CSVRecord csvRecord) {
        if(examples){
            return RepoExample.builder()
                    .owner(csvRecord.get("owner"))
                    .appName(csvRecord.get("name"))
                    .url(csvRecord.get("url"))
                    .microservice(Boolean.parseBoolean(csvRecord.get("microservice")))
                    .createdDate(new Date())
                    .build();
        }
        return RepoMined.builder()
                .appName(csvRecord.get(0))
                .url(csvRecord.get(1))
                .minedDate(new Date())
                .build();
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

