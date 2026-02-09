/* eslint-disable no-console */
import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { fetchLotById } from '../../features/lots/api/lots';
import { getProjectMetadata } from '../../features/projects/api/projectMetadataApi';
import scheduleApi from '../../features/schedules/api/scheduleApi';
import taskApi from '../../features/schedules/api/taskApi';
import useBackendUser from '../../hooks/useBackendUser';
import usePageTranslations from '../../hooks/usePageTranslations';
import '../../styles/Public_Facing/home.css';
import '../../styles/Project/ProjectMetadata.css';

const formatPrice = price => {
  if (price == null) return '—';
  const numericPrice = typeof price === 'number' ? price : Number(price);
  if (Number.isNaN(numericPrice)) return String(price);
  return new Intl.NumberFormat('en-CA', {
    style: 'currency',
    currency: 'CAD',
  }).format(numericPrice);
};

const normalizeStatusKey = raw => {
  if (!raw) return 'unknown';
  const key = String(raw)
    .toLowerCase()
    .replace(/[^a-z0-9]/g, '');
  if (key.includes('contract')) return 'contract';
  if (key === 'inprogress') return 'inprogress';
  if (key === 'available') return 'available';
  if (key === 'reserved') return 'reserved';
  if (key === 'sold') return 'sold';
  if (key === 'pending') return 'pending';
  return key || 'unknown';
};

// Fallback labels for task statuses (used when translations aren't loaded)
const TASK_STATUS_LABELS = {
  todo: 'To Do',
  inprogress: 'In Progress',
  completed: 'Completed',
  onhold: 'On Hold',
  cancelled: 'Cancelled',
  pending: 'Pending',
};

// Normalize task statuses coming from API (handles uppercase/underscores)
const normalizeTaskStatusKey = raw => {
  if (!raw) return 'pending';
  // Convert to lowercase and remove underscores to match translation keys
  const key = String(raw).toLowerCase().replace(/_/g, '');
  if (key === 'completed') return 'completed';
  if (key === 'inprogress') return 'inprogress';
  if (key === 'todo') return 'todo';
  if (key === 'onhold') return 'onhold';
  if (key === 'cancelled') return 'cancelled';
  if (key === 'pending') return 'pending';
  return key || 'pending';
};

// Helper to get task status label with fallback
const getTaskStatusLabel = (t, statusKey) => {
  const translationKey = `taskStatus.${statusKey}`;
  const translated = t(translationKey);
  // If translation returns the key itself, use fallback
  if (translated === translationKey || translated.startsWith('taskStatus.')) {
    return TASK_STATUS_LABELS[statusKey] || statusKey;
  }
  return translated;
};

const LotMetadata = () => {
  const { projectId, lotId } = useParams();
  const navigate = useNavigate();
  const {
    isAuthenticated,
    isLoading: authLoading,
    getAccessTokenSilently,
  } = useAuth0();
  const [lot, setLot] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const { profile, role } = useBackendUser();
  const [project, setProject] = useState(null);
  const { t } = usePageTranslations('lotMetadata');

  const [tasks, setTasks] = useState([]);
  const [completedTasks, setCompletedTasks] = useState([]);
  const [remainingTasks, setRemainingTasks] = useState([]);

  useEffect(() => {
    let cancelled = false;

    const applyProjectColors = projectData => {
      document.documentElement.style.setProperty(
        '--project-primary',
        projectData.primaryColor
      );
      document.documentElement.style.setProperty(
        '--project-tertiary',
        projectData.tertiaryColor
      );
      document.documentElement.style.setProperty(
        '--project-buyer',
        projectData.buyerColor
      );
    };

    const load = async () => {
      if (authLoading) return;

      setError(null);
      setLoading(true);

      try {
        let token = null;
        if (isAuthenticated) {
          try {
            token = await getAccessTokenSilently({
              authorizationParams: {
                audience: import.meta.env.VITE_AUTH0_AUDIENCE,
              },
            });
          } catch (tokenErr) {
            // eslint-disable-next-line no-console
            console.warn(
              'Unable to fetch token, continuing without auth',
              tokenErr
            );
          }
        }

        const projectData = await getProjectMetadata(projectId, token);
        if (!cancelled) {
          setProject(projectData);
          applyProjectColors(projectData);
        }

        const lotData = await fetchLotById({
          projectIdentifier: projectId,
          lotId,
          token,
        });
        if (!cancelled) {
          setLot(lotData);
        }

        try {
          const schedulesData = await scheduleApi.getSchedulesByProject(
            projectId,
            token
          );
          const lotSchedules = schedulesData.filter(
            s => String(s.lotId) === String(lotId)
          );

          if (lotSchedules.length > 0) {
            const allTasksArrays = await Promise.all(
              lotSchedules.map(schedule =>
                taskApi
                  .getTasksForSchedule(
                    schedule.scheduleIdentifier ||
                      schedule.scheduleId ||
                      schedule.id,
                    token
                  )
                  .catch(() => [])
              )
            );
            const allTasks = allTasksArrays.flat();

            if (!cancelled) {
              setTasks(allTasks);
              setCompletedTasks(
                allTasks.filter(t => t.taskStatus === 'COMPLETED')
              );
              setRemainingTasks(
                allTasks.filter(t => t.taskStatus !== 'COMPLETED')
              );
            }
          } else if (!cancelled) {
            setTasks([]);
            setCompletedTasks([]);
            setRemainingTasks([]);
          }
        } catch (taskErr) {
          console.warn('Failed to load tasks:', taskErr);
          if (!cancelled) {
            setTasks([]);
            setCompletedTasks([]);
            setRemainingTasks([]);
          }
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to load lot');
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();

    return () => {
      cancelled = true;
      document.documentElement.style.removeProperty('--project-primary');
      document.documentElement.style.removeProperty('--project-tertiary');
      document.documentElement.style.removeProperty('--project-buyer');
    };
  }, [authLoading, getAccessTokenSilently, isAuthenticated, lotId, projectId]);

  if (loading)
    return <div className="page">{t('loadingLot') || 'Loading...'}</div>;
  if (error) return <div className="page">{error}</div>;
  if (!lot) return null;

  return (
    <div
      className="project-metadata"
      style={{
        ['--project-primary']: lot.primaryColor || '#2c7be5',
        ['--project-buyer']: lot.buyerColor || '#27ae60',
      }}
    >
      <div
        className="metadata-hero"
        style={{
          backgroundColor: project?.primaryColor || lot.primaryColor || '#ddd',
        }}
      >
        <div className="hero-content">
          <h1 className="project-title">
            {lot.id
              ? `${t('lot')} ${lot.id}`
              : lot.lotNumber || `${t('lot')} ${lot.lotId}`}
          </h1>
          {(() => {
            const statusKey = normalizeStatusKey(lot.lotStatus);
            const statusLabel =
              t(`lotStatus.${statusKey}`) || lot.lotStatus || '';
            return (
              <span className={`status-badge status-${statusKey}`}>
                {statusLabel}
              </span>
            );
          })()}
        </div>
        {project?.imageIdentifier && (
          <div className="hero-image">
            <img
              src={`${
                import.meta.env.VITE_FILES_SERVICE_URL ||
                (typeof window !== 'undefined' &&
                window.location.hostname.includes('constructions-dominiccyr')
                  ? 'https://files-service-app-xubs2.ondigitalocean.app'
                  : `${window.location.origin}/files`)
              }/files/${project.imageIdentifier}`}
              alt={project.projectName}
            />
          </div>
        )}
      </div>

      <div className="metadata-content">
        <section className="metadata-section">
          <h2 style={{ color: lot.primaryColor }}>
            {t('lotOverview') || 'Lot Overview'}
          </h2>
          <div className="metadata-grid">
            <div className="metadata-item">
              <span className="metadata-label">
                {t('civicAddress') || 'Civic Address'}
              </span>
              <span className="metadata-value">
                {lot.civicAddress || t('notSet')}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">
                {t('areaSqft') || 'Area (sqft)'}
              </span>
              <span className="metadata-value">
                {lot.dimensionsSquareFeet || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">
                {t('areaSqm') || 'Area (sqm)'}
              </span>
              <span className="metadata-value">
                {lot.dimensionsSquareMeters || '—'}
              </span>
            </div>
            <div className="metadata-item">
              <span className="metadata-label">{t('price') || 'Price'}</span>
              <span className="metadata-value">{formatPrice(lot.price)}</span>
            </div>
            <div className="metadata-item full-width">
              <span className="metadata-label">
                {t('progress') || 'Progress'}
              </span>
              <div className="progress-bar">
                <div
                  className="progress-fill"
                  style={{
                    width: `${tasks.length > 0 ? Math.round((completedTasks.length / tasks.length) * 100) : lot.progressPercentage || 0}%`,
                    backgroundColor:
                      project?.primaryColor || lot.primaryColor || '#27ae60',
                  }}
                ></div>
                <span className="progress-text">
                  {tasks.length > 0
                    ? Math.round((completedTasks.length / tasks.length) * 100)
                    : lot.progressPercentage || 0}
                  %
                </span>
              </div>
              {tasks.length > 0 && (
                <span
                  className="metadata-value"
                  style={{
                    marginTop: '5px',
                    fontSize: '0.9rem',
                    color: '#666',
                  }}
                >
                  {completedTasks.length} / {tasks.length}{' '}
                  {t('tasksCompleted') || 'tasks completed'}
                </span>
              )}
            </div>
          </div>
          {lot.lotDescription && (
            <div className="project-description">
              <p>{lot.lotDescription}</p>
            </div>
          )}
        </section>

        {lot.assignedUsers && (
          <section className="metadata-section">
            <h2 style={{ color: lot.primaryColor }}>
              {t('assignedUsers') || 'Assigned Users'}
            </h2>
            <div className="lots-grid">
              {lot.assignedUsers
                .filter(user => {
                  if (role === 'OWNER') {
                    return (
                      user.role !== 'OWNER' && user.userId !== profile?.userId
                    );
                  }
                  return user.role !== 'OWNER';
                })
                .map(user => (
                  <div
                    key={user.userId || user.id}
                    className="lot-card"
                    style={{ borderColor: lot.primaryColor }}
                  >
                    <h3>
                      {user.fullName ||
                        `${user.firstName || ''} ${user.lastName || ''}`.trim() ||
                        t('unnamedUser')}
                    </h3>
                    <p className="lot-address">{user.email || t('noEmail')}</p>
                    <div className="lot-status-inline">
                      <span className="status-label">
                        {t(`userRole.${(user.role || '').toLowerCase()}`) ||
                          user.role}
                      </span>
                    </div>
                  </div>
                ))}
            </div>
          </section>
        )}

        {tasks.length > 0 && (
          <>
            <section className="metadata-section">
              <h2 style={{ color: lot.primaryColor }}>
                {t('completedTasks') || 'Completed Tasks'} (
                {completedTasks.length})
              </h2>
              {completedTasks.length > 0 ? (
                <div className="lots-grid">
                  {completedTasks.map(task => (
                    <div
                      key={task.taskId}
                      className="lot-card"
                      style={{
                        borderColor: lot.primaryColor,
                        cursor: 'pointer',
                      }}
                      onClick={() => navigate(`/tasks/${task.taskId}`)}
                    >
                      <h3>{task.taskTitle}</h3>
                      <p className="lot-address">
                        {task.taskDescription || t('noDescription')}
                      </p>
                      <div
                        className="progress-bar"
                        style={{ marginTop: '10px' }}
                      >
                        <div
                          className="progress-fill"
                          style={{
                            width: `${task.taskProgress != null ? Math.round(task.taskProgress) : 100}%`,
                            backgroundColor: lot.primaryColor || '#27ae60',
                          }}
                        ></div>
                        <span className="progress-text">
                          {task.taskProgress != null
                            ? Math.round(task.taskProgress)
                            : 100}
                          %
                        </span>
                      </div>
                      <div className="lot-status-inline">
                        <span className="status-label">
                          {getTaskStatusLabel(
                            t,
                            normalizeTaskStatusKey(task.taskStatus)
                          )}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p>{t('noCompletedTasks') || 'No completed tasks yet.'}</p>
              )}
            </section>

            <section className="metadata-section">
              <h2 style={{ color: lot.primaryColor }}>
                {t('remainingTasks') || 'Remaining Tasks'} (
                {remainingTasks.length})
              </h2>
              {remainingTasks.length > 0 ? (
                <div className="lots-grid">
                  {remainingTasks.map(task => (
                    <div
                      key={task.taskId}
                      className="lot-card"
                      style={{
                        borderColor: lot.primaryColor,
                        cursor: 'pointer',
                      }}
                      onClick={() => navigate(`/tasks/${task.taskId}`)}
                    >
                      <h3>{task.taskTitle}</h3>
                      <p className="lot-address">
                        {task.taskDescription || t('noDescription')}
                      </p>
                      <div
                        className="progress-bar"
                        style={{ marginTop: '10px' }}
                      >
                        <div
                          className="progress-fill"
                          style={{
                            width: `${task.taskProgress != null ? Math.round(task.taskProgress) : 0}%`,
                            backgroundColor: lot.primaryColor || '#27ae60',
                          }}
                        ></div>
                        <span className="progress-text">
                          {task.taskProgress != null
                            ? Math.round(task.taskProgress)
                            : 0}
                          %
                        </span>
                      </div>
                      <div className="lot-status-inline">
                        <span className="status-label">
                          {getTaskStatusLabel(
                            t,
                            normalizeTaskStatusKey(task.taskStatus)
                          )}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <p>{t('noRemainingTasks') || 'No remaining tasks.'}</p>
              )}
            </section>
          </>
        )}
      </div>

      <div className="button-container">
        <button
          className="project-metadata-back"
          style={{ backgroundColor: lot.primaryColor, color: '#fff' }}
          onClick={() => {
            try {
              if (window.history.length > 1) {
                navigate(-1);
                return;
              }
            } catch (e) {
              // ignore
            }
            navigate(`/projects/${projectId}/lots/select`);
          }}
          onMouseOver={e => (e.currentTarget.style.filter = 'brightness(0.9)')}
          onMouseOut={e => (e.currentTarget.style.filter = '')}
        >
          {t('backToLotSelection') || 'Back to lot selection'}
        </button>

        <button
          className="project-metadata-schedule"
          style={{ backgroundColor: lot.primaryColor, color: '#fff' }}
          onClick={() => navigate('/projects')}
          onMouseOver={e => (e.currentTarget.style.filter = 'brightness(0.9)')}
          onMouseOut={e => (e.currentTarget.style.filter = '')}
        >
          {t('backToProjects') || 'Back to projects'}
        </button>
      </div>
    </div>
  );
};

export default LotMetadata;
