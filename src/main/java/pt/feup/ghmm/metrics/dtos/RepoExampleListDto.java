package pt.feup.ghmm.metrics.dtos;

import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.integration.response.ResponseMessage;
import pt.feup.ghmm.repo.models.RepoExample;

import java.util.List;

@SuperBuilder
public class RepoExampleListDto extends ResponseMessage {
    List<RepoExample> examples;
}
