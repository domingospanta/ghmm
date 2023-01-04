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
public class RepoExample {

    @Id
    @GeneratedValue
    private Long id;

    private String owner;

    private String name;

    @NotBlank(message = "Url is mandatory")
    @Column(unique = true)
    private String url;

    @NotNull(message = "Microservice flag is mandatory")
    private boolean microservice;
}
