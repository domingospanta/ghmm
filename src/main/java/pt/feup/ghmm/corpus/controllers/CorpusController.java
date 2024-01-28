package pt.feup.ghmm.corpus.controllers;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.feup.ghmm.metrics.dtos.RepoResult;
import pt.feup.ghmm.repo.models.RepoMined;
import pt.feup.ghmm.repo.services.CodeRepoService;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/repo/corpus")
public class CorpusController {

    private CodeRepoService codeRepoService;

    @GetMapping
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

        return "repoMinedList";
    }

    @GetMapping("/delete/{id}")
    public String deleteMinedRepo(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        RepoMined repoMined = codeRepoService.findRepoMinedById(id);
        if(repoMined != null){
            RepoResult repoResult = codeRepoService.delete(repoMined);
            redirectAttributes.addFlashAttribute("result", repoResult);
        }

        return "redirect:/repo/corpus";
    }
}
