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
import pt.feup.ghmm.metrics.services.RepoExampleMetricsService;
import pt.feup.ghmm.metrics.services.RepoExampleService;

import java.util.List;

@Controller
@RequestMapping("/metrics")
public class MetricsController {

    private RepoExampleMetricsService repoExampleMetricsService;
    private RepoExampleService repoExampleService;
    private final String LIST_PAGE = "metricslist";
    private final String GENERATE_PAGE = "metricsgeneration";
    private final String STATISTICS_PAGE = "metricsstatistics";
    private ProcessExecution processExecution;

    public MetricsController(RepoExampleMetricsService repoExampleMetricsService, RepoExampleService repoExampleService) {
        this.repoExampleMetricsService = repoExampleMetricsService;
        this.repoExampleService = repoExampleService;
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
        long processedWithoutErrorTotal = repoExampleService.countAllByProcessedTrueAndProcessingErrorFalse();
        long processedWithErrorTotal = repoExampleService.countAllByProcessedTrueAndProcessingErrorTrue();
        long unprocessed = repoExampleService.countAllByProcessedFalse();
        model.addAttribute("examplesTotal", repoExampleService.countAll());
        model.addAttribute("processedWithoutErrorTotal", processedWithoutErrorTotal);
        model.addAttribute("processedWithErrorTotal", processedWithErrorTotal);
        model.addAttribute("unprocessedTotal", unprocessed);
        return GENERATE_PAGE;
    }

    @GetMapping("/generation/start")
    @ResponseBody
    public String startMetricsGeneration(){
        processExecution = repoExampleMetricsService.createProcessExecution();
        repoExampleMetricsService.runMetricsExtraction(processExecution, repoExampleService.findByProcessedFalse());
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
