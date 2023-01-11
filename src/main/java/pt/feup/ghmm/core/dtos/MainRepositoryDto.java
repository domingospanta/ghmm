package pt.feup.ghmm.core.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import pt.feup.ghmm.core.response.ResponseMessage;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MainRepositoryDto {
    private String name;

    private String description;

    private long size;

    private String language;

    private List<String> topics;

    @JsonProperty("default_branch")
    private String defaultBranch;
}
