package pt.feup.ghmm.metrics.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.RepoExampleDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedRepository;

import static pt.feup.ghmm.core.utils.CSVHelper.getOwnerFromUrl;
import static pt.feup.ghmm.core.utils.CSVHelper.getRepositoryNameFromUrl;

@AllArgsConstructor
@Service
public class CodeRepoService {

    private static final Logger logger = LoggerFactory.getLogger(CodeRepoService.class);

    private RepoExampleRepository repoExampleRepository;

   private RepoMinedRepository repoMinedRepository;

    public List<RepoResult> save(MultipartFile file) {
        try {
            List<RepoExample> repoExamples = CSVHelper.csvToRepoExamples(file.getInputStream());
            return saveAll(repoExamples);
        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }

    public RepoExample findById(Long id){
        if(id == null) return null;
        try {
            return repoExampleRepository.findById(id).orElse(null);
        } catch (Exception exception){
            logger.error("Error deleting repo of id: " + id, exception);
            return null;
        }
    }

    public RepoResult delete(RepoExample repoExample){
        try {
            if(repoExample == null) return null;
            repoExampleRepository.deleteById(repoExample.getId());
        } catch (Exception exception){
            logger.error("Error deleting repo:" + repoExample.getUrl(), exception);
            return RepoResult.builder()
                    .repo(repoExample.getUrl())
                    .error(true)
                    .message(" error " + exception.getMessage() )
                    .build();
        }
        return RepoResult.builder()
                .repo(repoExample.getUrl())
                .error(false)
                .message(" deletion successful!")
                .build();
    }

    private List<RepoResult> saveAll(List<RepoExample> repoExamples) {
        List<RepoResult> repoResults = new ArrayList<>();
        for(RepoExample example: repoExamples){
            repoResults.add(save(example));
        }
        return repoResults;
    }

    public Page<RepoExample> findAll(Pageable paging) {
        return repoExampleRepository.findAll(paging);
    }

    public List<RepoExample> findByProcessedFalse() {
        return repoExampleRepository.findByProcessedFalse();
    }

    public Page<RepoExample> findByUrlContainingIgnoreCase(String keyword, Pageable paging) {
        return repoExampleRepository.findByUrlContainingIgnoreCase(keyword, paging);
    }


    public long countAllByProcessedTrueAndProcessingErrorFalse() {
        return repoExampleRepository.countAllByProcessedTrueAndProcessingErrorFalse();
    }

    public long countAllByProcessedTrueAndProcessingErrorTrue() {
        return repoExampleRepository.countAllByProcessedTrueAndProcessingErrorTrue();
    }

    public long countAllByProcessedFalse() {
        return repoExampleRepository.countAllByProcessedFalse();
    }

    public long countAll() {
        return repoExampleRepository.count();
    }



    public RepoResult save(RepoExampleDto repoExampleDto) {
            RepoExample repoExample = RepoExample
                    .builder()
                    .url(repoExampleDto.getUrl())
                    .appName(repoExampleDto.getName())
                    .owner(repoExampleDto.getOwner())
                    .name(getRepositoryNameFromUrl(repoExampleDto.getUrl()))
                    .microservice(repoExampleDto.isMicroservice())
                    .build();
            return save(repoExample);
    }

    public RepoResult save(RepoExample repoExample) {
        try{
            repoExampleRepository.save(repoExample);
        }catch (TransactionSystemException | DataIntegrityViolationException exception){
            return RepoResult.builder()
                    .error(true)
                    .repo(repoExample.getUrl())
                    .message(exception.getMessage())
                    .build();
        }

        return RepoResult.builder()
                .error(false)
                .repo(repoExample.getUrl())
                .message("Saved successfully")
                .build();

    }

    public RepoExample update(RepoExample repoExample, MainRepositoryDto mainRepositoryDto) {
        String repoCurrentUrl = mainRepositoryDto.getUrl();
        if(!StringUtils.isEmpty(repoCurrentUrl) &&
                !repoCurrentUrl.equalsIgnoreCase(repoExample.getUrl())){
            repoExample.setUrl(repoCurrentUrl);
            repoExample.setName(getRepositoryNameFromUrl(repoCurrentUrl));
            repoExample.setOwner(getOwnerFromUrl(repoCurrentUrl));
            repoExampleRepository.save(repoExample);
        }
        return repoExample;
    }
}
