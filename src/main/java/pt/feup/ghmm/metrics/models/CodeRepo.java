package pt.feup.ghmm.metrics.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
public class CodeRepo {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Url is mandatory")
    private String owner;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @NotBlank(message = "App is mandatory")
    private String appName;

    @NotBlank(message = "Url is mandatory")
    @Column(unique = true)
    private String url;

    private boolean processed;

    private boolean processingError;

    private String message;

    private double score;

    private String classification;

    private Date lastModified;
}
