package pt.feup.ghmm.core.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DockerComposeDto {

    private String version;

    private Map<String, ServiceDto> services;

}
