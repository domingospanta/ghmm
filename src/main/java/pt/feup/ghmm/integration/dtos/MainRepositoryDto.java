package pt.feup.ghmm.integration.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MainRepositoryDto {
    private String name;

    @JsonProperty("html_url")
    private String url;

    private String description;

    private long size;

    private String language;

    private List<String> topics;

    @JsonProperty("default_branch")
    private String defaultBranch;
}
