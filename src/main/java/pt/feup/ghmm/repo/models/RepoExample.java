package pt.feup.ghmm.repo.models;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
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
public class RepoExample extends CodeRepo {

    @NotNull(message = "Microservice flag is mandatory")
    private boolean microservice;

    @NotNull(message = "Microservice flag is mandatory")
    private boolean microserviceSet;

    private Date createdDate;
}
