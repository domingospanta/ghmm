package pt.feup.ghmm.metrics.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.core.dtos.ItemDto;
import pt.feup.ghmm.core.dtos.MainRepositoryDto;
import pt.feup.ghmm.core.dtos.SearchResultDto;
import pt.feup.ghmm.core.services.GitHubApiService;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.BulkCodeRepoResultDto;
import pt.feup.ghmm.metrics.dtos.CodeRepoDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.dtos.SearchRepoDto;
import pt.feup.ghmm.metrics.models.CodeRepo;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.models.RepoMined;
import pt.feup.ghmm.metrics.repositories.RepoExampleRepository;
import pt.feup.ghmm.metrics.repositories.RepoMinedRepository;

import static pt.feup.ghmm.core.utils.CSVHelper.getOwnerFromUrl;
import static pt.feup.ghmm.core.utils.CSVHelper.getRepositoryNameFromUrl;
import static pt.feup.ghmm.core.utils.Constants.EXAMPLE_PROCESS_TYPE;

@AllArgsConstructor
@Service
public class CodeRepoService {
   private static final Logger logger = LoggerFactory.getLogger(CodeRepoService.class);

   private RepoExampleRepository repoExampleRepository;

   private RepoMinedRepository repoMinedRepository;

   private GitHubApiService gitHubApiService;

    public List<RepoResult> save(MultipartFile file, boolean examples) {
        try {
            List<CodeRepo> codeRepos = CSVHelper.csvToCodeRepos(file.getInputStream(), examples);
            return saveAll(codeRepos);
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

    public RepoMined findRepoMinedById(Long id){
        if(id == null) return null;
        try {
            return repoMinedRepository.findById(id).orElse(null);
        } catch (Exception exception){
            logger.error("Error deleting repo of id: " + id, exception);
            return null;
        }
    }

    public RepoResult delete(CodeRepo codeRepo){
        try {
            if(codeRepo == null) return null;
            if(codeRepo instanceof RepoExample){
              repoExampleRepository.deleteById(codeRepo.getId());
            } else {
              repoMinedRepository.deleteById(codeRepo.getId());
            }
        } catch (Exception exception){
            logger.error("Error deleting repo:" + codeRepo.getUrl(), exception);
            return RepoResult.builder()
                    .repo(codeRepo.getUrl())
                    .error(true)
                    .message(" error " + exception.getMessage() )
                    .build();
        }
        return RepoResult.builder()
                .repo(codeRepo.getUrl())
                .error(false)
                .message(" deletion successful!")
                .build();
    }

    private List<RepoResult> saveAll(List<CodeRepo> codeRepos) {
        List<RepoResult> repoResults = new ArrayList<>();
        for(CodeRepo codeRepo: codeRepos){
            repoResults.add(save(codeRepo));
        }
        return repoResults;
    }

    public Page<RepoExample> findAllExamples(Pageable paging) {
        return repoExampleRepository.findAll(paging);
    }

    public Page<RepoMined> findAllMined(Pageable paging) {
        return repoMinedRepository.findAll(paging);
    }

    public List<? extends CodeRepo> findByProcessedFalse(String processType) {
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            return repoExampleRepository.findByProcessedFalse();
        }
        return repoMinedRepository.findByProcessedFalse();
    }


    public Page<RepoExample> findByUrlContainingIgnoreCase(String keyword, Pageable paging) {
        return repoExampleRepository.findByUrlContainingIgnoreCase(keyword, paging);
    }

    public Page<RepoMined> findMinedReposByUrlContainingIgnoreCase(String keyword, Pageable paging) {
        return repoMinedRepository.findByUrlContainingIgnoreCase(keyword, paging);
    }


    public long countAllByProcessedTrueAndProcessingErrorFalse(boolean example) {
        if(example){
            return repoExampleRepository.countAllByProcessedTrueAndProcessingErrorFalse();
        }
        return repoMinedRepository.countAllByProcessedTrueAndProcessingErrorFalse();
    }

    public long countAllByProcessedTrueAndProcessingErrorTrue(boolean example) {
        if(example){
            return repoExampleRepository.countAllByProcessedTrueAndProcessingErrorTrue();
        }
        return repoMinedRepository.countAllByProcessedTrueAndProcessingErrorTrue();
    }

    public long countAllByProcessedFalse(boolean example) {
        if(example){
            return repoExampleRepository.countAllByProcessedFalse();
        }
        return repoMinedRepository.countAllByProcessedFalse();
    }

    public long countAll(boolean example) {
        if(example){
            return repoExampleRepository.count();
        }
        return repoMinedRepository.count();
    }

    public RepoResult save(CodeRepoDto codeRepoDto) {
            RepoExample repoExample = RepoExample
                    .builder()
                    .url(codeRepoDto.getUrl())
                    .appName(codeRepoDto.getName())
                    .owner(codeRepoDto.getOwner())
                    .name(getRepositoryNameFromUrl(codeRepoDto.getUrl()))
                    .microservice(codeRepoDto.isMicroservice())
                    .build();
            return save(repoExample);
    }

    public RepoResult save(CodeRepo codeRepo) {
        try{
            codeRepo.setLastModified(new Date());
            if(codeRepo instanceof RepoExample){
                repoExampleRepository.save((RepoExample)codeRepo);
            }else {
                repoMinedRepository.save((RepoMined) codeRepo);
            }
        }catch (TransactionSystemException | DataIntegrityViolationException exception){
            return RepoResult.builder()
                    .error(true)
                    .repo(codeRepo.getUrl())
                    .message(exception.getMessage())
                    .build();
        }

        return RepoResult.builder()
                .error(false)
                .repo(codeRepo.getUrl())
                .message("Saved successfully")
                .build();

    }

    public RepoResult save(ItemDto itemDto) {
        RepoMined repoMined = RepoMined
                .builder()
                .url(itemDto.getUrl())
                .appName(itemDto.getName())
                .owner(getOwnerFromUrl(itemDto.getUrl()))
                .name(itemDto.getName())
                .build();
        return save(repoMined);
    }

    public CodeRepo update(CodeRepo codeRepo, MainRepositoryDto mainRepositoryDto) {
        String repoCurrentUrl = mainRepositoryDto.getUrl();
        if(!StringUtils.isEmpty(repoCurrentUrl) &&
                !repoCurrentUrl.equalsIgnoreCase(codeRepo.getUrl())){
            codeRepo.setUrl(repoCurrentUrl);
            codeRepo.setName(getRepositoryNameFromUrl(repoCurrentUrl));
            codeRepo.setOwner(getOwnerFromUrl(repoCurrentUrl));
            save(codeRepo);
        }
        return codeRepo;
    }

    public BulkCodeRepoResultDto search(SearchRepoDto searchRepo) {
        if(isSearchInvalid(searchRepo)) return getInvalidSearchQueryResponse(searchRepo);
        List<RepoResult> resultMap = new ArrayList<>();
        int page = 0;
        do{
            SearchResultDto searchResultDto = gitHubApiService.searchRepositories(searchRepo, ++page);
            if(searchResultDto != null && CollectionUtils.isNotEmpty(searchResultDto.getItems())){
                for(ItemDto itemDto: searchResultDto.getItems()){
                    RepoResult result = save(itemDto);
                    if(!result.isError()){
                        resultMap.add(result);
                    }
                    if(resultMap.size() >= searchRepo.getQuantity()){
                        break;
                    }
                }
            } else {
                return BulkCodeRepoResultDto.builder()
                        .error(true)
                        .message(searchResultDto != null ? "Your search did not return any result due to: " + searchResultDto.getMessage() + ". Please try again." :
                                "Invalid Search. Please try again.")
                        .build();
            }
        } while(resultMap.size() < searchRepo.getQuantity());

        return BulkCodeRepoResultDto.builder()
                .resultMap(resultMap)
                .message("Found repos successfully!")
                .error(false)
                .build();
    }

    private BulkCodeRepoResultDto getInvalidSearchQueryResponse(SearchRepoDto searchRepo) {
        StringBuilder message = new StringBuilder();
        if(searchRepo.getQuantity() == 0){
            message.append("Quantity cannot be zero!\n");
        }
        if(StringUtils.isEmpty(searchRepo.getSearchString()) && StringUtils.isEmpty(searchRepo.getProgrammingLanguages())){
            message.append("GH API does not allow an empty query parameter. Please type Search String or select at least one Programming Language.");
        }
        BulkCodeRepoResultDto repoResultDto = BulkCodeRepoResultDto.builder()
                .message(message.toString())
                .error(true)
                .build();
        return repoResultDto;
    }

    private boolean isSearchInvalid(SearchRepoDto searchRepo) {
        return searchRepo.getQuantity() == 0 || (StringUtils.isEmpty(searchRepo.getSearchString()) && StringUtils.isEmpty(searchRepo.getProgrammingLanguages()));
    }
}
