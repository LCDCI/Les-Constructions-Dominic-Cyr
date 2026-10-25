import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useAuth0 } from '@auth0/auth0-react';
import { usePageTranslations } from '../../../hooks/usePageTranslations';
import { createHouse, updateHouse } from '../api/housesApi';
import { uploadFile, deleteFile } from '../../files/api/filesApi';
import '../../../styles/MySitePage.css';

const emptyHouse = {
  projectIdentifier: '',
  houseName: '',
  location: '',
  description: '',
  descriptionEn: '',
  descriptionFr: '',
  imageIdentifier: '',
  numberOfRooms: '',
  numberOfBedrooms: '',
  numberOfBathrooms: '',
  constructionYear: '',
};

const nullableNumber = value => {
  if (value === '' || value == null) return null;
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
};

const filesBase = () =>
  import.meta.env.VITE_FILES_SERVICE_URL ||
  (typeof window !== 'undefined'
    ? `${window.location.origin}/files`
    : '/files');

export default function HouseEditorModal({
  house,
  projects,
  initialProjectIdentifier,
  onClose,
  onSaved,
}) {
  const { t } = usePageTranslations('mySite');
  const { getAccessTokenSilently, user } = useAuth0();
  const [form, setForm] = useState(emptyHouse);
  const [imageFile, setImageFile] = useState(null);
  const [removeExistingImage, setRemoveExistingImage] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    setForm(
      house
        ? { ...emptyHouse, ...house }
        : {
            ...emptyHouse,
            projectIdentifier:
              initialProjectIdentifier || projects[0]?.projectIdentifier || '',
          }
    );
    setImageFile(null);
    setRemoveExistingImage(false);
    setError('');
  }, [house, initialProjectIdentifier, projects]);

  const updateField = (field, value) =>
    setForm(current => ({ ...current, [field]: value }));

  const getToken = async () => {
    const { getAuthAudience } = await import('../../../utils/authConfig');
    return getAccessTokenSilently({
      authorizationParams: { audience: getAuthAudience() },
    });
  };

  const imageUrl = form.imageIdentifier
    ? String(form.imageIdentifier).startsWith('http')
      ? form.imageIdentifier
      : `${filesBase().replace(/\/$/, '')}/files/${form.imageIdentifier}`
    : '';

  const handleSubmit = async event => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const token = await getToken();
      let imageIdentifier = removeExistingImage
        ? null
        : form.imageIdentifier || null;
      if (imageFile) {
        // A new house is created first so the backend can scope the file to its house ID.
        if (house) imageIdentifier = null;
      }

      const payload = {
        projectIdentifier: form.projectIdentifier,
        houseName: form.houseName,
        location: form.location,
        descriptionEn: form.descriptionEn || null,
        descriptionFr: form.descriptionFr || null,
        imageIdentifier,
        description:
          form.descriptionEn || form.descriptionFr || form.description || null,
        numberOfRooms: nullableNumber(form.numberOfRooms),
        numberOfBedrooms: nullableNumber(form.numberOfBedrooms),
        numberOfBathrooms: nullableNumber(form.numberOfBathrooms),
        constructionYear: nullableNumber(form.constructionYear),
      };
      const saved = house
        ? await updateHouse(house.houseIdentifier, payload, token)
        : await createHouse(payload, token);

      let finalHouse = saved;
      if (imageFile) {
        const formData = new FormData();
        formData.append('file', imageFile);
        formData.append('category', 'PHOTO');
        formData.append('projectId', saved.projectIdentifier);
        formData.append('uploadedBy', user?.sub || 'house-editor');
        formData.append('uploaderRole', 'OWNER');

        const uploaded = await uploadFile(formData, token);
        const imageIdentifier = uploaded.fileId || uploaded.id;
        if (!imageIdentifier) {
          throw new Error('Image upload returned no file identifier.');
        }

        finalHouse = await updateHouse(
          saved.houseIdentifier,
          { ...payload, imageIdentifier },
          token
        );

        if (
          house?.imageIdentifier &&
          house.imageIdentifier !== imageIdentifier &&
          !String(house.imageIdentifier).startsWith('http')
        ) {
          try {
            await deleteFile(house.imageIdentifier, {
              deletedBy: user?.sub || 'house-editor',
              token,
            });
          } catch {
            // Preserve the successful replacement if the old object is missing.
          }
        }
      }
      onSaved(finalHouse);
    } catch (saveError) {
      setError(saveError.message || 'Unable to save this house.');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      className="my-site-modal-backdrop"
      role="presentation"
      onMouseDown={event => event.target === event.currentTarget && onClose()}
    >
      <form className="my-site-modal" onSubmit={handleSubmit}>
        <div className="my-site-modal-header">
          <div>
            <p className="my-site-kicker">{t('houseModel', 'House model')}</p>
            <h2>
              {house
                ? t('editHouseTitle', 'Edit house')
                : t('addHouseTitle', 'Add house')}
            </h2>
          </div>
          <button
            type="button"
            className="my-site-modal-close"
            onClick={onClose}
            aria-label={t('close', 'Close')}
          >
            ×
          </button>
        </div>

        {error && <p className="my-site-error">{error}</p>}
        <label>
          {t('houseProject', 'Project')}
          <select
            value={form.projectIdentifier}
            onChange={event =>
              updateField('projectIdentifier', event.target.value)
            }
            required
          >
            <option value="" disabled>
              {t('selectProject', 'Select a project')}
            </option>
            {projects.map(project => (
              <option
                key={project.projectIdentifier}
                value={project.projectIdentifier}
              >
                {project.projectName}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t('houseName', 'House name')}
          <input
            value={form.houseName}
            onChange={event => updateField('houseName', event.target.value)}
            required
            maxLength={255}
          />
        </label>
        <label>
          {t('houseLocation', 'Location')}
          <input
            value={form.location || ''}
            onChange={event => updateField('location', event.target.value)}
            maxLength={255}
          />
        </label>
        <label>
          {t('houseDescriptionLegacy', 'Description (fallback)')}
          <textarea
            value={form.description || ''}
            onChange={event => updateField('description', event.target.value)}
            rows={4}
            maxLength={100000}
          />
        </label>
        <label>
          {t('houseDescriptionEnglish', 'Description (English)')}
          <textarea
            value={form.descriptionEn || ''}
            onChange={event => updateField('descriptionEn', event.target.value)}
            rows={4}
            maxLength={100000}
          />
        </label>
        <label>
          {t('houseDescriptionFrench', 'Description (French)')}
          <textarea
            value={form.descriptionFr || ''}
            onChange={event => updateField('descriptionFr', event.target.value)}
            rows={4}
            maxLength={100000}
          />
        </label>
        <div className="my-site-modal-fields">
          <label>
            {t('houseRooms', 'Rooms')}{' '}
            <span className="my-site-optional">
              {t('optional', 'Optional')}
            </span>
            <input
              type="number"
              min="0"
              max="100"
              step="0.1"
              value={form.numberOfRooms ?? ''}
              onChange={event =>
                updateField('numberOfRooms', event.target.value)
              }
            />
          </label>
          <label>
            {t('houseBedrooms', 'Bedrooms')}{' '}
            <span className="my-site-optional">
              {t('optional', 'Optional')}
            </span>
            <input
              type="number"
              min="0"
              max="100"
              step="0.1"
              value={form.numberOfBedrooms ?? ''}
              onChange={event =>
                updateField('numberOfBedrooms', event.target.value)
              }
            />
          </label>
          <label>
            {t('houseBathrooms', 'Bathrooms')}{' '}
            <span className="my-site-optional">
              {t('optional', 'Optional')}
            </span>
            <input
              type="number"
              min="0"
              max="100"
              step="0.1"
              value={form.numberOfBathrooms ?? ''}
              onChange={event =>
                updateField('numberOfBathrooms', event.target.value)
              }
            />
          </label>
          <label>
            {t('houseYear', 'Year')}{' '}
            <span className="my-site-optional">
              {t('optional', 'Optional')}
            </span>
            <input
              type="number"
              min="1800"
              max="2500"
              value={form.constructionYear ?? ''}
              onChange={event =>
                updateField('constructionYear', event.target.value)
              }
            />
          </label>
        </div>

        {imageUrl && !removeExistingImage && (
          <img
            className="my-site-modal-image"
            src={imageUrl}
            alt={form.houseName || 'House'}
          />
        )}
        <label>
          {t('houseImage', 'House image')}
          <input
            type="file"
            accept="image/png,image/jpeg,image/webp"
            onChange={event => {
              setImageFile(event.target.files?.[0] || null);
              setRemoveExistingImage(false);
            }}
          />
        </label>
        {form.imageIdentifier && !removeExistingImage && (
          <button
            type="button"
            className="my-site-modal-link danger"
            onClick={() => setRemoveExistingImage(true)}
          >
            {t('removeHouseImage', 'Remove current image')}
          </button>
        )}

        <div className="my-site-modal-actions">
          <button
            type="button"
            className="my-site-modal-secondary"
            onClick={onClose}
          >
            {t('cancel', 'Cancel')}
          </button>
          <button
            type="submit"
            className="my-site-modal-primary"
            disabled={saving}
          >
            {saving ? t('saving', 'Saving...') : t('saveHouse', 'Save house')}
          </button>
        </div>
      </form>
    </div>
  );
}

HouseEditorModal.propTypes = {
  house: PropTypes.object,
  projects: PropTypes.arrayOf(PropTypes.object).isRequired,
  initialProjectIdentifier: PropTypes.string,
  onClose: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
};
