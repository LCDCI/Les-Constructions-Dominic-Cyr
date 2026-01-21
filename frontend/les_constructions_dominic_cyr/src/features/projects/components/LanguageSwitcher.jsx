import React from 'react';
import PropTypes from 'prop-types';
import '../../../styles/Project/create-project.css';

const LanguageSwitcher = ({ currentLanguage }) => {
  return (
    <div className="language-switcher">
      <span
        className={`lang-indicator ${currentLanguage === 'fr' ? 'active' : ''}`}
      >
        Français
      </span>
      <span
        className={`lang-indicator ${currentLanguage === 'en' ? 'active' : ''}`}
      >
        English
      </span>
    </div>
  );
};

LanguageSwitcher.propTypes = {
  currentLanguage: PropTypes.oneOf(['en', 'fr']).isRequired,
};

export default LanguageSwitcher;
