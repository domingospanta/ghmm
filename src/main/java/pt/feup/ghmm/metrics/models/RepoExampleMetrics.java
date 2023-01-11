package pt.feup.ghmm.metrics.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
public class RepoExampleMetrics {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "RepoExample is mandatory")
    @OneToOne
    private RepoExample repoExample;

    @NotNull(message = "Size (in KB) is mandatory")
    private long size;

    @NotNull(message = "DefaultLang flag is mandatory")
    @OneToOne
    private Language defaultLang;

    @OneToMany
    private Set<Language> languages;

    @NotNull(message = "Number of files in the main language is mandatory")
    private long files;

    @NotNull(message = "Number of all contents (files + folders) is mandatory")
    private long allContentsNumber;

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
