package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class ProcessExecution {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String processType;

    private long totalItems;

    private long processedItems;

    private boolean running;

    private boolean finished;

    private boolean error;

    private String message;
}
