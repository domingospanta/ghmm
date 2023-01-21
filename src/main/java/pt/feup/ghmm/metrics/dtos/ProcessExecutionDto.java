package pt.feup.ghmm.metrics.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ProcessExecutionDto {

    private long totalItems;

    private long processedItems;

    private boolean running;

    private boolean error;

    private String message;
}
