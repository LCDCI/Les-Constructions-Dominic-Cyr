export class TranslationPage {
  constructor(page) {
    this.page = page;
    
    // Language toggle button
    this.languageButton = page.locator('.btn-language');
    this.mobileLanguageButton = page.locator('.mobile-actions .btn-language');
    
    // Navigation items (these should be translated)
    this.navHome = page.locator('nav a[href="/"]');
    this.navProjects = page.locator('nav a[href="/residential-projects"]');
    this.navRenovation = page.locator('nav a[href="/renovations"]');
    this.navProjectManagement = page.locator('nav a[href="/projectmanagement"]');
    this.navRealisations = page.locator('nav a[href="/realizations"], nav a[href="/realisations"]');
    this.navContact = page.locator('nav a[href="/contact"]');
    
    // Mobile navigation
    this.mobileNav = page.locator('.mobile-nav');
    this.mobileMenuToggle = page.locator('.mobile-menu-toggle');
    
    // Home page elements (for testing page content translation)
    this.heroHeading = page.locator('.hero-heading');
    this.heroDescription = page.locator('.hero-description');
    this.heroLabel = page.locator('.hero-label');
    
    // Footer (if it has translations)
    this.footer = page.locator('.footer, footer');
  }

  async goto(path = '/') {
    await this.page.goto(path);
    await this.page.waitForLoadState('networkidle');
  }

  async waitForTranslationsToLoad() {
    // Wait for network to be idle to ensure translations are loaded
    await this.page.waitForLoadState('networkidle');
    // Give a small delay for i18n to update
    await this.page.waitForTimeout(500);
  }

  async getCurrentLanguage() {
    // Check the language button text to determine current language
    // Try desktop button first, then mobile button
    let buttonText = null;
    const desktopButtonCount = await this.languageButton.count();
    
    if (desktopButtonCount > 0) {
      try {
        const isVisible = await this.languageButton.isVisible({ timeout: 2000 }).catch(() => false);
        if (isVisible) {
          buttonText = await this.languageButton.textContent();
        }
      } catch (e) {
        // Button might not be visible, try mobile
      }
    }
    
    // If desktop button didn't work, try mobile button
    if (!buttonText) {
      const mobileButtonCount = await this.mobileLanguageButton.count();
      if (mobileButtonCount > 0) {
        try {
          const isVisible = await this.mobileLanguageButton.isVisible({ timeout: 2000 }).catch(() => false);
          if (isVisible) {
            buttonText = await this.mobileLanguageButton.textContent();
          }
        } catch (e) {
          // Mobile button also not available
        }
      }
    }
    
    // If button shows "EN", we're in French. If it shows "FR", we're in English.
    // Default to 'en' if we can't determine
    return buttonText?.trim() === 'EN' ? 'fr' : 'en';
  }

  async toggleLanguage() {
    const buttonCount = await this.languageButton.count();
    if (buttonCount > 0) {
      const isVisible = await this.languageButton.isVisible();
      if (isVisible) {
        await this.languageButton.click();
        await this.waitForTranslationsToLoad();
      }
    }
  }

  async toggleLanguageMobile() {
    // Open mobile menu first if needed
    const isMobileMenuOpen = await this.mobileNav.evaluate(
      el => el.classList.contains('open')
    );
    if (!isMobileMenuOpen) {
      await this.mobileMenuToggle.click();
      await this.page.waitForTimeout(300);
    }
    await this.mobileLanguageButton.click();
    await this.waitForTranslationsToLoad();
  }

  async setLanguage(language) {
    // Try desktop button first
    const buttonCount = await this.languageButton.count();
    if (buttonCount > 0) {
      const isVisible = await this.languageButton.isVisible().catch(() => false);
      if (isVisible) {
        const currentLang = await this.getCurrentLanguage();
        if (currentLang !== language) {
          await this.toggleLanguage();
          // Wait a bit extra to ensure language change is processed
          await this.page.waitForTimeout(500);
        }
        return;
      }
    }
    
    // Try mobile button if desktop didn't work
    const mobileButtonCount = await this.mobileLanguageButton.count();
    if (mobileButtonCount > 0) {
      const isMobileVisible = await this.mobileLanguageButton.isVisible().catch(() => false);
      if (isMobileVisible) {
        const currentLang = await this.getCurrentLanguage();
        if (currentLang !== language) {
          await this.toggleLanguageMobile();
          await this.page.waitForTimeout(500);
        }
      }
    }
  }

  async getNavText(selector) {
    const count = await selector.count();
    if (count > 0) {
      return await selector.first().textContent();
    }
    return null;
  }

  async getCookieLanguage() {
    const cookies = await this.page.context().cookies();
    const langCookie = cookies.find(c => c.name === 'language' || c.name === 'i18next');
    return langCookie ? langCookie.value : null;
  }

  async getLocalStorageLanguage() {
    return await this.page.evaluate(() => {
      return localStorage.getItem('i18nextLng') || localStorage.getItem('language');
    });
  }

  // Helper to check if text contains expected translation
  async verifyTextContains(selector, expectedTexts) {
    const text = await selector.textContent();
    const textLower = text?.toLowerCase() || '';
    return expectedTexts.some(expected => 
      textLower.includes(expected.toLowerCase())
    );
  }
}
