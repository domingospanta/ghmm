package pt.feup.ghmm.identification.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.feup.ghmm.core.utils.CSVHelper;
import pt.feup.ghmm.metrics.dtos.BulkCodeRepoResultDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.dtos.SearchRepoDto;
import pt.feup.ghmm.metrics.models.RepoMined;
import pt.feup.ghmm.metrics.services.CodeRepoService;
import pt.feup.ghmm.metrics.services.LanguageService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@AllArgsConstructor
@Controller
@RequestMapping("/repo/mined")
public class RepoMinedController {

    private CodeRepoService codeRepoService;

    private LanguageService languageService;

    private final String LIST_PAGE = "repoMinedList";
    private final String UPLOAD_PAGE = "repoSearchOrUpload";

    @GetMapping("/all")
    public String getAll(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        try {
            List<RepoMined> minedList;
            Pageable paging = PageRequest.of(page - 1, size);

            Page<RepoMined> pageTuts;
            if (keyword == null) {
                pageTuts = codeRepoService.findAllMined(paging);
            } else {
                pageTuts = codeRepoService.findMinedReposByUrlContainingIgnoreCase(keyword, paging);
                model.addAttribute("keyword", keyword);
            }

            minedList = pageTuts.getContent();

            model.addAttribute("minedRepos", minedList);
            model.addAttribute("currentPage", pageTuts.getNumber() + 1);
            model.addAttribute("totalItems", pageTuts.getTotalElements());
            model.addAttribute("totalPages", pageTuts.getTotalPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return LIST_PAGE;
    }

    @GetMapping("/upload")
    public String getMinedRepoPage(Model model){
        return prepareDataForUploadPage(model);
    }

    @GetMapping("/delete/{id}")
    public String deleteMinedRepo(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        RepoMined repoMined = codeRepoService.findRepoMinedById(id);
        if(repoMined != null){
            RepoResult repoResult = codeRepoService.delete(repoMined);
            redirectAttributes.addFlashAttribute("result", repoResult);
        }

        return "redirect:/repo/mined/all";
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
