package pt.feup.ghmm.metrics.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.metrics.dtos.RepoExampleUploadDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.models.RepoExample;
import pt.feup.ghmm.metrics.services.RepoExampleService;
import pt.feup.ghmm.core.utils.CSVHelper;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/repo/examples")
public class ExampleController {

    private RepoExampleService repoExampleService;

    @GetMapping("/page")
    public String getRepoExamplesPage(){
        return "repolist";
    }

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
                pageTuts = repoExampleService.findAll(paging);
            } else {
                pageTuts = repoExampleService.findByUrlContainingIgnoreCase(keyword, paging);
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

        return "repolist";
    }



    @PostMapping("/upload")
    public @ResponseBody ResponseEntity<RepoExampleUploadDto> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = "";

        if (CSVHelper.hasCSVFormat(file)) {
            try {
                List<RepoResult> repoResults = repoExampleService.save(file);

                message = "Uploaded the file successfully: " + file.getOriginalFilename();
                return ResponseEntity.status(HttpStatus.OK).body(
                        RepoExampleUploadDto.builder()
                                .resultMap(repoResults)
                                .message(message).build());
            } catch (Exception e) {
                message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(RepoExampleUploadDto.builder().message(message).build());
            }
        }

        message = "Please upload a csv file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RepoExampleUploadDto.builder().message(message).build());
    }
}
