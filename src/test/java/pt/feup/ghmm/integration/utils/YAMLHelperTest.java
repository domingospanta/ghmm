package pt.feup.ghmm.integration.utils;

import org.junit.jupiter.api.Test;
import pt.feup.ghmm.integration.dtos.DockerComposeDto;

import static org.junit.jupiter.api.Assertions.*;

class YAMLHelperTest {

    @Test
    void yamlToDto()  {

        DockerComposeDto  dockerComposeDto = YAMLHelper.yamlToDto(null);
        assertNull( dockerComposeDto);

        String content =
                "version: \"1\"\n" +
                "services:\n" +
                "  service1:\n" +
                "    image: image1\n" +
                "  service2:\n" +
                "    image: image2";
        dockerComposeDto = YAMLHelper.yamlToDto(content);
        assertNotNull(dockerComposeDto);

        assertEquals("1", dockerComposeDto.getVersion());
        assertEquals(dockerComposeDto.getServices().size(), 2);
    }

}