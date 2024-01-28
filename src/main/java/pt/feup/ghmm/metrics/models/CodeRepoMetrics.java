package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.identification.models.Language;
import pt.feup.ghmm.identification.models.Service;

import java.util.Set;

import static jakarta.persistence.FetchType.EAGER;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Entity
public class CodeRepoMetrics {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Size (in KB) is mandatory")
    private long size;

    @NotNull(message = "DefaultLang flag is mandatory")
    @OneToOne
    private Language defaultLang;

    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "repo_metrics_languages",
            joinColumns = @JoinColumn(name = "repo_metrics_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id"))
    private Set<Language> languages;

    @ManyToMany(fetch = EAGER)
    @JoinTable(
            name = "repo_metrics_services",
            joinColumns = @JoinColumn(name = "repo_metrics_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<Service> services;

    @NotNull(message = "Number of files in the main language is mandatory")
    private String defaultBranch;

    @NotNull(message = "Number of files in the main language is mandatory")
    private long files;

    @NotNull(message = "Number of all contents (files + folders) is mandatory")
    private long allContentsNumber;

    @NotNull(message = "DatabaseServices count is mandatory")
    private long databaseServices;

    @NotNull(message = "LogServices count is mandatory")
    private long logServices;

    @NotNull(message = "MessagingServices count is mandatory")
    private long messagingServices;

    @NotNull(message = "Dockerfiles count is mandatory")
    private long dockerfiles;

    @NotNull(message = "Programming languages count is mandatory")
    private long programmingLanguages;

    @NotNull(message = "MicroserviceMention flag is mandatory")
    private boolean microserviceMention;

    @NotNull(message = "DatabaseConnection flag is mandatory")
    private boolean databaseConnection;

    @NotNull(message = "Dockerfile flag is mandatory")
    private boolean dockerfile;

    @NotNull(message = "Restful flag is mandatory")
    private boolean restful;

    @NotNull(message = "Soap flag is mandatory")
    private boolean soap;

    @NotNull(message = "Messaging flag is mandatory")
    private boolean messaging;

    @NotNull(message = "Logs Service flag is mandatory")
    private boolean logsService;
}
