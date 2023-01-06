package pt.feup.ghmm.metrics.dtos;

import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;
import pt.feup.ghmm.metrics.models.RepoExample;

import java.util.List;

@SuperBuilder
public class RepoExampleListDto extends ResponseMessage {
    List<RepoExample> examples;
}
