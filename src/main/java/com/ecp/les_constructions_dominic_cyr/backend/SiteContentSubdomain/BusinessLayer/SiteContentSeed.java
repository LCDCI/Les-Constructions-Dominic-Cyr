package com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer.SiteContent;
import com.ecp.les_constructions_dominic_cyr.backend.SiteContentSubdomain.DataAccessLayer.SiteContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SiteContentSeed implements ApplicationRunner {
    private static final Map<String, String> HOME_MEDIA = Map.ofEntries(
            Map.entry("media.hero", "0313008f-b07c-4392-824f-45ff11a2d7a3"),
            Map.entry("media.live", "1681b3d5-8f0a-4daf-9590-53a1ce37cf20"),
            Map.entry("media.build", "1659ff85-b160-4111-b419-84834eb4375a"),
            Map.entry("media.think", "1634e9ee-2680-41d1-b28a-47353f842d9c"),
            Map.entry("media.foresta", "a93f9fbc-44d6-4c0d-b763-0523ee42656d"),
            Map.entry("media.project2", "bb6dd250-ed32-4041-8b4e-020e2ef45e2f"),
            Map.entry("media.collage1", "48f50cea-f368-41d6-91c3-ae55157bd868"),
            Map.entry("media.collage2", "55378cf7-c0a0-48be-b5f9-9d2507eff177"),
            Map.entry("media.collage3", "610354b2-8a9c-4e87-95a1-3cc63f494c6e"),
            Map.entry("media.panorama", "47d2b619-70cf-460c-b929-605d52ee9eb6"),
            Map.entry("media.contact", "dff685b4-05a5-443a-9c84-848fa9dbd905")
    );

    private static final Map<String, String> HOME_ENGLISH = Map.ofEntries(
            Map.entry("hero.label", "DESIGN & BUILD"),
            Map.entry("hero.title", "Crafting Your Dream Space"),
            Map.entry("hero.subtitle", "Quality construction, timeless design, since 2006."),
            Map.entry("think.title", "intelligent, practical and comfortable spaces"),
            Map.entry("think.subtitle", "Passionate about architecture and design, our mission is to provide you with a unique and memorable experience."),
            Map.entry("build.title", "a strong relationship and quality partnership"),
            Map.entry("build.subtitle", "Driven by our desire for perfection, we do everything to ensure you are more than satisfied with our work."),
            Map.entry("live.title", "in a space that reflects your values"),
            Map.entry("live.subtitle", "Your home is one of the most important investments of your life. Choose reliability and lasting quality."),
            Map.entry("features.title", "Personalized Support"),
            Map.entry("features.intro", "Dominic Cyr Construction Inc. is a dynamic family business with over 30 years of experience in residential construction. We are here to support you in realizing your ideas and projects with personalized, professional service built on transparency."),
            Map.entry("portfolio.title", "Projects & Achievements")
    );

    private static final Map<String, String> HOME_FRENCH = Map.ofEntries(
            Map.entry("hero.label", "CONCEPTION & CONSTRUCTION"),
            Map.entry("hero.title", "Créer l'espace de vos rêves"),
            Map.entry("hero.subtitle", "Construction de qualité, design intemporel, depuis 2006"),
            Map.entry("think.title", "Pensez des espaces intelligents, pratiques et confortables"),
            Map.entry("think.subtitle", "Passionnés par l'architecture et le design, notre mission est de vous offrir une expérience unique et mémorable."),
            Map.entry("build.title", "Construisez une relation solide et un partenariat de qualité"),
            Map.entry("build.subtitle", "Guidés par notre désir de perfection, nous faisons tout pour que vous soyez plus que satisfait de nos travaux."),
            Map.entry("live.title", "Nous collaborons avec vous à la réalisation de vos rêves !"),
            Map.entry("live.subtitle", "Votre maison constitue l'un des plus importants investissements que vous ferez dans votre vie. Optez pour une valeur sûre et sans pareil avec Les Constructions Dominic Cyr Inc."),
            Map.entry("features.title", "Un accompagnement sur mesure"),
            Map.entry("features.intro", "Les Constructions Dominic Cyr Inc. est une entreprise familiale et dynamique qui compte plus de 20 ans d'expérience dans le domaine de la construction résidentielle. Nous sommes là pour vous accompagner dans la réalisation de vos idées et de vos projets, en assurant un service personnalisé et professionnel misant sur la transparence."),
            Map.entry("portfolio.title", "Projets et réalisations")
    );

    private static final Map<String, String> ENGLISH_FOOTER = Map.ofEntries(
            Map.entry("information", "Information"),
            Map.entry("phone", "514-705-7848"),
            Map.entry("email", "constructions.dcyr@gmail.com"),
            Map.entry("accreditations", "Accreditations"),
            Map.entry("rbqLabel", "Régie du Bâtiment du Québec"),
            Map.entry("rbq", "8356-0169-03"),
            Map.entry("rbqUrl", "https://www.pes.rbq.gouv.qc.ca/RegistreLicences/FicheDetenteur/8356016903?mode=Entreprise"),
            Map.entry("gcrLabel", "Residential Construction Guarantee"),
            Map.entry("gcr", "11084"),
            Map.entry("gcrUrl", "https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html"),
            Map.entry("copyright", "© {{year}} Les Constructions Dominic Cyr Inc. All rights reserved.")
    );

    private static final Map<String, String> FRENCH_FOOTER = Map.ofEntries(
            Map.entry("information", "Informations"),
            Map.entry("phone", "514-705-7848"),
            Map.entry("email", "constructions.dcyr@gmail.com"),
            Map.entry("accreditations", "Accréditations"),
            Map.entry("rbqLabel", "Régie du Bâtiment du Québec"),
            Map.entry("rbq", "8356-0169-03"),
            Map.entry("rbqUrl", "https://www.pes.rbq.gouv.qc.ca/RegistreLicences/FicheDetenteur/8356016903?mode=Entreprise"),
            Map.entry("gcrLabel", "Garantie Construction Résidentielle"),
            Map.entry("gcr", "11084"),
            Map.entry("gcrUrl", "https://repertoire.garantiegcr.com/entrepreneurs/8356-0169-03.html"),
            Map.entry("copyright", "© {{year}} Les Constructions Dominic Cyr Inc. Tous droits réservés.")
    );

    private final SiteContentRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        seedLanguage("en", ENGLISH_FOOTER);
        seedLanguage("fr", FRENCH_FOOTER);
        seedHome("en", HOME_ENGLISH);
        seedHome("fr", HOME_FRENCH);
        seedHomeMedia();
    }

    private void seedLanguage(String language, Map<String, String> values) {
        IntStream.range(0, values.size()).forEach(index -> {
            String key = values.keySet().stream().skip(index).findFirst().orElseThrow();
            if (repository.findByPageGroupAndPageKeyAndLanguage("footer", key, language).isEmpty()) {
                repository.save(SiteContent.builder()
                        .pageGroup("footer")
                        .pageKey(key)
                        .language(language)
                        .contentText(values.get(key))
                        .sortOrder(index)
                        .build());
            }
        });
    }

    private void seedHome(String language, Map<String, String> values) {
        seedEntries("home", language, values, false);
    }

    private void seedHomeMedia() {
        seedEntries("home", "en", HOME_MEDIA, true);
        seedEntries("home", "fr", HOME_MEDIA, true);
    }

    private void seedEntries(
            String pageGroup,
            String language,
            Map<String, String> values,
            boolean imageValues
    ) {
        IntStream.range(0, values.size()).forEach(index -> {
            String key = values.keySet().stream().skip(index).findFirst().orElseThrow();
            if (repository.findByPageGroupAndPageKeyAndLanguage(pageGroup, key, language).isEmpty()) {
                repository.save(SiteContent.builder()
                        .pageGroup(pageGroup)
                        .pageKey(key)
                        .language(language)
                        .contentText(imageValues ? null : values.get(key))
                        .imageIdentifier(imageValues ? values.get(key) : null)
                        .sortOrder(index)
                        .build());
            }
        });
    }
}
