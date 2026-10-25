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

    private static final Map<String, String> RENOVATIONS_ENGLISH = Map.ofEntries(
            Map.entry("hero.label", "Renovate, Modernize & Transform"),
            Map.entry("hero.heading", "Renovations"),
            Map.entry("hero.lede", "Your home deserves the very best"),
            Map.entry("intro.kicker", "Transform"),
            Map.entry("intro.title", "your home into the living space of your dreams!"),
            Map.entry("intro.description", "Whether it's modernizing your kitchen, completely rethinking your basement, opening up spaces, or increasing your property value, our team delivers your project with precision, style, and efficiency. We offer a turnkey service: planning, design, site management, and high-end finishing. Every detail is crafted to reflect your personality and improve your daily comfort. Entrust your renovation to passionate professionals!"),
            Map.entry("features.kicker", "What sets us apart"),
            Map.entry("features.title", "A renovation experience shaped around you"),
            Map.entry("features.feature1", "Passionate expertise"),
            Map.entry("features.feature1Description", "A dedicated team bringing care, experience, and energy to every renovation detail."),
            Map.entry("features.feature2", "Creative transformation"),
            Map.entry("features.feature2Description", "Thoughtful ideas that reveal the full potential of your existing space."),
            Map.entry("features.feature3", "Precision at every step"),
            Map.entry("features.feature3Description", "Careful planning and exact execution from the first sketch to the final finish."),
            Map.entry("features.feature4", "Comfortable, responsible choices"),
            Map.entry("features.feature4Description", "Materials and solutions selected to create lasting comfort while respecting your home."),
            Map.entry("features.feature5", "High-end finishing"),
            Map.entry("features.feature5Description", "Refined materials and craftsmanship that give your renovation a distinctive feel."),
            Map.entry("features.feature6", "A trusted partner"),
            Map.entry("features.feature6Description", "Clear support and communication throughout your turnkey renovation."),
            Map.entry("cta.kicker", "Talk to an expert"),
            Map.entry("cta.title", "Entrust your renovation to passionate professionals!"),
            Map.entry("cta.subtitle", "We offer a turnkey service tailored to your home, your lifestyle, and your ambitions."),
            Map.entry("cta.button", "Talk to an expert")
    );

    private static final Map<String, String> RENOVATIONS_FRENCH = Map.ofEntries(
            Map.entry("hero.label", "Rénover, Moderniser & transformer"),
            Map.entry("hero.heading", "Rénovations"),
            Map.entry("hero.lede", "Votre maison mérite ce qu'il y a de mieux"),
            Map.entry("intro.kicker", "Transformez"),
            Map.entry("intro.title", "votre maison en un espace de vie dont vous rêvez !"),
            Map.entry("intro.description", "Que ce soit pour moderniser votre cuisine, repenser entièrement votre sous-sol, ouvrir des espaces ou augmenter la valeur de votre propriété, notre équipe réalise votre projet avec précision, style et efficacité. Nous offrons un service clé en main : planification, design, gestion de chantier et finition haut de gamme. Chaque détail est pensé pour refléter votre personnalité et améliorer votre confort au quotidien. Confiez votre rénovation à des professionnels passionnés !"),
            Map.entry("features.kicker", "Ce qui nous distingue"),
            Map.entry("features.title", "Une expérience de rénovation à votre image"),
            Map.entry("features.feature1", "Une expertise passionnée"),
            Map.entry("features.feature1Description", "Une équipe engagée qui met son expérience et son savoir-faire au service de chaque détail."),
            Map.entry("features.feature2", "Une transformation créative"),
            Map.entry("features.feature2Description", "Des idées réfléchies qui révèlent tout le potentiel de votre espace existant."),
            Map.entry("features.feature3", "La précision à chaque étape"),
            Map.entry("features.feature3Description", "Une planification attentive et une exécution précise, de la première esquisse à la finition."),
            Map.entry("features.feature4", "Des choix responsables et confortables"),
            Map.entry("features.feature4Description", "Des matériaux et des solutions choisis pour un confort durable, dans le respect de votre maison."),
            Map.entry("features.feature5", "Une finition haut de gamme"),
            Map.entry("features.feature5Description", "Des matériaux raffinés et un travail soigné qui donnent une identité unique à votre rénovation."),
            Map.entry("features.feature6", "Un partenaire de confiance"),
            Map.entry("features.feature6Description", "Un accompagnement clair et une communication attentive pendant toute votre rénovation clé en main."),
            Map.entry("cta.kicker", "Parlez à un expert"),
            Map.entry("cta.title", "Confiez votre rénovation à des professionnels passionnés !"),
            Map.entry("cta.subtitle", "Nous offrons un service clé en main adapté à votre maison, à votre quotidien et à vos ambitions."),
            Map.entry("cta.button", "Parlez à un expert")
    );

    private static final Map<String, String> RENOVATIONS_LEGACY_ENGLISH = Map.ofEntries(
            Map.entry("intro.kicker", "A fresh start for your home"),
            Map.entry("intro.title", "Renovations designed around your life"),
            Map.entry("intro.description", "From the first idea to the final finish, we transform existing spaces into homes that feel more functional, comfortable, and truly yours. Our team coordinates each renovation with care, clear communication, and respect for your daily life."),
            Map.entry("features.kicker", "What guides our work"),
            Map.entry("features.title", "The difference is in the details"),
            Map.entry("features.feature1", "A team you can trust"),
            Map.entry("features.feature1Description", "Experienced professionals who keep your renovation moving and your expectations clear."),
            Map.entry("features.feature2", "Spaces that work better"),
            Map.entry("features.feature2Description", "Thoughtful layouts that bring more light, comfort, and function to your home."),
            Map.entry("features.feature3", "Careful site coordination"),
            Map.entry("features.feature3Description", "A structured approach that keeps trades, timelines, and decisions aligned."),
            Map.entry("features.feature4", "Respect for your home"),
            Map.entry("features.feature4Description", "Clean, considerate work that protects your space throughout the renovation."),
            Map.entry("features.feature5", "Finishes made to last"),
            Map.entry("features.feature5Description", "Quality materials and precise craftsmanship for results you can enjoy for years."),
            Map.entry("features.feature6", "Support from start to finish"),
            Map.entry("features.feature6Description", "A responsive partner before, during, and after your renovation.")
    );

    private static final Map<String, String> RENOVATIONS_LEGACY_FRENCH = Map.ofEntries(
            Map.entry("intro.kicker", "Un nouveau départ pour votre maison"),
            Map.entry("intro.title", "Des rénovations pensées pour votre quotidien"),
            Map.entry("intro.description", "De la première idée à la dernière finition, nous transformons les espaces existants en lieux plus fonctionnels, confortables et à votre image. Notre équipe coordonne chaque rénovation avec soin, transparence et respect de votre quotidien."),
            Map.entry("features.kicker", "Ce qui guide notre travail"),
            Map.entry("features.title", "La différence se trouve dans les détails"),
            Map.entry("features.feature1", "Une équipe de confiance"),
            Map.entry("features.feature1Description", "Des professionnels expérimentés qui font avancer votre rénovation et clarifient chaque étape."),
            Map.entry("features.feature2", "Des espaces plus fonctionnels"),
            Map.entry("features.feature2Description", "Des aménagements réfléchis pour apporter plus de lumière, de confort et de fonctionnalité."),
            Map.entry("features.feature3", "Une coordination attentive"),
            Map.entry("features.feature3Description", "Une approche structurée qui harmonise les métiers, les échéanciers et les décisions."),
            Map.entry("features.feature4", "Le respect de votre maison"),
            Map.entry("features.feature4Description", "Des travaux propres et soignés qui protègent votre espace pendant la rénovation."),
            Map.entry("features.feature5", "Des finitions durables"),
            Map.entry("features.feature5Description", "Des matériaux de qualité et un savoir-faire précis pour des résultats durables."),
            Map.entry("features.feature6", "Un accompagnement complet"),
            Map.entry("features.feature6Description", "Un partenaire disponible avant, pendant et après votre rénovation.")
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
        seedRenovations("en", RENOVATIONS_ENGLISH, RENOVATIONS_LEGACY_ENGLISH);
        seedRenovations("fr", RENOVATIONS_FRENCH, RENOVATIONS_LEGACY_FRENCH);
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

    private void seedRenovations(
            String language,
            Map<String, String> values,
            Map<String, String> legacyValues
    ) {
        IntStream.range(0, values.size()).forEach(index -> {
            String key = values.keySet().stream().skip(index).findFirst().orElseThrow();
            repository.findByPageGroupAndPageKeyAndLanguage("renovations", key, language)
                    .ifPresentOrElse(content -> {
                        if (legacyValues.containsKey(key)
                                && legacyValues.get(key).equals(content.getContentText())) {
                            content.setContentText(values.get(key));
                            repository.save(content);
                        }
                    }, () -> repository.save(SiteContent.builder()
                            .pageGroup("renovations")
                            .pageKey(key)
                            .language(language)
                            .contentText(values.get(key))
                            .sortOrder(index)
                            .build()));
        });
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
