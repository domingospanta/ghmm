package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class RepoMetricsDto extends ResponseMessage {
    private long total;

    private long processedWithoutError;

    private long processedWithError;

    private long unprocessed;
}
