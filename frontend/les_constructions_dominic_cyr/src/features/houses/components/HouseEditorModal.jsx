import { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { useAuth0 } from '@auth0/auth0-react';
import { createHouse, updateHouse, uploadHouseImage } from '../api/housesApi';
import '../../../styles/MySitePage.css';

const emptyHouse = {
  projectIdentifier: '',
  houseName: '',
  location: '',
  description: '',
  imageIdentifier: '',
  numberOfRooms: '',
  numberOfBedrooms: '',
  numberOfBathrooms: '',
  constructionYear: '',
};

const filesBase = () =>
  import.meta.env.VITE_FILES_SERVICE_URL ||
  (typeof window !== 'undefined'
    ? `${window.location.origin}/files`
    : '/files');

export default function HouseEditorModal({
  house,
  projects,
  onClose,
  onSaved,
}) {
  const { getAccessTokenSilently } = useAuth0();
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
            projectIdentifier: projects[0]?.projectIdentifier || '',
          }
    );
    setImageFile(null);
    setRemoveExistingImage(false);
    setError('');
  }, [house, projects]);

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
        ...form,
        imageIdentifier,
        numberOfRooms:
          form.numberOfRooms === '' ? null : Number(form.numberOfRooms),
        numberOfBedrooms:
          form.numberOfBedrooms === '' ? null : Number(form.numberOfBedrooms),
        numberOfBathrooms:
          form.numberOfBathrooms === '' ? null : Number(form.numberOfBathrooms),
        constructionYear:
          form.constructionYear === '' ? null : Number(form.constructionYear),
      };
      const saved = house
        ? await updateHouse(house.houseIdentifier, payload, token)
        : await createHouse(payload, token);

      let finalHouse = saved;
      if (imageFile) {
        finalHouse = await uploadHouseImage(
          saved.houseIdentifier,
          imageFile,
          token
        );
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
            <p className="my-site-kicker">House model</p>
            <h2>{house ? 'Edit house' : 'Add house'}</h2>
          </div>
          <button
            type="button"
            className="my-site-modal-close"
            onClick={onClose}
            aria-label="Close"
          >
            ×
          </button>
        </div>

        {error && <p className="my-site-error">{error}</p>}
        <label>
          Project
          <select
            value={form.projectIdentifier}
            onChange={event =>
              updateField('projectIdentifier', event.target.value)
            }
            required
          >
            <option value="" disabled>
              Select a project
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
          House name
          <input
            value={form.houseName}
            onChange={event => updateField('houseName', event.target.value)}
            required
            maxLength={255}
          />
        </label>
        <label>
          Location
          <input
            value={form.location || ''}
            onChange={event => updateField('location', event.target.value)}
            maxLength={255}
          />
        </label>
        <label>
          Description
          <textarea
            value={form.description || ''}
            onChange={event => updateField('description', event.target.value)}
            rows={4}
            maxLength={100000}
          />
        </label>
        <div className="my-site-modal-fields">
          <label>
            Rooms <span className="my-site-optional">Optional</span>
            <input
              type="number"
              min="0"
              max="100"
              value={form.numberOfRooms ?? ''}
              onChange={event =>
                updateField('numberOfRooms', event.target.value)
              }
            />
          </label>
          <label>
            Bedrooms <span className="my-site-optional">Optional</span>
            <input
              type="number"
              min="0"
              max="100"
              value={form.numberOfBedrooms ?? ''}
              onChange={event =>
                updateField('numberOfBedrooms', event.target.value)
              }
            />
          </label>
          <label>
            Bathrooms <span className="my-site-optional">Optional</span>
            <input
              type="number"
              min="0"
              max="100"
              value={form.numberOfBathrooms ?? ''}
              onChange={event =>
                updateField('numberOfBathrooms', event.target.value)
              }
            />
          </label>
          <label>
            Year <span className="my-site-optional">Optional</span>
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
          House image
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
            Remove current image
          </button>
        )}

        <div className="my-site-modal-actions">
          <button
            type="button"
            className="my-site-modal-secondary"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="my-site-modal-primary"
            disabled={saving}
          >
            {saving ? 'Saving...' : 'Save house'}
          </button>
        </div>
      </form>
    </div>
  );
}

HouseEditorModal.propTypes = {
  house: PropTypes.object,
  projects: PropTypes.arrayOf(PropTypes.object).isRequired,
  onClose: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
};
