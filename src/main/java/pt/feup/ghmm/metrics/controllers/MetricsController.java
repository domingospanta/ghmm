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
import pt.feup.ghmm.metrics.services.CodeRepoService;
import pt.feup.ghmm.metrics.services.RepoExampleMetricsService;

import java.util.List;

@Controller
@RequestMapping("/metrics")
public class MetricsController {

    private RepoExampleMetricsService repoExampleMetricsService;
    private CodeRepoService codeRepoService;
    private final String LIST_PAGE = "metricslist";
    private final String GENERATE_PAGE = "metricsgeneration";
    private final String STATISTICS_PAGE = "metricsstatistics";
    private ProcessExecution processExecution;

    public MetricsController(RepoExampleMetricsService repoExampleMetricsService, CodeRepoService codeRepoService) {
        this.repoExampleMetricsService = repoExampleMetricsService;
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
                pageTuts = repoExampleMetricsService.findAll(paging);
            } else {
                pageTuts = repoExampleMetricsService.findByRepoExamples(keyword, paging);
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
        long processedWithoutErrorTotal = codeRepoService.countAllByProcessedTrueAndProcessingErrorFalse();
        long processedWithErrorTotal = codeRepoService.countAllByProcessedTrueAndProcessingErrorTrue();
        long unprocessed = codeRepoService.countAllByProcessedFalse();
        model.addAttribute("examplesTotal", codeRepoService.countAll());
        model.addAttribute("processedWithoutErrorTotal", processedWithoutErrorTotal);
        model.addAttribute("processedWithErrorTotal", processedWithErrorTotal);
        model.addAttribute("unprocessedTotal", unprocessed);
        return GENERATE_PAGE;
    }

    @GetMapping("/generation/start")
    @ResponseBody
    public String startMetricsGeneration(){
        processExecution = repoExampleMetricsService.createProcessExecution();
        repoExampleMetricsService.runMetricsExtraction(processExecution, codeRepoService.findByProcessedFalse());
        return "started";
    }

    @GetMapping("/generation/status")
    @ResponseBody
    public ProcessExecutionDto getMetricsGenerationStatus(){
        if(processExecution == null) return ProcessExecutionDto.builder().build();
        processExecution = repoExampleMetricsService.getProcessExecutionById(processExecution.getId());
        return ProcessExecutionDto.builder()
                .message(processExecution.getMessage())
                .running(processExecution.isRunning())
                .processedItems(processExecution.getProcessedItems())
                .totalItems(processExecution.getTotalItems())
                .error(processExecution.isError())
                .build();
    }


    @GetMapping("/statistics")
    public String getMetricsStatisticsPage(Model model){
        MetricsStatisticsDto metricsStatisticsDto = repoExampleMetricsService.getMetricsStatistics(true);
        model.addAttribute("msMetrics", metricsStatisticsDto);
        metricsStatisticsDto = repoExampleMetricsService.getMetricsStatistics(false);
        model.addAttribute("moMetrics", metricsStatisticsDto);
        return STATISTICS_PAGE;
    }


}
