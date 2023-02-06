package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
public class Threshold {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Metric is mandatory")
    private String metric;

    @NotNull(message = "Microservice flag is mandatory")
    private boolean microservice;

    private double thresholdValue;
}
