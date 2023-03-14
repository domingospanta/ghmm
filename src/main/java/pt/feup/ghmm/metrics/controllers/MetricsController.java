package pt.feup.ghmm.metrics.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.feup.ghmm.metrics.dtos.MetricsStatisticsDto;
import pt.feup.ghmm.metrics.dtos.ProcessExecutionDto;
import pt.feup.ghmm.metrics.models.ProcessExecution;
import pt.feup.ghmm.metrics.models.RepoExampleMetrics;
import pt.feup.ghmm.metrics.services.CodeRepoMetricsService;
import pt.feup.ghmm.metrics.services.CodeRepoService;

import java.util.List;

@Controller
@RequestMapping("/metrics")
public class MetricsController {

    private CodeRepoMetricsService codeRepoMetricsService;
    private CodeRepoService codeRepoService;
    private final String LIST_PAGE = "metricslist";
    private final String GENERATE_PAGE = "metricsgeneration";
    private final String STATISTICS_PAGE = "metricsstatistics";
    private ProcessExecution processExecution;

    public MetricsController(CodeRepoMetricsService codeRepoMetricsService, CodeRepoService codeRepoService) {
        this.codeRepoMetricsService = codeRepoMetricsService;
        this.codeRepoService = codeRepoService;
    }

    @GetMapping("/all")
    public String getAll(Model model,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        try {
            List<RepoExampleMetrics> repoExampleMetrics;
            Pageable paging = PageRequest.of(page - 1, size);

            Page<RepoExampleMetrics> pageTuts;
            if (keyword == null) {
                pageTuts = codeRepoMetricsService.findAll(paging);
            } else {
                pageTuts = codeRepoMetricsService.findByRepoExamples(keyword, paging);
                model.addAttribute("keyword", keyword);
            }

            repoExampleMetrics = pageTuts.getContent();

            model.addAttribute("reposMetrics", repoExampleMetrics);
            model.addAttribute("currentPage", pageTuts.getNumber() + 1);
            model.addAttribute("totalItems", pageTuts.getTotalElements());
            model.addAttribute("totalPages", pageTuts.getTotalPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return LIST_PAGE;
    }

    @GetMapping("/generation")
    public String getMetricsGenerationPage(Model model){
        model.addAttribute("processing", processExecution != null && processExecution.isRunning());

        model.addAttribute("repoExampleMetrics", codeRepoMetricsService.getMetricsForCodeRepos(true));
        model.addAttribute("minedRepoMetrics", codeRepoMetricsService.getMetricsForCodeRepos(false));
        return GENERATE_PAGE;
    }


    @GetMapping("/example/start")
    @ResponseBody
    public String startMetricsGeneration(){
        if(processExecution == null || !processExecution.isRunning()){
            processExecution = codeRepoMetricsService.createProcessExecution("example");
            codeRepoMetricsService.runMetricsExtraction(processExecution, codeRepoService.findByProcessedFalse());
            return "started";
        }
        return "There can be only one process executing at a time due to GitHub API requests per minute limitation. \nThere is a process of type " + processExecution.getProcessType() + " in execution.";
    }

    @GetMapping("/mined/start")
    @ResponseBody
    public String startMinedMetricsGeneration(){
        if(processExecution == null || !processExecution.isRunning()){
            processExecution = codeRepoMetricsService.createProcessExecution("mined");
            codeRepoMetricsService.runMetricsExtraction(processExecution, codeRepoService.findByProcessedFalse());
            return "started";
        }
        return "There can be only one process executing at a time due to GitHub API requests per minute limitation. \nThere is a process of type" + processExecution.getProcessType() + " in execution.";
    }

    @GetMapping("/generation/status")
    @ResponseBody
    public ProcessExecutionDto getMetricsGenerationStatus(){
        if(processExecution == null) return ProcessExecutionDto.builder().build();
        processExecution = codeRepoMetricsService.getProcessExecutionById(processExecution.getId());
        return ProcessExecutionDto.builder()
                .message(processExecution.getMessage())
                .running(processExecution.isRunning())
                .processedItems(processExecution.getProcessedItems())
                .type(processExecution.getProcessType())
                .totalItems(processExecution.getTotalItems())
                .error(processExecution.isError())
                .build();
    }


    @GetMapping("/statistics")
    public String getMetricsStatisticsPage(Model model){
        MetricsStatisticsDto metricsStatisticsDto = codeRepoMetricsService.getMetricsStatistics(true);
        model.addAttribute("msMetrics", metricsStatisticsDto);
        metricsStatisticsDto = codeRepoMetricsService.getMetricsStatistics(false);
        model.addAttribute("moMetrics", metricsStatisticsDto);
        return STATISTICS_PAGE;
    }


}
