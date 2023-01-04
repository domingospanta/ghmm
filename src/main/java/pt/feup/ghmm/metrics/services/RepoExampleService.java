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
            try {
                repository.save(example);
                repoResults.add(RepoResult.builder()
                                .repo(example.getUrl())
                                .message("Saved successfully")
                        .build());
            } catch (TransactionSystemException exception){
                repoResults.add(RepoResult.builder()
                        .repo(example.getUrl())
                        .message(exception.getMessage())
                        .build());
            }
        }
        return repoResults;
    }


    public Page<RepoExample> findAll(Pageable paging) {
        return repository.findAll(paging);
    }

    public Page<RepoExample> findByUrlContainingIgnoreCase(String keyword, Pageable paging) {
        return repository.findByUrlContainingIgnoreCase(keyword, paging);
    }
}
