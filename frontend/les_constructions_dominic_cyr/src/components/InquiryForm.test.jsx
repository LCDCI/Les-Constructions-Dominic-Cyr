import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import InquiryForm from './InquiryForm';

// Mock fetch
global.fetch = jest.fn();

describe('InquiryForm E2E Tests', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  test('E2E: User successfully submits an inquiry with all fields', async () => {
    // Given
    fetch.mockResolvedValueOnce({
      ok: true,
      text: async () => 'Thank you! Your inquiry has been received.',
    });

    render(<InquiryForm />);

    // When
    const nameInput = screen.getByPlaceholderText(/your name/i);
    const emailInput = screen.getByPlaceholderText(/your.email@example.com/i);
    const phoneInput = screen.getByPlaceholderText(/\(555\) 123-4567/i);
    const messageInput = screen.getByPlaceholderText(
      /tell us about your project/i
    );
    const submitButton = screen.getByRole('button', {
      name: /submit inquiry/i,
    });

    await userEvent.type(nameInput, 'Jane Doe');
    await userEvent.type(emailInput, 'jane@example.com');
    await userEvent.type(phoneInput, '555-1234');
    await userEvent.type(
      messageInput,
      'I am interested in building a custom home.'
    );

    fireEvent.click(submitButton);

    // Then
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: 'Jane Doe',
          email: 'jane@example.com',
          phone: '555-1234',
          message: 'I am interested in building a custom home.',
        }),
      });
    });

    await waitFor(() => {
      expect(
        screen.getByText(/thank you! your inquiry has been received/i)
      ).toBeInTheDocument();
    });

    // Form should be cleared
    expect(nameInput.value).toBe('');
    expect(emailInput.value).toBe('');
    expect(phoneInput.value).toBe('');
    expect(messageInput.value).toBe('');
  });

  test('E2E: User submits inquiry without optional phone field', async () => {
    // Given
    fetch.mockResolvedValueOnce({
      ok: true,
      text: async () => 'Thank you! Your inquiry has been received.',
    });

    render(<InquiryForm />);

    // When
    await userEvent.type(
      screen.getByPlaceholderText(/your name/i),
      'John Smith'
    );
    await userEvent.type(
      screen.getByPlaceholderText(/your.email@example.com/i),
      'john@test.com'
    );
    await userEvent.type(
      screen.getByPlaceholderText(/tell us about your project/i),
      'Need a renovation quote.'
    );

    fireEvent.click(screen.getByRole('button', { name: /submit inquiry/i }));

    // Then
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith('/api/inquiries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: 'John Smith',
          email: 'john@test.com',
          phone: '',
          message: 'Need a renovation quote.',
        }),
      });
    });

    await waitFor(() => {
      expect(screen.getByText(/thank you/i)).toBeInTheDocument();
    });
  });

  test('E2E: User sees validation error when required fields are missing', async () => {
    // Given
    render(<InquiryForm />);

    // When - Submit without filling required fields
    fireEvent.click(screen.getByRole('button', { name: /submit inquiry/i }));

    // Then
    await waitFor(() => {
      expect(
        screen.getByText(/please fill out all required fields/i)
      ).toBeInTheDocument();
    });

    expect(fetch).not.toHaveBeenCalled();
  });

  test('E2E: User sees error message when submission fails', async () => {
    // Given
    fetch.mockResolvedValueOnce({
      ok: false,
      text: async () => 'Server error',
    });

    render(<InquiryForm />);

    // When
    await userEvent.type(screen.getByPlaceholderText(/your name/i), 'Alice');
    await userEvent.type(
      screen.getByPlaceholderText(/your.email@example.com/i),
      'alice@test.com'
    );
    await userEvent.type(
      screen.getByPlaceholderText(/tell us about your project/i),
      'Test message'
    );

    fireEvent.click(screen.getByRole('button', { name: /submit inquiry/i }));

    // Then
    await waitFor(() => {
      expect(screen.getByText(/server error/i)).toBeInTheDocument();
    });
  });

  test('E2E: User sees network error when fetch fails', async () => {
    // Given
    fetch.mockRejectedValueOnce(new Error('Network failure'));

    render(<InquiryForm />);

    // When
    await userEvent.type(screen.getByPlaceholderText(/your name/i), 'Bob');
    await userEvent.type(
      screen.getByPlaceholderText(/your.email@example.com/i),
      'bob@test.com'
    );
    await userEvent.type(
      screen.getByPlaceholderText(/tell us about your project/i),
      'Inquiry message'
    );

    fireEvent.click(screen.getByRole('button', { name: /submit inquiry/i }));

    // Then
    await waitFor(() => {
      expect(screen.getByText(/network error/i)).toBeInTheDocument();
    });
  });

  test('E2E: Submit button shows loading state during submission', async () => {
    // Given
    let resolvePromise;
    fetch.mockReturnValueOnce(
      new Promise(resolve => {
        resolvePromise = resolve;
      })
    );

    render(<InquiryForm />);

    // When
    await userEvent.type(screen.getByPlaceholderText(/your name/i), 'Charlie');
    await userEvent.type(
      screen.getByPlaceholderText(/your.email@example.com/i),
      'charlie@test.com'
    );
    await userEvent.type(
      screen.getByPlaceholderText(/tell us about your project/i),
      'Test inquiry'
    );

    fireEvent.click(screen.getByRole('button', { name: /submit inquiry/i }));

    // Then - Button should show loading state
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /sending/i })).toBeDisabled();
    });

    // Resolve the promise
    resolvePromise({
      ok: true,
      text: async () => 'Thank you! Your inquiry has been received.',
    });

    // Button should return to normal state
    await waitFor(() => {
      expect(
        screen.getByRole('button', { name: /submit inquiry/i })
      ).not.toBeDisabled();
    });
  });
});
