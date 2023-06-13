package pt.feup.ghmm.metrics.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pt.feup.ghmm.identification.models.Language;
import pt.feup.ghmm.identification.repositories.LanguageRepository;
import pt.feup.ghmm.identification.services.LanguageService;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureWebClient
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LanguageService.class})
class LanguageServiceTest {

    @MockBean
    private LanguageRepository languageRepository;

    @Autowired
    private LanguageService languageService;


    @Test
    void findOrCreateByName() {
        Language language = languageService.findOrCreateByName("Java");
        assertNotNull(language);
        assertTrue(language.isProgrammingLanguage());
    }
}