import React, { useEffect, useMemo, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { usePageTranslations } from '../hooks/usePageTranslations';
import ConfirmationModal from '../components/Modals/ConfirmationModal';

const fetchInquiries = async getAccessTokenSilently => {
  const { getAuthAudience } = await import('../utils/authConfig');
  const token = await getAccessTokenSilently({
    authorizationParams: {
      audience: getAuthAudience(),
    },
  });
  const res = await fetch('/api/v1/inquiries', {
    method: 'GET',
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error('Failed to fetch inquiries');
  return res.json();
};

export default function OwnerInquiriesPage() {
  const { t } = usePageTranslations('ownerInquiriesPage');
  const [inquiries, setInquiries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [redirecting, setRedirecting] = useState(false);
  const [pendingAction, setPendingAction] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const {
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    loginWithRedirect,
  } = useAuth0();

  useEffect(() => {
    if (isLoading) return;
    if (!isAuthenticated && !redirecting) {
      setRedirecting(true);
      setLoading(false);
      loginWithRedirect({ appState: { returnTo: '/inquiries' } });
      return;
    }

    fetchInquiries(getAccessTokenSilently)
      .then(setInquiries)
      .catch(setError)
      .finally(() => setLoading(false));
  }, [
    isAuthenticated,
    isLoading,
    getAccessTokenSilently,
    loginWithRedirect,
    redirecting,
  ]);

  const formatDate = value => {
    const d = value ? new Date(value) : null;
    return d && !Number.isNaN(d.getTime()) ? d.toLocaleString() : 'N/A';
  };

  const formatContactSubject = value => {
    const key = String(value || '').toLowerCase();
    return t(`table.contactSubjects.${key}`, value || '-');
  };

  const unescapeHtml = str => {
    if (!str) return str;
    const decoder = document.createElement('textarea');
    decoder.innerHTML = String(str);
    return decoder.value;
  };

  const performInquiryAction = async () => {
    if (!pendingAction) return;
    setActionLoading(true);
    setActionError(null);
    try {
      const { getAuthAudience } = await import('../utils/authConfig');
      const token = await getAccessTokenSilently({
        authorizationParams: { audience: getAuthAudience() },
      });
      const { inquiryId, action } = pendingAction;
      const response = await fetch(
        action === 'delete'
          ? `/api/v1/inquiries/${inquiryId}`
          : `/api/v1/inquiries/${inquiryId}/archive?archived=${action === 'archive'}`,
        {
          method: action === 'delete' ? 'DELETE' : 'PATCH',
          credentials: 'omit',
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      if (!response.ok)
        throw new Error(t('actionError', 'Unable to update inquiry.'));
      if (action === 'delete') {
        setInquiries(current =>
          current.filter(inquiry => inquiry.id !== inquiryId)
        );
      } else {
        const updated = await response.json();
        setInquiries(current =>
          current.map(inquiry => (inquiry.id === inquiryId ? updated : inquiry))
        );
      }
      setPendingAction(null);
    } catch (actionRequestError) {
      setActionError(actionRequestError);
    } finally {
      setActionLoading(false);
    }
  };

  const confirmationConfig = pendingAction
    ? {
        title:
          pendingAction.action === 'delete'
            ? t('confirm.deleteTitle', 'Delete inquiry')
            : pendingAction.action === 'archive'
              ? t('confirm.archiveTitle', 'Archive inquiry')
              : t('confirm.unarchiveTitle', 'Unarchive inquiry'),
        message:
          pendingAction.action === 'delete'
            ? t(
                'confirm.deleteMessage',
                'Are you sure you want to permanently delete this inquiry?'
              )
            : pendingAction.action === 'archive'
              ? t(
                  'confirm.archiveMessage',
                  'Are you sure you want to archive this inquiry?'
                )
              : t(
                  'confirm.unarchiveMessage',
                  'Are you sure you want to unarchive this inquiry?'
                ),
        confirmText:
          pendingAction.action === 'delete'
            ? t('actions.delete', 'Delete')
            : pendingAction.action === 'archive'
              ? t('actions.archive', 'Archive')
              : t('actions.unarchive', 'Unarchive'),
        cancelText: t('actions.cancel', 'Cancel'),
        isDestructive: pendingAction.action === 'delete',
        onConfirm: performInquiryAction,
      }
    : {};

  const decodedInquiries = useMemo(
    () =>
      inquiries.map(inq => ({
        ...inq,
        name: unescapeHtml(inq.name),
        email: unescapeHtml(inq.email),
        phone: unescapeHtml(inq.phone),
        contactSubject: unescapeHtml(inq.contactSubject),
        message: unescapeHtml(inq.message),
      })),
    [inquiries]
  );

  return (
    <>
      <div style={{ padding: '2rem', minHeight: 'calc(100vh - 200px)' }}>
        <h1>{t('title', 'Inquiry Review')}</h1>
        {loading && <p>{t('loading', 'Loading inquiries...')}</p>}
        {error && (
          <p style={{ color: 'red' }}>
            {t('error', 'Error:')} {error.message}
          </p>
        )}
        {actionError && (
          <p style={{ color: 'red' }}>
            {t('actionError', 'Unable to update inquiry.')}{' '}
            {actionError.message}
          </p>
        )}
        {!loading && !error && (
          <table
            style={{
              width: '100%',
              borderCollapse: 'collapse',
              marginTop: '2rem',
            }}
          >
            <thead>
              <tr style={{ background: '#f0f0f0' }}>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.name', 'Name')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.email', 'Email')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.phone', 'Phone')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.contactSubject', 'Contacting about')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.message', 'Message')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.timestamp', 'Timestamp')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.status', 'Status')}
                </th>
                <th style={{ padding: '0.5rem', border: '1px solid #ccc' }}>
                  {t('table.actions', 'Actions')}
                </th>
              </tr>
            </thead>
            <tbody>
              {decodedInquiries.length === 0 ? (
                <tr>
                  <td
                    colSpan={8}
                    style={{ textAlign: 'center', padding: '2rem' }}
                  >
                    {t('noInquiries', 'No inquiries found.')}
                  </td>
                </tr>
              ) : (
                decodedInquiries.map(inq => (
                  <tr
                    key={inq.id}
                    style={inq.archived ? { opacity: 0.65 } : undefined}
                  >
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.name}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      <a
                        href={`mailto:${encodeURIComponent(inq.email || '')}`}
                        style={{
                          color: '#0066cc',
                          textDecoration: 'underline',
                          cursor: 'pointer',
                        }}
                      >
                        {inq.email}
                      </a>
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.phone ? inq.phone : '-'}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {formatContactSubject(inq.contactSubject)}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.message}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {formatDate(inq.createdAt)}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      {inq.archived
                        ? t('status.archived', 'Archived')
                        : t('status.active', 'Active')}
                    </td>
                    <td style={{ padding: '0.5rem', border: '1px solid #eee' }}>
                      <button
                        type="button"
                        disabled={actionLoading}
                        onClick={() =>
                          setPendingAction({
                            inquiryId: inq.id,
                            action: inq.archived ? 'unarchive' : 'archive',
                          })
                        }
                      >
                        {inq.archived
                          ? t('actions.unarchive', 'Unarchive')
                          : t('actions.archive', 'Archive')}
                      </button>{' '}
                      <button
                        type="button"
                        disabled={actionLoading}
                        onClick={() =>
                          setPendingAction({
                            inquiryId: inq.id,
                            action: 'delete',
                          })
                        }
                      >
                        {t('actions.delete', 'Delete')}
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
      <ConfirmationModal
        isOpen={Boolean(pendingAction)}
        onCancel={() => !actionLoading && setPendingAction(null)}
        config={confirmationConfig}
      />
    </>
  );
}
