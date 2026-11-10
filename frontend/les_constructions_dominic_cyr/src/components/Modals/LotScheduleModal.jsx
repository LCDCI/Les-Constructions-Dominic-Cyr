import React, { useEffect, useMemo, useRef, useState } from 'react';
import PropTypes from 'prop-types';
import { format, parseISO, addDays, isValid } from 'date-fns';
import { usePageTranslations } from '../../hooks/usePageTranslations';
import '../../styles/Project/ProjectSchedule.css';

const taskDefinitions = [
  [
    'purchasePromise',
    'Promesse d’achat signée',
    'Signed purchase promise',
    null,
    null,
    true,
  ],
  [
    'constructionPlans',
    'Plans de construction',
    'Construction plans',
    'purchasePromise',
    15,
  ],
  [
    'layoutPlan',
    'Plan d’implantation',
    'Site layout plan',
    'constructionPlans',
    10,
    false,
    'Arpenteur',
  ],
  [
    'gcrRegistration',
    'Inscription GCR',
    'GCR registration',
    'constructionPlans',
    20,
  ],
  [
    'clientConditions',
    'Levée des conditions du client',
    'Client conditions lifted',
    null,
    null,
    true,
  ],
  ['survey', 'Arpentage', 'Surveying', null, null, true, 'Arpenteur'],
  [
    'excavation',
    'Excavation',
    'Excavation',
    'survey',
    3,
    false,
    'Excavateur',
    2,
  ],
  [
    'foundations',
    'Fondations',
    'Foundations',
    'excavation',
    null,
    false,
    'Fondation',
    4,
  ],
  ['stone', 'Pierre', 'Stone', 'foundations', null, false, 'Fondation'],
  [
    'foundationBackfill',
    'Remblayage des fondations',
    'Foundation backfill',
    'stone',
    null,
    false,
    'Excavateur',
    2,
  ],
  [
    'floorJoistsDelivery',
    'Livraison des poutrelles',
    'Floor joist delivery',
    'foundationBackfill',
    null,
    false,
    'Charpentier',
    30,
  ],
  [
    'framing',
    'Charpente',
    'Framing',
    'floorJoistsDelivery',
    null,
    false,
    'Charpentier',
  ],
  ['roofing', 'Toiture', 'Roofing', null, null, true, 'Toiture'],
  [
    'plumbingRoughIn',
    'Sous-terre de plomberie',
    'Underground plumbing',
    'framing',
    20,
    false,
    'Plombier',
  ],
  [
    'electricalEntry',
    'Entrée électrique',
    'Electrical entry',
    'plumbingRoughIn',
    1,
    false,
    'Électricien',
  ],
  ['slab', 'Dalles', 'Slabs', 'plumbingRoughIn', 4, false, 'Cimentier', 2],
  [
    'windowDelivery',
    'Livraison des fenêtres',
    'Window delivery',
    null,
    null,
    true,
    'Charpentier',
  ],
  [
    'garageDoorInstallation',
    'Installation des portes de garage',
    'Garage door installation',
    'windowDelivery',
    1,
    false,
    'Porte de garage',
  ],
  [
    'plumbingElevation',
    'Élévation plomberie',
    'Plumbing elevation',
    'slab',
    7,
    false,
    'Plombier',
    2,
  ],
  [
    'ventilationRoughIn',
    'Rough ventilation',
    'Ventilation rough-in',
    'plumbingElevation',
    null,
    false,
    'Ventilation',
  ],
  [
    'fireplaceInstallation',
    'Installation foyer',
    'Fireplace installation',
    'ventilationRoughIn',
    null,
    false,
    'Foyer',
  ],
  [
    'electricalWiring',
    'Filage électrique',
    'Electrical wiring',
    'ventilationRoughIn',
    null,
    false,
    'Électricien',
    3,
  ],
  [
    'securityVacuum',
    'Alarme & balayeuse',
    'Alarm & central vacuum',
    'electricalWiring',
    null,
    false,
    'Alarme & balayeuse',
  ],
  [
    'insulation',
    'Isolation',
    'Insulation',
    'electricalWiring',
    null,
    false,
    'Isolation',
    2,
  ],
  [
    'drywallDelivery',
    'Livraison du gypse',
    'Drywall delivery',
    'insulation',
    null,
    false,
    'Poseur de gypse',
    14,
  ],
  [
    'drywallFinishing',
    'Gypse & joints',
    'Drywall & joints',
    'drywallDelivery',
    null,
    false,
    'Poseur de gypse',
    3,
  ],
  [
    'ceilingPainting',
    'Peinture plafonds',
    'Ceiling painting',
    'drywallFinishing',
    null,
    false,
    'Peintre',
  ],
  [
    'tileDelivery',
    'Livraison céramique',
    'Tile delivery',
    'ceilingPainting',
    null,
    false,
    'Poseur de céramique',
  ],
  [
    'tileWork',
    'Céramique',
    'Tile work',
    'tileDelivery',
    null,
    false,
    'Poseur de céramique',
    8,
  ],
  [
    'woodworkDelivery',
    'Livraison boiserie',
    'Woodwork delivery',
    'tileWork',
    null,
    false,
    'Poseur de boiserie',
  ],
  [
    'electricalBaseboards',
    'Plinthes électriques',
    'Electrical baseboards',
    null,
    null,
    true,
    'Électricien',
  ],
  [
    'woodwork',
    'Boiserie',
    'Woodwork',
    'woodworkDelivery',
    null,
    false,
    'Poseur de boiserie',
    4,
  ],
  [
    'wallPainting',
    'Peinture murs',
    'Wall painting',
    'woodwork',
    null,
    false,
    'Peintre',
    3,
  ],
  [
    'cabinetsVanities',
    'Armoires & vanités',
    'Cabinets & vanities',
    'wallPainting',
    null,
    false,
    'Cuisiniste',
    5,
  ],
  [
    'lightFixtureDelivery',
    'Livraison des luminaires',
    'Light fixture delivery',
    'cabinetsVanities',
    -2,
    false,
    'Électricien',
  ],
  [
    'plumbingFinishing',
    'Finition plomberie',
    'Plumbing finishing',
    'cabinetsVanities',
    null,
    false,
    'Plombier',
    2,
  ],
  [
    'ventilationFinishing',
    'Finition ventilation',
    'Ventilation finishing',
    'plumbingFinishing',
    null,
    false,
    'Ventilation',
  ],
  [
    'electricalFinishing',
    'Finition électrique',
    'Electrical finishing',
    'plumbingFinishing',
    null,
    false,
    'Électricien',
    2,
  ],
  [
    'securityFinishing',
    'Finition alarme & balayeuse',
    'Alarm & central vacuum finishing',
    'electricalFinishing',
    null,
    false,
    'Alarme & balayeuse',
  ],
  [
    'stairDelivery',
    'Livraison escaliers',
    'Stair delivery',
    'electricalFinishing',
    null,
    false,
    "Poseur d'escalier",
  ],
  [
    'stairInstallation',
    'Installation escaliers',
    'Stair installation',
    'stairDelivery',
    null,
    false,
    "Poseur d'escalier",
    3,
  ],
  [
    'woodFloorDelivery',
    'Livraison planchers de bois',
    'Wood floor delivery',
    'electricalFinishing',
    null,
    false,
    'Poseur de plancher',
  ],
  [
    'woodFloorInstallation',
    'Installation planchers de bois',
    'Wood floor installation',
    'stairInstallation',
    null,
    false,
    'Poseur de plancher',
    2,
  ],
  [
    'woodworkFinishing',
    'Finition boiserie',
    'Woodwork finishing',
    'woodFloorInstallation',
    null,
    false,
    'Poseur de boiserie',
    2,
  ],
  [
    'closetFinishing',
    'Finition des garde-robes',
    'Walk-in closet finishing',
    'woodFloorInstallation',
    null,
    false,
    'Aménagement garde-robe',
    1,
  ],
  [
    'glazing',
    'Vitrerie',
    'Glazing',
    'woodworkFinishing',
    null,
    false,
    'Vitrier',
    2,
  ],
  [
    'paintingFinishing',
    'Finition peinture',
    'Painting finishing',
    'woodworkFinishing',
    null,
    false,
    'Peintre',
    2,
  ],
  [
    'cleaning',
    'Ménage',
    'Cleaning',
    'woodworkFinishing',
    3,
    false,
    'Ménage',
    2,
  ],
  [
    'masonryDelivery',
    'Livraison maçonnerie',
    'Masonry delivery',
    'electricalFinishing',
    15,
    false,
    'Briqueteur',
  ],
  [
    'masonry',
    'Pose maçonnerie',
    'Masonry installation',
    'masonryDelivery',
    null,
    false,
    'Briqueteur',
    10,
  ],
  [
    'exteriorCladding',
    'Revêtement extérieur',
    'Exterior cladding',
    'masonry',
    null,
    false,
    'Poseur revêtement extérieur',
    6,
  ],
  [
    'terrainGrading',
    'Nivellement de terrain',
    'Terrain grading',
    'exteriorCladding',
    3,
    false,
    'Excavateur',
  ],
  ['stucco', 'Crépi', 'Stucco', 'terrainGrading', null, false, 'Crépi'],
  ['gutters', 'Gouttières', 'Gutters', 'stucco', null, false, 'Gouttières'],
  [
    'locationCertificate',
    'Certificat de localisation',
    'Location certificate',
    'terrainGrading',
    null,
    false,
    'Arpenteur',
  ],
  [
    'clientInspection',
    'Inspection avec client',
    'Client inspection',
    'cleaning',
    3,
  ],
  [
    'projectDelivery',
    'Livraison du projet',
    'Project handover',
    'clientInspection',
    5,
  ],
];

const optionalDefinitions = [
  ['stairGlass', 'Mesure verres de l’escalier', 'Stair glass measurement'],
  ['landscaping', 'Aménagement paysager', 'Landscaping'],
  [
    'divisionValidation',
    'Validation client pour divisions',
    'Client validation for partitions',
  ],
  [
    'lightingValidation',
    'Validation client pour éclairage',
    'Client validation for lighting',
  ],
  ['premiumService', 'Service prestige', 'Premium service'],
];

const normalize = value =>
  String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();

const addDate = (date, days) => {
  if (!date) return '';
  const parsed = parseISO(date);
  return isValid(parsed) ? format(addDays(parsed, days), 'yyyy-MM-dd') : '';
};

const maxDate = (first, second) => {
  if (!first) return second || '';
  if (!second) return first;
  return first > second ? first : second;
};

const taskLabel = (definition, french) =>
  french ? definition[1] : definition[2];

const getDependencyStartDate = (dependencyRow, offset) => {
  if (!dependencyRow) return '';
  if (offset === -2) return addDate(dependencyRow.periodEnd, -2);
  if (offset === null || offset === undefined) {
    return addDate(dependencyRow.periodEnd, 1);
  }
  return addDate(dependencyRow.periodStart, offset);
};

const buildTaskRows = (
  startDate,
  manualDates,
  assignments,
  includeOptional
) => {
  const rows = {};
  const definitions = [
    ...taskDefinitions.map(definition => ({ definition, optional: false })),
    ...(includeOptional
      ? optionalDefinitions.map(definition => ({ definition, optional: true }))
      : []),
  ];

  definitions.forEach(({ definition, optional }) => {
    const [key, , , dependency, offset, manual, supplierType, duration] =
      definition;
    const manualDate = manualDates[key];
    let periodStart = optional
      ? manualDate || ''
      : manual
        ? manualDate || ''
        : '';

    if (!manual && !optional && dependency && rows[dependency]?.periodStart) {
      const dependencyRow = rows[dependency];
      const dependencyDate = getDependencyStartDate(dependencyRow, offset);
      periodStart = maxDate(periodStart, dependencyDate);
    }

    if (!periodStart && !dependency && !optional) periodStart = startDate;
    const periodEnd = periodStart
      ? addDate(periodStart, Math.max((duration || 1) - 1, 0))
      : '';

    rows[key] = {
      key,
      titleFr: definition[1],
      titleEn: definition[2],
      periodStart,
      periodEnd,
      assignedToUserId: assignments[key] || '',
      supplierType: supplierType || '',
      duration: duration || 1,
      manual: Boolean(manual),
      optional,
      dependency,
      waitingForManualDependency: Boolean(
        dependency &&
        rows[dependency] &&
        !rows[dependency].periodStart &&
        (rows[dependency].manual || rows[dependency].waitingForManualDependency)
      ),
      minimumStartDate:
        dependency && rows[dependency]
          ? getDependencyStartDate(rows[dependency], offset)
          : '',
    };
  });

  return definitions.map(({ definition, optional }) => ({
    ...rows[definition[0]],
    optional,
  }));
};

const contractorLabel = contractor =>
  [contractor.firstName, contractor.lastName].filter(Boolean).join(' ') ||
  contractor.primaryEmail ||
  contractor.userIdentifier;

const LotScheduleModal = ({
  isOpen,
  lots,
  lotsLoading,
  contractors,
  isSaving,
  errorMessage,
  onClose,
  onSubmit,
}) => {
  const { t, currentLanguage } = usePageTranslations('projectSchedulePage');
  const french = String(currentLanguage || '')
    .toLowerCase()
    .startsWith('fr');
  const [lotId, setLotId] = useState('');
  const [startDate, setStartDate] = useState('');
  const [manualDates, setManualDates] = useState({});
  const [assignments, setAssignments] = useState({});
  const [assignmentByType, setAssignmentByType] = useState({});
  const [assignmentOverrides, setAssignmentOverrides] = useState({});
  const [includeOptional, setIncludeOptional] = useState(true);
  const [localError, setLocalError] = useState('');
  const originalOverflow = useRef(null);

  useEffect(() => {
    if (!isOpen) return;
    setLotId('');
    setStartDate('');
    setManualDates({});
    setAssignments({});
    setAssignmentByType({});
    setAssignmentOverrides({});
    setIncludeOptional(true);
    setLocalError('');
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      if (originalOverflow.current === null) {
        originalOverflow.current = document.body.style.overflow || '';
      }
      document.body.style.overflow = 'hidden';
    } else if (originalOverflow.current !== null) {
      document.body.style.overflow = originalOverflow.current;
      originalOverflow.current = null;
    }
    return () => {
      if (originalOverflow.current !== null) {
        document.body.style.overflow = originalOverflow.current;
        originalOverflow.current = null;
      }
    };
  }, [isOpen]);

  const rows = useMemo(
    () => buildTaskRows(startDate, manualDates, assignments, includeOptional),
    [startDate, manualDates, assignments, includeOptional]
  );

  const getContractors = row => {
    if (!row.supplierType) return contractors;
    const expected = normalize(row.supplierType);
    return contractors.filter(
      contractor => normalize(contractor.supplierType) === expected
    );
  };

  const supplierTypes = useMemo(
    () =>
      Array.from(
        new Set(
          taskDefinitions.map(definition => definition[6]).filter(Boolean)
        )
      ).sort((first, second) =>
        first.localeCompare(second, french ? 'fr' : 'en')
      ),
    [french]
  );

  const handleTypeAssignmentChange = (supplierType, userId) => {
    setAssignmentByType(prev => ({ ...prev, [supplierType]: userId }));
    setAssignments(prev => {
      const next = { ...prev };
      taskDefinitions.forEach(definition => {
        const key = definition[0];
        if (definition[6] === supplierType && !assignmentOverrides[key]) {
          next[key] = userId;
        }
      });
      return next;
    });
  };

  const handleTaskAssignmentChange = (key, userId) => {
    setAssignmentOverrides(prev => ({ ...prev, [key]: true }));
    setAssignments(prev => ({ ...prev, [key]: userId }));
  };

  const lotLabel = lot =>
    lot.displayId !== undefined && lot.displayId !== null
      ? `${french ? 'Lot' : 'Lot'} ${lot.displayId}`
      : french
        ? 'Lot inconnu'
        : 'Unknown lot';

  const handleSubmit = event => {
    event.preventDefault();
    const requiredManual = rows.find(row => row.manual && !row.periodStart);
    if (!startDate) {
      setLocalError(
        french
          ? 'Veuillez saisir la date de la promesse d’achat signée.'
          : 'Enter the signed purchase promise date.'
      );
      return;
    }
    if (!lotId) {
      setLocalError(french ? 'Veuillez sélectionner un lot.' : 'Select a lot.');
      return;
    }
    if (requiredManual) {
      setLocalError(
        french
          ? `Veuillez saisir la date de « ${requiredManual.titleFr} ».`
          : `Enter the date for “${requiredManual.titleEn}”.`
      );
      return;
    }
    const unresolved = rows.find(row => !row.optional && !row.periodStart);
    if (unresolved) {
      setLocalError(
        french
          ? `La date de « ${unresolved.titleFr} » ne peut pas être calculée.`
          : `The date for “${unresolved.titleEn}” cannot be calculated.`
      );
      return;
    }

    const activeRows = rows.filter(
      row => !row.optional || row.periodStart || row.assignedToUserId
    );
    const scheduleEndDate = activeRows.reduce(
      (latest, row) => (row.periodEnd > latest ? row.periodEnd : latest),
      startDate
    );

    onSubmit({
      lotId,
      startDate,
      endDate: scheduleEndDate,
      tasks: activeRows,
      description: `${french ? 'Calendrier du lot' : 'Lot schedule'} ${lotId}`,
    });
  };

  if (!isOpen) return null;

  return (
    <div
      className="schedule-modal-overlay"
      role="dialog"
      aria-modal="true"
      onMouseDown={event => {
        if (event.target === event.currentTarget) onClose();
      }}
    >
      <div
        className="schedule-modal lot-schedule-modal"
        onMouseDown={event => event.stopPropagation()}
      >
        <div className="schedule-modal-header">
          <div className="schedule-modal-title">
            {t(
              'createLotSchedule.title',
              french ? 'Créer le calendrier du lot' : 'Create lot schedule'
            )}
          </div>
          <button
            type="button"
            className="modal-close"
            aria-label={french ? 'Fermer' : 'Close'}
            onClick={onClose}
          >
            ×
          </button>
        </div>
        {(errorMessage || localError) && (
          <div className="form-error">{errorMessage || localError}</div>
        )}
        <form onSubmit={handleSubmit}>
          <div className="lot-schedule-setup">
            <label>
              <span>{t('createLotSchedule.lot', french ? 'Lot' : 'Lot')}</span>
              <select
                value={lotId}
                onChange={event => setLotId(event.target.value)}
                disabled={lotsLoading}
                required
              >
                <option value="">
                  {lotsLoading
                    ? french
                      ? 'Chargement…'
                      : 'Loading…'
                    : french
                      ? 'Sélectionner un lot'
                      : 'Select a lot'}
                </option>
                {lots.map(lot => (
                  <option key={lot.value} value={lot.value}>
                    {lotLabel(lot)}
                  </option>
                ))}
              </select>
            </label>
            <label>
              <span>
                {french ? 'Promesse d’achat signée' : 'Signed purchase promise'}
              </span>
              <input
                type="date"
                value={startDate}
                onChange={event => setStartDate(event.target.value)}
                required
              />
            </label>
          </div>

          <div className="lot-schedule-assignment-presets">
            <h4>
              {french
                ? 'Assignation par type de fournisseur'
                : 'Assignment by supplier type'}
            </h4>
            <p>
              {french
                ? 'Choisissez un fournisseur par type; vous pouvez modifier chaque tâche individuellement.'
                : 'Choose a supplier for each type; individual task assignments can still be changed below.'}
            </p>
            <div className="lot-schedule-assignment-grid">
              {supplierTypes.map(supplierType => {
                const eligibleContractors = contractors.filter(
                  contractor =>
                    normalize(contractor.supplierType) ===
                    normalize(supplierType)
                );
                return (
                  <label key={supplierType}>
                    <span>{supplierType}</span>
                    <select
                      value={assignmentByType[supplierType] || ''}
                      onChange={event =>
                        handleTypeAssignmentChange(
                          supplierType,
                          event.target.value
                        )
                      }
                    >
                      <option value="">
                        {french ? 'Non assigné' : 'Unassigned'}
                      </option>
                      {eligibleContractors.map(contractor => (
                        <option
                          key={contractor.userIdentifier}
                          value={contractor.userIdentifier}
                        >
                          {contractorLabel(contractor)}
                        </option>
                      ))}
                    </select>
                  </label>
                );
              })}
            </div>
          </div>

          <div className="lot-schedule-task-list">
            {rows.map(row => {
              const eligibleContractors = getContractors(row);
              return (
                <div
                  className={`lot-schedule-task ${row.optional ? 'optional' : ''}`}
                  key={row.key}
                >
                  <div className="lot-schedule-task-title">
                    <strong>
                      {taskLabel([row.key, row.titleFr, row.titleEn], french)}
                    </strong>
                    {row.optional && (
                      <span>{french ? 'Optionnel' : 'Optional'}</span>
                    )}
                    {!row.optional && row.manual && (
                      <span>
                        {french ? 'À inscrire manuellement' : 'Enter manually'}
                      </span>
                    )}
                  </div>
                  <div className="lot-schedule-task-meta">
                    {row.manual || row.optional ? (
                      <input
                        type="date"
                        aria-label={taskLabel(
                          [row.key, row.titleFr, row.titleEn],
                          french
                        )}
                        value={row.periodStart}
                        min={row.minimumStartDate || undefined}
                        onChange={event =>
                          setManualDates(prev => ({
                            ...prev,
                            [row.key]: event.target.value,
                          }))
                        }
                        required={row.manual}
                      />
                    ) : (
                      <span>
                        {row.periodStart ||
                          (row.waitingForManualDependency
                            ? french
                              ? 'Ajoutez la date manuelle précédente pour calculer cette date'
                              : 'Add the previous manual date to calculate this date'
                            : french
                              ? 'En attente d’une date précédente'
                              : 'Waiting for a previous date')}
                      </span>
                    )}
                    {row.periodEnd && row.periodEnd !== row.periodStart && (
                      <span>→ {row.periodEnd}</span>
                    )}
                  </div>
                  <select
                    value={row.assignedToUserId}
                    onChange={event =>
                      handleTaskAssignmentChange(row.key, event.target.value)
                    }
                    aria-label={`${french ? 'Assigné' : 'Assigned'}: ${taskLabel([row.key, row.titleFr, row.titleEn], french)}`}
                  >
                    <option value="">
                      {french ? 'Non assigné' : 'Unassigned'}
                    </option>
                    {eligibleContractors.map(contractor => (
                      <option
                        key={contractor.userIdentifier}
                        value={contractor.userIdentifier}
                      >
                        {contractorLabel(contractor)}
                      </option>
                    ))}
                  </select>
                </div>
              );
            })}
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="modal-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
              {french ? 'Annuler' : 'Cancel'}
            </button>
            <button type="submit" className="modal-primary" disabled={isSaving}>
              {isSaving
                ? french
                  ? 'Création…'
                  : 'Creating…'
                : french
                  ? 'Créer le calendrier'
                  : 'Create schedule'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LotScheduleModal;

LotScheduleModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  lots: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      label: PropTypes.string,
      displayId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
    })
  ).isRequired,
  lotsLoading: PropTypes.bool.isRequired,
  contractors: PropTypes.arrayOf(
    PropTypes.shape({
      userIdentifier: PropTypes.string,
      firstName: PropTypes.string,
      lastName: PropTypes.string,
      primaryEmail: PropTypes.string,
      supplierType: PropTypes.string,
    })
  ).isRequired,
  isSaving: PropTypes.bool.isRequired,
  errorMessage: PropTypes.string,
  onClose: PropTypes.func.isRequired,
  onSubmit: PropTypes.func.isRequired,
};
