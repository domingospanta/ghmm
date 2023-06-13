package pt.feup.ghmm.metrics.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import pt.feup.ghmm.identification.models.ProcessExecution;
import pt.feup.ghmm.metrics.dtos.MetricsStatisticsDto;
import pt.feup.ghmm.metrics.dtos.ProcessExecutionDto;
import pt.feup.ghmm.metrics.models.CodeRepoMetrics;
import pt.feup.ghmm.metrics.services.CodeRepoMetricsService;
import pt.feup.ghmm.repo.services.CodeRepoService;

import java.util.List;

import static pt.feup.ghmm.integration.utils.Constants.EXAMPLE_PROCESS_TYPE;
import static pt.feup.ghmm.integration.utils.Constants.MINED_PROCESS_TYPE;

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

    @GetMapping("/all/{processType}")
    public String getAll(Model model,
                         @PathVariable("processType") String processType,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size) {
        try {
            List<? extends CodeRepoMetrics> codeRepoMetrics;
            Pageable paging = PageRequest.of(page - 1, size);

            Page<? extends CodeRepoMetrics> pageTuts;
            if (keyword == null) {
                pageTuts = codeRepoMetricsService.findAll(paging, processType);
            } else {
                pageTuts = codeRepoMetricsService.findAllByProcessType(keyword, paging, processType);
                model.addAttribute("keyword", keyword);
            }

            codeRepoMetrics = pageTuts.getContent();

            model.addAttribute("processType", processType);
            model.addAttribute("reposMetrics", codeRepoMetrics);
            model.addAttribute("currentPage", pageTuts.getNumber() + 1);
            model.addAttribute("totalItems", pageTuts.getTotalElements());
            model.addAttribute("totalPages", pageTuts.getTotalPages());
            model.addAttribute("pageSize", size);
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
        }

        return LIST_PAGE;
    }

    @GetMapping("/generation/{processType}")
    public String getMetricsGenerationPage(@PathVariable("processType") String processType,  Model model){
        model.addAttribute("processing", processExecution != null && processExecution.isRunning());
        model.addAttribute("processType", processType);
        if(EXAMPLE_PROCESS_TYPE.equalsIgnoreCase(processType)){
            model.addAttribute("repoExampleMetrics", codeRepoMetricsService.getMetricsForCodeRepos(true));
        }else {
            model.addAttribute("minedRepoMetrics", codeRepoMetricsService.getMetricsForCodeRepos(false));
        }
        return GENERATE_PAGE;
    }


    @GetMapping("/example/start")
    @ResponseBody
    public String startMetricsGeneration(){
        if(processExecution == null || !processExecution.isRunning()){
            processExecution = codeRepoMetricsService.createProcessExecution(EXAMPLE_PROCESS_TYPE);
            codeRepoMetricsService.runMetricsExtraction(processExecution, codeRepoService.findByProcessedFalse(EXAMPLE_PROCESS_TYPE));
            return "started";
        }
        return "There can be only one process executing at a time due to GitHub API requests per minute limitation. \nThere is a process of type " + processExecution.getProcessType() + " in execution.";
    }

    @GetMapping("/mined/start")
    @ResponseBody
    public String startMinedMetricsGeneration(){
        if(processExecution == null || !processExecution.isRunning()){
            processExecution = codeRepoMetricsService.createProcessExecution(MINED_PROCESS_TYPE);
            codeRepoMetricsService.runMetricsExtraction(processExecution, codeRepoService.findByProcessedFalse(MINED_PROCESS_TYPE));
            return "started";
        }
        return "There can be only one process executing at a time due to GitHub API requests per minute limitation. \nThere is a process of type " + processExecution.getProcessType() + " in execution.";
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
