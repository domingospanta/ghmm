package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.integration.response.ResponseMessage;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class BulkCodeRepoResultDto extends ResponseMessage {
    List<RepoResult> resultMap;
    boolean error;
    String message;
}


