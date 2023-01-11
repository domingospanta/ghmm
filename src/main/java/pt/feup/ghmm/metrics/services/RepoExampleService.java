package pt.feup.ghmm.metrics.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.RepoExampleDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;

@AllArgsConstructor
@Service
public class RepoExampleService {
    private RepoExampleRepository repository;

    public List<RepoResult> save(MultipartFile file) {
        try {
            List<RepoExample> repoExamples = CSVHelper.csvToRepoExamples(file.getInputStream());
            return saveAll(repoExamples);
        } catch (IOException e) {
            throw new RuntimeException("fail to store csv data: " + e.getMessage());
        }
    }

    private List<RepoResult> saveAll(List<RepoExample> repoExamples) {
        List<RepoResult> repoResults = new ArrayList<>();
        for(RepoExample example: repoExamples){
            repoResults.add(save(example));
        }
        return repoResults;
    }

    public Page<RepoExample> findAll(Pageable paging) {
        return repository.findAll(paging);
    }

    public Iterable<RepoExample> findAll() {
        return repository.findAll();
    }

    public Page<RepoExample> findByUrlContainingIgnoreCase(String keyword, Pageable paging) {
        return repository.findByUrlContainingIgnoreCase(keyword, paging);
    }

    public RepoResult save(RepoExampleDto repoExampleDto) {
            RepoExample repoExample = RepoExample
                    .builder()
                    .owner(repoExampleDto.getOwner())
                    .name(repoExampleDto.getName())
                    .url(repoExampleDto.getUrl())
                    .microservice(repoExampleDto.isMicroservice())
                    .build();
            return save(repoExample);
    }

    private RepoResult save(RepoExample repoExample) {
        try{
            repository.save(repoExample);
        }catch (TransactionSystemException exception){
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
}
