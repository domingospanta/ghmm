package pt.feup.ghmm.repo.controllers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.identification.services.LanguageService;
import pt.feup.ghmm.metrics.dtos.BulkCodeRepoResultDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.dtos.SearchRepoDto;
import pt.feup.ghmm.repo.services.CodeRepoService;
import pt.feup.ghmm.repo.utils.CSVHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@Controller
@RequestMapping("/repo/mined")
public class RepoMinedController {

    private CodeRepoService codeRepoService;

    private LanguageService languageService;

    private final String UPLOAD_PAGE = "repoSearchOrUpload";

    @GetMapping("/upload")
    public String getMinedRepoPage(Model model){
        return prepareDataForUploadPage(model);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        String message;
        boolean error = true;
        List<RepoResult> repoResults = null;
        if (CSVHelper.hasCSVFormat(file)) {
            try {
                repoResults = codeRepoService.save(file, false);
                model.addAttribute("results", repoResults);
                message = "File uploaded successfully: " + file.getOriginalFilename();
                error = false;
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            }
        } else {
            message = "Please select a csv file!";
        }
        BulkCodeRepoResultDto uploadResult =
                BulkCodeRepoResultDto.builder()
                        .resultMap(repoResults)
                        .error(error)
                        .message(message).build();
        model.addAttribute("bulkCodeRepoResult", uploadResult);
        return prepareDataForUploadPage(model);
    }

    @PostMapping("/search")
    public String searchMinedRepo(@ModelAttribute SearchRepoDto searchRepo, Model model) throws ExecutionException, InterruptedException {
        CompletableFuture<BulkCodeRepoResultDto> completableFuture = codeRepoService.search(searchRepo);
        BulkCodeRepoResultDto repoResult = completableFuture.get();
        model.addAttribute("bulkCodeRepoResult", repoResult);
        model.addAttribute("processType", "search");
        model.addAttribute("programmingLanguages", languageService.getProgrammingLanguages());
        if(repoResult.isError()){
            model.addAttribute("searchRepoDto", searchRepo);
        } else {
            model.addAttribute("searchRepoDto", new SearchRepoDto());
        }
        return UPLOAD_PAGE;
    }

    private String prepareDataForUploadPage(Model model) {
        model.addAttribute("searchRepoDto", new SearchRepoDto());
        model.addAttribute("programmingLanguages", languageService.getProgrammingLanguages());
        return UPLOAD_PAGE;
    }
}
