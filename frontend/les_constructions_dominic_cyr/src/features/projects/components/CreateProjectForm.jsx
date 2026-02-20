import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';
import { useAuth0 } from '@auth0/auth0-react';
import useBackendUser from '../../../hooks/useBackendUser';
import LotSelector from './LotSelector';
import { projectApi } from '../api/projectApi';
import { uploadFile } from '../../files/api/filesApi';
import { createLot } from '../../lots/api/lots';
import '../../../styles/Project/create-project.css';

// Hardcoded translations
const translations = {
  en: {
    pageTitle: 'Create New Project',
    form: {
      sections: {
        basicInfo: 'Basic Information',
        statusDates: 'Status & Dates',
        colors: 'Colors',
        customerBuyer: 'Customer & Buyer',
        progress: 'Progress',
        coverImage: 'Project Cover Image',
        lots: 'Add Lots to Project',
      },
      fields: {
        projectName: 'Project Name',
        projectDescription: 'Project Description',
        status: 'Status',
        startDate: 'Start Date',
        endDate: 'End Date',
        location: 'Location',
        primaryColor: 'Primary Color',
        tertiaryColor: 'Secondary Color',
        buyerColor: 'Accent Color',
        // Removed in UI: buyerName, customerId
        progressPercentage: 'Progress Percentage',
        coverPhoto: 'Cover Photo',
      },
      placeholders: {
        projectName: 'Enter project name',
        projectDescription: 'Enter project description',
        location: 'Enter project location',
        // Removed in UI: buyerName, customerId
        coverPhoto: 'Choose an image file...',
      },
      buttons: {
        createProject: 'Create Project',
        cancel: 'Cancel',
        submitting: 'Creating...',
        fillOutEnglish: 'Fill out English',
        continue: 'Continue',
        continueToLots: 'Continue to add lots',
        back: 'Back',
        chooseImage: 'Choose Image',
        changeImage: 'Change Image',
        removeImage: 'Remove Image',
      },
      labels: {
        noFileChosen: 'No file chosen',
      },
      steps: {
        one: 'Step 1 of 3',
        two: 'Step 2 of 3',
        three: 'Step 3 of 3',
      },
      validation: {
        required: 'This field is required',
        endDateAfterStart: 'End date must be after start date',
        invalidImageType: 'Invalid image type. Allowed: PNG, JPG, JPEG, WEBP',
      },
      messages: {
        fillOutEnglish: 'Please fill out the English version of the form',
        missingFields:
          'Some required fields are missing. Please complete the highlighted fields.',
      },
    },
  },
  fr: {
    pageTitle: 'Créer un nouveau projet',
    form: {
      sections: {
        basicInfo: 'Informations de base',
        statusDates: 'Statut et dates',
        colors: 'Couleurs',
        customerBuyer: 'Client et acheteur',
        progress: 'Progression',
        coverImage: 'Image de couverture du projet',
        lots: 'Ajouter des lots au projet',
      },
      fields: {
        projectName: 'Nom du projet',
        projectDescription: 'Description du projet',
        status: 'Statut',
        startDate: 'Date de début',
        endDate: 'Date de fin',
        location: 'Emplacement',
        primaryColor: 'Couleur principale',
        tertiaryColor: 'Couleur secondaire',
        buyerColor: "Couleur d'accent",
        // Removed in UI: buyerName, customerId
        progressPercentage: 'Pourcentage de progression',
        coverPhoto: 'Photo de couverture',
      },
      placeholders: {
        projectName: 'Entrez le nom du projet',
        projectDescription: 'Entrez la description du projet',
        location: "Entrez l'emplacement du projet",
        // Removed in UI: buyerName, customerId
        coverPhoto: 'Choisissez une image...',
      },
      buttons: {
        createProject: 'Créer le projet',
        cancel: 'Annuler',
        submitting: 'Création en cours...',
        fillOutEnglish: "Remplir l'anglais",
        continue: 'Continuer',
        continueToLots: 'Continuer vers les lots',
        back: 'Retour',
        chooseImage: 'Choisir une image',
        changeImage: "Changer l'image",
        removeImage: "Supprimer l'image",
      },
      labels: {
        noFileChosen: 'Aucun fichier choisi',
      },
      steps: {
        one: 'Étape 1 sur 3',
        two: 'Étape 2 sur 3',
        three: 'Étape 3 sur 3',
      },
      validation: {
        required: 'Ce champ est requis',
        endDateAfterStart: 'La date de fin doit être après la date de début',
        invalidImageType:
          "Type d'image invalide. Autorisés: PNG, JPG, JPEG, WEBP",
      },
      messages: {
        fillOutEnglish: 'Veuillez remplir la version anglaise du formulaire',
        missingFields:
          'Des champs obligatoires sont manquants. Veuillez compléter les champs en surbrillance.',
      },
    },
  },
};

const CreateProjectForm = ({ onCancel, onSuccess, onError }) => {
  const { getAccessTokenSilently, user } = useAuth0();
  const { role } = useBackendUser();
  // Start with French form first
  const [currentLanguage, setCurrentLanguage] = useState('fr');

  // Get translations based on current language
  const t = key => {
    const keys = key.split('.');
    let value = translations[currentLanguage];
    for (const k of keys) {
      value = value?.[k];
    }
    return value || key;
  };

  // Bilingual form state
  const [formData, setFormData] = useState({
    // English fields
    projectNameEn: '',
    projectDescriptionEn: '',
    // buyerNameEn removed

    // French fields
    projectNameFr: '',
    projectDescriptionFr: '',
    // buyerNameFr removed

    // Location (bilingual: entered on FR step and EN step)
    locationFr: '',
    locationEn: '',
    // Common fields (only on French step)
    status: 'PLANNED',
    startDate: '',
    endDate: '',
    primaryColor: '#4A90A4',
    tertiaryColor: '#33FF57',
    buyerColor: '#3357FF',
    // customerId removed
    progressPercentage: 0,
    lotIdentifiers: [],
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [coverImageFile, setCoverImageFile] = useState(null);
  const [coverImagePreviewUrl, setCoverImagePreviewUrl] = useState(null);
  const [animateLangSwitch, setAnimateLangSwitch] = useState(false);
  const formContentRef = useRef(null);
  const coverImageInputRef = useRef(null);
  const [validationAlert, setValidationAlert] = useState(null);

  // Lot creation form state - lifted to parent to persist across language switches
  const [lotFormData, setLotFormData] = useState({
    lotNumber: '',
    civicAddress: '',
    dimensionsSquareFeet: '',
    dimensionsSquareMeters: '',
    price: '',
    lotStatus: 'AVAILABLE',
    assignedCustomerId: '',
  });
  const [showLotCreateForm, setShowLotCreateForm] = useState(false);

  // Draft lots: created on step 3 without a project; created via API after project is saved
  const [draftLots, setDraftLots] = useState([]);

  // Three-step wizard: 0 = French (no lots), 1 = English (no lots), 2 = Lots only
  const [step, setStep] = useState(0);

  // Language is auto-switched, no manual change needed

  const handleInputChange = (field, value) => {
    setFormData(prev => ({
      ...prev,
      [field]: value,
    }));
    // Clear error for this field
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const handleBilingualInputChange = (field, value) => {
    const fieldName = currentLanguage === 'en' ? `${field}En` : `${field}Fr`;
    handleInputChange(fieldName, value);
  };

  const getBilingualValue = field => {
    const fieldName = currentLanguage === 'en' ? `${field}En` : `${field}Fr`;
    return formData[fieldName] || '';
  };

  // Check if French form is complete - ALL required fields must be filled
  const isFrenchComplete = () => {
    return (
      formData.projectNameFr.trim() !== '' &&
      formData.projectDescriptionFr.trim() !== '' &&
      (formData.locationFr ?? formData.location ?? '').trim() !== '' &&
      formData.status !== '' &&
      formData.startDate !== '' &&
      formData.primaryColor !== '' &&
      formData.tertiaryColor !== '' &&
      formData.buyerColor !== ''
    );
  };

  // Check if English form is complete - only translatable fields (name, description, location)
  const isEnglishComplete = () => {
    return (
      formData.projectNameEn.trim() !== '' &&
      formData.projectDescriptionEn.trim() !== '' &&
      (formData.locationEn ?? '').trim() !== ''
    );
  };

  const validateForm = () => {
    const newErrors = {};

    // Validate bilingual fields - both languages required
    if (!formData.projectNameEn.trim()) {
      newErrors.projectNameEn = t('form.validation.required');
    }
    if (!formData.projectNameFr.trim()) {
      newErrors.projectNameFr = t('form.validation.required');
    }
    if (!formData.projectDescriptionEn.trim()) {
      newErrors.projectDescriptionEn = t('form.validation.required');
    }
    if (!formData.projectDescriptionFr.trim()) {
      newErrors.projectDescriptionFr = t('form.validation.required');
    }
    if (!(formData.locationFr ?? '').trim()) {
      newErrors.locationFr = t('form.validation.required');
    }
    if (!(formData.locationEn ?? '').trim()) {
      newErrors.locationEn = t('form.validation.required');
    }

    // Validate common fields
    if (!formData.startDate) {
      newErrors.startDate = t('form.validation.required');
    }
    if (
      formData.endDate &&
      formData.startDate &&
      formData.endDate < formData.startDate
    ) {
      newErrors.endDate = t('form.validation.endDateAfterStart');
    }
    if (!formData.primaryColor) {
      newErrors.primaryColor = t('form.validation.required');
    }
    if (!formData.tertiaryColor) {
      newErrors.tertiaryColor = t('form.validation.required');
    }
    if (!formData.buyerColor) {
      newErrors.buyerColor = t('form.validation.required');
    }
    // customerId no longer required

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleCoverImageChange = file => {
    if (!file) {
      setCoverImageFile(null);
      setCoverImagePreviewUrl(null);
      return;
    }

    const validTypes = ['image/png', 'image/jpeg', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setErrors(prev => ({
        ...prev,
        coverImage: t('form.validation.invalidImageType'),
      }));
      setCoverImageFile(null);
      setCoverImagePreviewUrl(null);
      return;
    }

    setErrors(prev => {
      const e = { ...prev };
      delete e.coverImage;
      return e;
    });

    setCoverImageFile(file);
    const url = URL.createObjectURL(file);
    setCoverImagePreviewUrl(url);
  };

  const uploadTranslationFiles = async projectIdentifier => {
    // Use project identifier for filenames as requested
    const baseName = projectIdentifier;

    // Create translation JSON files
    const translationEn = {
      projectName: formData.projectNameEn,
      projectDescription: formData.projectDescriptionEn,
      location: (formData.locationEn ?? '').trim() || '',
    };

    const translationFr = {
      projectName: formData.projectNameFr,
      projectDescription: formData.projectDescriptionFr,
      location: (formData.locationFr ?? '').trim() || '',
    };

    // Convert to JSON strings
    const jsonEn = JSON.stringify(translationEn, null, 2);
    const jsonFr = JSON.stringify(translationFr, null, 2);

    // Create Blob objects
    const blobEn = new Blob([jsonEn], { type: 'application/json' });
    const blobFr = new Blob([jsonFr], { type: 'application/json' });

    // Create File objects
    const fileEn = new File([blobEn], `${baseName}_en.json`, {
      type: 'application/json',
    });
    const fileFr = new File([blobFr], `${baseName}_fr.json`, {
      type: 'application/json',
    });

    // Upload to file service
    // Store as DOCUMENT under the project's folder in MinIO
    // Files-service (deployed) requires uploadedBy and uploaderRole
    const uploadedBy = user?.sub ?? '';
    const uploaderRoleVal =
      role && role.trim() !== '' ? role.trim().toUpperCase() : 'OWNER';

    const formDataEn = new FormData();
    formDataEn.append('file', fileEn);
    formDataEn.append('category', 'DOCUMENT');
    formDataEn.append('projectId', projectIdentifier);
    formDataEn.append('uploadedBy', uploadedBy);
    formDataEn.append('uploaderRole', uploaderRoleVal);

    const formDataFr = new FormData();
    formDataFr.append('file', fileFr);
    formDataFr.append('category', 'DOCUMENT');
    formDataFr.append('projectId', projectIdentifier);
    formDataFr.append('uploadedBy', uploadedBy);
    formDataFr.append('uploaderRole', uploaderRoleVal);

    try {
      const [resultEn, resultFr] = await Promise.all([
        uploadFile(formDataEn),
        uploadFile(formDataFr),
      ]);

      return {
        enFileId: resultEn.fileId || resultEn.id,
        frFileId: resultFr.fileId || resultFr.id,
      };
    } catch (error) {
      console.error('Error uploading translation files:', error);
      throw new Error('Failed to upload translation files');
    }
  };

  const handleSwitchToEnglish = () => {
    setStep(1);
    setAnimateLangSwitch(true);
    setCurrentLanguage('en');
    /* No scroll-to-top: step container has min-height matching Lots step so layout stays stable. */
  };

  const handleSubmit = async e => {
    e.preventDefault();

    if (!validateForm()) {
      setValidationAlert(t('form.messages.missingFields'));
      // Scroll the form to top so the alert is visible
      setTimeout(() => {
        const getScrollableParent = node => {
          let el = node;
          while (el && el !== document.body) {
            const style = window.getComputedStyle(el);
            const oy = style.overflowY;
            if (
              (oy === 'auto' || oy === 'scroll') &&
              el.scrollHeight > el.clientHeight
            ) {
              return el;
            }
            el = el.parentElement;
          }
          return window;
        };
        const target = formContentRef.current;
        const parent = target ? getScrollableParent(target) : window;
        try {
          if (parent === window) {
            window.scrollTo({ top: 0, behavior: 'smooth' });
          } else {
            parent.scrollTo({ top: 0, behavior: 'smooth' });
          }
        } catch (_) {
          if (parent === window) {
            window.scrollTo(0, 0);
          } else {
            parent.scrollTop = 0;
          }
        }
        requestAnimationFrame(() => {
          try {
            if (parent === window) {
              window.scrollTo({ top: 0, behavior: 'smooth' });
            } else {
              parent.scrollTo({ top: 0, behavior: 'smooth' });
            }
          } catch (_) {}
        });
      }, 0);
      return;
    }

    setIsSubmitting(true);
    if (onError) {
      onError(null);
    }

    try {
      // TODO: Translation file upload will be implemented later
      // For now, we'll skip translation file upload and focus on project creation
      // Translation files can be uploaded separately after project is created

      // Prepare project data for backend
      // TODO: Backend needs to be updated to accept bilingual fields
      // For now, we'll send only the fields the backend expects (using English as default)
      // The translation files are uploaded separately and stored in file service
      const projectData = {
        projectName: formData.projectNameEn, // Use English as default for now
        projectDescription: formData.projectDescriptionEn, // Use English as default for now
        status: formData.status,
        startDate: formData.startDate,
        endDate: formData.endDate || null,
        location: (formData.locationEn ?? '').trim() || null,
        primaryColor: formData.primaryColor,
        tertiaryColor: formData.tertiaryColor,
        buyerColor: formData.buyerColor,
        // buyerName removed
        imageIdentifier: null,
        // customerId removed
        lotIdentifiers: formData.lotIdentifiers || [],
        progressPercentage: formData.progressPercentage,
      };

      // Note: Translation files will be uploaded separately after project creation
      // Backend will need to be updated to store references to translation files

      // Get auth token
      const token = await getAccessTokenSilently({
        authorizationParams: {
          audience:
            import.meta.env.VITE_AUTH0_AUDIENCE ||
            'https://construction-api.loca',
        },
      });

      const createdProject = await projectApi.createProject(projectData, token);

      const nonBlockingErrors = [];

      // If a cover image was selected, upload it and update the project
      if (coverImageFile && createdProject?.projectIdentifier) {
        try {
          const form = new FormData();
          form.append('file', coverImageFile);
          form.append('category', 'PHOTO');
          form.append('projectId', createdProject.projectIdentifier);
          // Files-service (deployed) requires uploadedBy and uploaderRole
          form.append('uploadedBy', user?.sub ?? '');
          form.append(
            'uploaderRole',
            role && role.trim() !== '' ? role.trim().toUpperCase() : 'OWNER'
          );

          const uploadResult = await uploadFile(form);
          const uploadedId = uploadResult.fileId || uploadResult.id;

          if (uploadedId) {
            await projectApi.updateProject(
              createdProject.projectIdentifier,
              {
                imageIdentifier: uploadedId,
              },
              token
            );
          }
        } catch (uploadErr) {
          console.error('Cover image upload failed:', uploadErr);
          const raw = uploadErr?.message || '';
          const friendly =
            raw.includes('404') || raw.includes('status code 404')
              ? 'Cover image could not be uploaded (file service unavailable).'
              : raw || 'Cover image upload failed';
          nonBlockingErrors.push(friendly);
        }
      }

      // Upload translation files (EN/FR) to files service under project folder
      if (createdProject?.projectIdentifier) {
        try {
          await uploadTranslationFiles(createdProject.projectIdentifier);
        } catch (translationErr) {
          console.error('Translation upload failed:', translationErr);
          // Non-blocking: project remains created even if translation upload fails
        }
      }

      // Create draft lots (added on step 3 before project existed) for the new project
      if (
        createdProject?.projectIdentifier &&
        Array.isArray(draftLots) &&
        draftLots.length > 0
      ) {
        for (const draft of draftLots) {
          try {
            const lotPayload = {
              lotNumber: String(draft.lotNumber ?? ''),
              civicAddress: String(draft.civicAddress ?? ''),
              dimensionsSquareFeet: String(draft.dimensionsSquareFeet ?? ''),
              dimensionsSquareMeters: String(
                draft.dimensionsSquareMeters ?? ''
              ),
              price:
                draft.price != null && draft.price !== ''
                  ? Number(draft.price)
                  : null,
              lotStatus: draft.lotStatus || 'AVAILABLE',
              assignedUserIds: draft.assignedCustomerId
                ? [draft.assignedCustomerId]
                : [],
            };
            await createLot({
              projectIdentifier: createdProject.projectIdentifier,
              lotData: lotPayload,
              token,
            });
          } catch (lotErr) {
            console.error('Draft lot creation failed:', draft, lotErr);
            const raw = lotErr?.message || 'Lot creation failed';
            const friendly =
              raw.includes('404') || raw.includes('status code 404')
                ? 'One or more lots could not be created (service unavailable).'
                : raw;
            const display = `Lot "${draft.civicAddress || draft.lotNumber || '?'}": ${friendly}`;
            if (!nonBlockingErrors.some(e => e.includes(friendly))) {
              nonBlockingErrors.push(display);
            }
          }
        }
      }

      // Show any non-blocking errors in the persistent banner (parent keeps modal closed)
      if (nonBlockingErrors.length > 0 && onError) {
        onError(
          'Project created. Some issues: ' + nonBlockingErrors.join(' — ')
        );
      }

      if (onSuccess && createdProject && createdProject.projectIdentifier) {
        onSuccess(createdProject.projectIdentifier);
      } else {
        throw new Error(
          'Invalid response from server: missing projectIdentifier'
        );
      }
    } catch (error) {
      console.error('Error creating project:', error);
      const errorMessage =
        error.message || 'Failed to create project. Please try again.';
      if (onError) {
        onError(errorMessage);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // No auto-switch - user must click the button to switch to English

  return (
    <form className="create-project-form" onSubmit={handleSubmit}>
      <div
        className="language-switcher step-indicator"
        role="tablist"
        aria-label="Form step"
      >
        <span className={`lang-indicator ${step === 0 ? 'active' : ''}`}>
          Français
        </span>
        <span className={`lang-indicator ${step === 1 ? 'active' : ''}`}>
          English
        </span>
        <span className={`lang-indicator ${step === 2 ? 'active' : ''}`}>
          Lots
        </span>
      </div>

      {validationAlert && (
        <div className="validation-alert" role="alert">
          {validationAlert}
        </div>
      )}

      {/* Step 0: French — Step 1: English (no lots on either). Same min-height as Lots step to avoid layout jump. */}
      {step < 2 && (
        <div
          key={`lang-${currentLanguage}-${animateLangSwitch ? 'anim' : 'static'}`}
          className={`create-project-step-fields${animateLangSwitch && currentLanguage === 'en' ? ' slide-up-enter' : ''}`}
          ref={formContentRef}
          onAnimationEnd={() => setAnimateLangSwitch(false)}
        >
          {/* Step 0: French — all fields. Step 1: English — only name, description, location */}
          <div className="form-section">
            <h2>{t('form.sections.basicInfo')}</h2>

            {step === 0 && (
              <>
                <div className="form-group">
                  <label htmlFor="projectNameFr">
                    {t('form.fields.projectName')} *
                  </label>
                  <input
                    type="text"
                    id="projectNameFr"
                    value={formData.projectNameFr}
                    onChange={e =>
                      handleInputChange('projectNameFr', e.target.value)
                    }
                    placeholder={t('form.placeholders.projectName')}
                    className={errors.projectNameFr ? 'error' : ''}
                  />
                  {errors.projectNameFr && (
                    <span className="error-message">
                      {errors.projectNameFr}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="projectDescriptionFr">
                    {t('form.fields.projectDescription')} *
                  </label>
                  <textarea
                    id="projectDescriptionFr"
                    value={formData.projectDescriptionFr}
                    onChange={e =>
                      handleInputChange('projectDescriptionFr', e.target.value)
                    }
                    placeholder={t('form.placeholders.projectDescription')}
                    rows={5}
                    className={errors.projectDescriptionFr ? 'error' : ''}
                  />
                  {errors.projectDescriptionFr && (
                    <span className="error-message">
                      {errors.projectDescriptionFr}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="locationFr">
                    {t('form.fields.location')} *
                  </label>
                  <input
                    type="text"
                    id="locationFr"
                    value={formData.locationFr}
                    onChange={e =>
                      handleInputChange('locationFr', e.target.value)
                    }
                    placeholder={t('form.placeholders.location')}
                    maxLength={255}
                    className={errors.locationFr ? 'error' : ''}
                  />
                  {errors.locationFr && (
                    <span className="error-message">{errors.locationFr}</span>
                  )}
                </div>
              </>
            )}

            {step === 1 && (
              <>
                <div className="form-group">
                  <label htmlFor="projectNameEn">
                    {t('form.fields.projectName')} *
                  </label>
                  <input
                    type="text"
                    id="projectNameEn"
                    value={formData.projectNameEn}
                    onChange={e =>
                      handleInputChange('projectNameEn', e.target.value)
                    }
                    placeholder={t('form.placeholders.projectName')}
                    className={errors.projectNameEn ? 'error' : ''}
                  />
                  {errors.projectNameEn && (
                    <span className="error-message">
                      {errors.projectNameEn}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="projectDescriptionEn">
                    {t('form.fields.projectDescription')} *
                  </label>
                  <textarea
                    id="projectDescriptionEn"
                    value={formData.projectDescriptionEn}
                    onChange={e =>
                      handleInputChange('projectDescriptionEn', e.target.value)
                    }
                    placeholder={t('form.placeholders.projectDescription')}
                    rows={5}
                    className={errors.projectDescriptionEn ? 'error' : ''}
                  />
                  {errors.projectDescriptionEn && (
                    <span className="error-message">
                      {errors.projectDescriptionEn}
                    </span>
                  )}
                </div>

                <div className="form-group">
                  <label htmlFor="locationEn">
                    {t('form.fields.location')} *
                  </label>
                  <input
                    type="text"
                    id="locationEn"
                    value={formData.locationEn}
                    onChange={e =>
                      handleInputChange('locationEn', e.target.value)
                    }
                    placeholder={t('form.placeholders.location')}
                    maxLength={255}
                    className={errors.locationEn ? 'error' : ''}
                  />
                  {errors.locationEn && (
                    <span className="error-message">{errors.locationEn}</span>
                  )}
                </div>
              </>
            )}
          </div>

          {/* Status & Dates, Colors, Cover — only on Step 0 (French) */}
          {step === 0 && (
            <>
              <div className="form-section">
                <h2>{t('form.sections.statusDates')}</h2>

                <div className="form-group">
                  <label htmlFor="status">{t('form.fields.status')} *</label>
                  <select
                    id="status"
                    value={formData.status}
                    onChange={e => handleInputChange('status', e.target.value)}
                  >
                    <option value="PLANNED">PLANNED</option>
                    <option value="IN_PROGRESS">IN_PROGRESS</option>
                    <option value="DELAYED">DELAYED</option>
                    <option value="COMPLETED">COMPLETED</option>
                    <option value="CANCELLED">CANCELLED</option>
                  </select>
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="startDate">
                      {t('form.fields.startDate')} *
                    </label>
                    <input
                      type="date"
                      id="startDate"
                      value={formData.startDate}
                      onChange={e =>
                        handleInputChange('startDate', e.target.value)
                      }
                      className={errors.startDate ? 'error' : ''}
                    />
                    {errors.startDate && (
                      <span className="error-message">{errors.startDate}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="endDate">{t('form.fields.endDate')}</label>
                    <input
                      type="date"
                      id="endDate"
                      value={formData.endDate}
                      onChange={e =>
                        handleInputChange('endDate', e.target.value)
                      }
                      min={formData.startDate}
                      className={errors.endDate ? 'error' : ''}
                    />
                    {errors.endDate && (
                      <span className="error-message">{errors.endDate}</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Colors Section */}
              <div className="form-section">
                <h3>{t('form.sections.colors')}</h3>

                <div className="form-row form-row-three-col">
                  <div className="form-group">
                    <label htmlFor="primaryColor">
                      {t('form.fields.primaryColor')} *
                    </label>
                    <input
                      type="color"
                      id="primaryColor"
                      value={formData.primaryColor}
                      onChange={e =>
                        handleInputChange('primaryColor', e.target.value)
                      }
                      className={errors.primaryColor ? 'error' : ''}
                    />
                    {errors.primaryColor && (
                      <span className="error-message">
                        {errors.primaryColor}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="tertiaryColor">
                      {t('form.fields.tertiaryColor')} *
                    </label>
                    <input
                      type="color"
                      id="tertiaryColor"
                      value={formData.tertiaryColor}
                      onChange={e =>
                        handleInputChange('tertiaryColor', e.target.value)
                      }
                      className={errors.tertiaryColor ? 'error' : ''}
                    />
                    {errors.tertiaryColor && (
                      <span className="error-message">
                        {errors.tertiaryColor}
                      </span>
                    )}
                  </div>

                  <div className="form-group">
                    <label htmlFor="buyerColor">
                      {t('form.fields.buyerColor')} *
                    </label>
                    <input
                      type="color"
                      id="buyerColor"
                      value={formData.buyerColor}
                      onChange={e =>
                        handleInputChange('buyerColor', e.target.value)
                      }
                      className={errors.buyerColor ? 'error' : ''}
                    />
                    {errors.buyerColor && (
                      <span className="error-message">{errors.buyerColor}</span>
                    )}
                  </div>
                </div>
              </div>

              {/* Cover Image Section */}
              <div className="form-section">
                <h2>{t('form.sections.coverImage')}</h2>
                <div className="form-group">
                  <label htmlFor="coverImage">
                    {t('form.fields.coverPhoto')}
                  </label>
                  <input
                    ref={coverImageInputRef}
                    type="file"
                    id="coverImage"
                    accept="image/png, image/jpeg, image/webp"
                    onChange={e => handleCoverImageChange(e.target.files?.[0])}
                    className="create-project-file-input-hidden"
                    tabIndex={-1}
                    aria-label={t('form.fields.coverPhoto')}
                  />
                  {!coverImageFile ? (
                    <div className="create-project-file-trigger-wrap">
                      <label
                        htmlFor="coverImage"
                        className="create-project-file-trigger"
                      >
                        {t('form.buttons.chooseImage')}
                      </label>
                      <span className="create-project-file-status">
                        {t('form.labels.noFileChosen')}
                      </span>
                    </div>
                  ) : (
                    <div className="form-group">
                      <img
                        src={coverImagePreviewUrl}
                        alt="Cover preview"
                        className="create-project-cover-preview"
                      />
                      <div className="create-project-cover-actions">
                        <button
                          type="button"
                          className="btn-cancel create-project-cover-btn"
                          onClick={() => coverImageInputRef.current?.click()}
                          disabled={isSubmitting}
                          aria-label={t('form.buttons.changeImage')}
                        >
                          {t('form.buttons.changeImage')}
                        </button>
                        <button
                          type="button"
                          className="btn-cancel create-project-cover-btn"
                          onClick={() => {
                            setCoverImageFile(null);
                            setCoverImagePreviewUrl(null);
                            if (coverImageInputRef.current) {
                              coverImageInputRef.current.value = '';
                            }
                          }}
                          disabled={isSubmitting}
                        >
                          {t('form.buttons.removeImage')}
                        </button>
                      </div>
                    </div>
                  )}
                  {errors.coverImage && (
                    <span className="error-message">{errors.coverImage}</span>
                  )}
                </div>
              </div>
            </>
          )}

          {/* Form Actions — Step 0: Fill out English | Step 1: Back + Continue to add lots */}
          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onCancel}
              disabled={isSubmitting}
            >
              {t('form.buttons.cancel')}
            </button>
            {step === 0 && (
              <button
                type="button"
                className="btn-submit"
                onClick={handleSwitchToEnglish}
                disabled={isSubmitting || !isFrenchComplete()}
              >
                {t('form.buttons.fillOutEnglish')}
              </button>
            )}
            {step === 1 && (
              <>
                <button
                  type="button"
                  className="btn-cancel"
                  onClick={() => {
                    setStep(0);
                    setCurrentLanguage('fr');
                  }}
                  disabled={isSubmitting}
                >
                  {t('form.buttons.back')}
                </button>
                <button
                  type="button"
                  className="btn-submit"
                  onClick={() => setStep(2)}
                  disabled={isSubmitting || !isEnglishComplete()}
                >
                  {t('form.buttons.continueToLots')}
                </button>
              </>
            )}
          </div>
        </div>
      )}

      {/* Step 2: Lots only — draft mode: no projectIdentifier; new lots created after project is saved */}
      {step === 2 && (
        <div className="form-section" ref={formContentRef}>
          <h2>{t('form.sections.lots')}</h2>
          <LotSelector
            currentLanguage={currentLanguage}
            selectedLots={formData.lotIdentifiers || []}
            onChange={lotIds => {
              handleInputChange('lotIdentifiers', lotIds || []);
            }}
            onLotCreated={newLotId => {
              setFormData(prev => {
                const current = prev.lotIdentifiers || [];
                if (newLotId && !current.includes(newLotId)) {
                  return { ...prev, lotIdentifiers: [...current, newLotId] };
                }
                return prev;
              });
            }}
            projectIdentifier={undefined}
            draftLots={draftLots}
            onDraftLotAdded={lotData =>
              setDraftLots(prev => [...prev, lotData])
            }
            onDraftLotRemoved={index =>
              setDraftLots(prev => prev.filter((_, i) => i !== index))
            }
            lotFormData={lotFormData}
            onLotFormDataChange={setLotFormData}
            showLotCreateForm={showLotCreateForm}
            onShowLotCreateFormChange={setShowLotCreateForm}
          />
          <div className="form-actions">
            <button
              type="button"
              className="btn-cancel"
              onClick={onCancel}
              disabled={isSubmitting}
            >
              {t('form.buttons.cancel')}
            </button>
            <button
              type="button"
              className="btn-cancel"
              onClick={() => setStep(1)}
              disabled={isSubmitting}
            >
              {t('form.buttons.back')}
            </button>
            <button
              type="submit"
              className="btn-submit"
              disabled={isSubmitting}
            >
              {isSubmitting
                ? t('form.buttons.submitting')
                : t('form.buttons.createProject')}
            </button>
          </div>
        </div>
      )}
    </form>
  );
};

CreateProjectForm.propTypes = {
  onCancel: PropTypes.func.isRequired,
  onSuccess: PropTypes.func.isRequired,
  onError: PropTypes.func.isRequired,
};

export default CreateProjectForm;
