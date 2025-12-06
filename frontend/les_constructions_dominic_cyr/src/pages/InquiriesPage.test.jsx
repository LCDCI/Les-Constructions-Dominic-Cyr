import { render, screen, waitFor } from '@testing-library/react';
import InquiriesPage from './InquiriesPage';

// Mock fetch
global.fetch = jest.fn();

describe('InquiriesPage', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  test('loads and displays inquiries', async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => ([
        {
          id: 1,
          name: 'Jane Owner',
          email: 'jane@example.com',
          phone: '555-1234',
          message: 'Please call me',
          createdAt: '2024-02-01T10:00:00Z'
        }
      ])
    });

    render(<InquiriesPage />);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/inquiries');
    });

    expect(await screen.findByText(/jane owner/i)).toBeInTheDocument();
    expect(screen.getByText(/jane@example.com/i)).toBeInTheDocument();
    expect(screen.getByText(/please call me/i)).toBeInTheDocument();
  });

  test('shows error when load fails', async () => {
    fetch.mockResolvedValueOnce({ ok: false });

    render(<InquiriesPage />);

    expect(await screen.findByText(/unable to load inquiries/i)).toBeInTheDocument();
  });
});
