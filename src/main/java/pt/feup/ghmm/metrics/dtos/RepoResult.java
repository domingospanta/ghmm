package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RepoResult {
    String repo;
    String message;
    boolean error;
}