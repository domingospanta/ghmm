package pt.feup.ghmm.metrics.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.feup.ghmm.metrics.dtos.CodeRepoDto;
import pt.feup.ghmm.metrics.dtos.CodeRepoUploadDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.services.CodeRepoService;
import pt.feup.ghmm.core.utils.CSVHelper;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/repo/examples")
public class RepoExamplesController {

    private CodeRepoService codeRepoService;

    private final String LIST_PAGE = "repolist";
    private final String UPLOAD_PAGE = "repoupload";

    @GetMapping("/all")
    public String getAll(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        try {
            List<RepoExample> repoExamples;
            Pageable paging = PageRequest.of(page - 1, size);

            Page<RepoExample> pageTuts;
            if (keyword == null) {
                pageTuts = codeRepoService.findAllExamples(paging);
            } else {
                pageTuts = codeRepoService.findByUrlContainingIgnoreCase(keyword, paging);
                model.addAttribute("keyword", keyword);
            }

            repoExamples = pageTuts.getContent();

            model.addAttribute("repoExamples", repoExamples);
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
    public String getRepoExamplesPage(Model model){
        return prepareDataForUploadPage(model);
    }

    @GetMapping("/delete/{id}")
    public String deleteRepoExample(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        RepoExample repoExample = codeRepoService.findById(id);
        if(repoExample != null){
            RepoResult repoResult = codeRepoService.delete(repoExample);
            redirectAttributes.addFlashAttribute("result", repoResult);
        }

        return "redirect:/repo/examples/all";
    }

    @PostMapping("/add")
    public String addRepoExample(@ModelAttribute CodeRepoDto example, Model model) {
        RepoResult repoResult = codeRepoService.save(example);
        model.addAttribute("result", repoResult);
        if(repoResult.isError()){
            model.addAttribute("example", example);
        } else {
            model.addAttribute("example", new CodeRepoDto());
        }
        return UPLOAD_PAGE;
    }


    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        String message;
        boolean error = true;
        List<RepoResult> repoResults = null;
        if (CSVHelper.hasCSVFormat(file)) {
            try {
                repoResults = codeRepoService.save(file, true);
                model.addAttribute("results", repoResults);
                message = "File uploaded successfully: " + file.getOriginalFilename();
                error = false;
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
            }
        } else {
            message = "Please select a csv file!";
        }
        CodeRepoUploadDto uploadResult =
                CodeRepoUploadDto.builder()
                        .resultMap(repoResults)
                        .error(error)
                        .message(message).build();
        model.addAttribute("uploadResult", uploadResult);
        return prepareDataForUploadPage(model);
    }

    private String prepareDataForUploadPage(Model model) {
        model.addAttribute("example", new CodeRepoDto());
        return UPLOAD_PAGE;
    }
}
