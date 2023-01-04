package pt.feup.ghmm.metrics.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pt.feup.ghmm.metrics.dtos.RepoExampleUploadDto;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.metrics.services.RepoExampleService;
import pt.feup.ghmm.core.utils.CSVHelper;

import java.util.List;

@AllArgsConstructor
@RestController("/repo/examples")
public class ExampleController {

    private RepoExampleService repoExampleService;

    @GetMapping("/upload")
    public String getExamples(){
        return "Works!";
    }

    @PostMapping("/upload")
    public ResponseEntity<RepoExampleUploadDto> uploadFile(@RequestParam("file") MultipartFile file) {
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
