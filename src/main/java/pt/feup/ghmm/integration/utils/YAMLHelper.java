package pt.feup.ghmm.integration.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.thymeleaf.util.StringUtils;
import pt.feup.ghmm.integration.dtos.DockerComposeDto;

public class YAMLHelper {

    public static DockerComposeDto yamlToDto (String content){
        if(StringUtils.isEmpty(content)) return null;
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(content, DockerComposeDto.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
