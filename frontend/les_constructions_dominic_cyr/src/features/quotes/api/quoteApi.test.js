import { describe, it, expect, vi, beforeEach } from 'vitest';
import { quoteApi } from '../api/quoteApi';

/**
 * Quote API Integration Tests
 */
describe('quoteApi', () => {
  beforeEach(() => {
    // Reset any mocks before each test
    vi.clearAllMocks();
  });

  /**
   * POSITIVE TEST: Successfully create a quote
   */
  describe('createQuote', () => {
    it('should successfully create a quote with valid data', async () => {
      // Arrange
      const mockResponse = {
        quoteNumber: 'QT-0000001',
        projectIdentifier: 'proj-001',
        contractorId: 'contractor-001',
        lineItems: [
          {
            lineItemId: 1,
            itemDescription: 'Service 1',
            quantity: 10,
            rate: 100.0,
            lineTotal: 1000.0,
          },
        ],
        totalAmount: 1000.0,
      };

      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockResponse),
        })
      );

      const quoteData = {
        projectIdentifier: 'proj-001',
        lineItems: [
          {
            itemDescription: 'Service 1',
            quantity: 10,
            rate: 100.0,
            displayOrder: 1,
          },
        ],
      };

      // Act
      const result = await quoteApi.createQuote(quoteData, 'token-123');

      // Assert
      expect(result).toEqual(mockResponse);
      expect(result.quoteNumber).toBe('QT-0000001');
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('/quotes'),
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Content-Type': 'application/json',
          }),
        })
      );
    });

    /**
     * NEGATIVE TEST: Handle API error during quote creation
     */
    it('should throw error when API returns 400 Bad Request', async () => {
      // Arrange
      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: false,
          status: 400,
          text: () => Promise.resolve('Invalid quote data'),
        })
      );

      const quoteData = {
        projectIdentifier: 'proj-001',
        lineItems: [],
      };

      // Act & Assert
      await expect(quoteApi.createQuote(quoteData, 'token-123')).rejects.toThrow(
        'Invalid quote data'
      );
    });

    /**
     * NEGATIVE TEST: Handle network error
     */
    it('should throw error when network is unavailable', async () => {
      // Arrange
      global.fetch = vi.fn(() =>
        Promise.reject(new TypeError('fetch failed'))
      );

      const quoteData = {
        projectIdentifier: 'proj-001',
        lineItems: [
          {
            itemDescription: 'Service',
            quantity: 10,
            rate: 100,
            displayOrder: 1,
          },
        ],
      };

      // Act & Assert
      await expect(quoteApi.createQuote(quoteData, 'token-123')).rejects.toThrow(
        'Network error'
      );
    });
  });

  /**
   * GET Quotes Tests
   */
  describe('getQuotesByProject', () => {
    /**
     * POSITIVE TEST: Successfully fetch quotes for project
     */
    it('should fetch all quotes for a project', async () => {
      // Arrange
      const mockQuotes = [
        {
          quoteNumber: 'QT-0000001',
          projectIdentifier: 'proj-001',
          totalAmount: 1000.0,
        },
        {
          quoteNumber: 'QT-0000002',
          projectIdentifier: 'proj-001',
          totalAmount: 2000.0,
        },
      ];

      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockQuotes),
        })
      );

      // Act
      const result = await quoteApi.getQuotesByProject('proj-001', 'token-123');

      // Assert
      expect(result).toHaveLength(2);
      expect(result[0].quoteNumber).toBe('QT-0000001');
      expect(result[1].quoteNumber).toBe('QT-0000002');
    });

    /**
     * NEGATIVE TEST: Handle 404 when project has no quotes
     */
    it('should throw error when project not found', async () => {
      // Arrange
      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: false,
          status: 404,
          text: () => Promise.resolve('Project not found'),
        })
      );

      // Act & Assert
      await expect(
        quoteApi.getQuotesByProject('invalid-proj', 'token-123')
      ).rejects.toThrow('Failed to fetch quotes');
    });
  });

  /**
   * GET Specific Quote Tests
   */
  describe('getQuoteByNumber', () => {
    /**
     * POSITIVE TEST: Successfully fetch specific quote
     */
    it('should fetch a specific quote by number', async () => {
      // Arrange
      const mockQuote = {
        quoteNumber: 'QT-0000001',
        projectIdentifier: 'proj-001',
        contractorId: 'contractor-001',
        totalAmount: 1000.0,
        lineItems: [
          {
            lineItemId: 1,
            itemDescription: 'Service',
            quantity: 10,
            rate: 100.0,
            lineTotal: 1000.0,
          },
        ],
      };

      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockQuote),
        })
      );

      // Act
      const result = await quoteApi.getQuoteByNumber('QT-0000001', 'token-123');

      // Assert
      expect(result).toEqual(mockQuote);
      expect(result.quoteNumber).toBe('QT-0000001');
    });

    /**
     * NEGATIVE TEST: Handle 404 when quote not found
     */
    it('should throw error when quote not found', async () => {
      // Arrange
      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: false,
          status: 404,
          text: () => Promise.resolve('Quote not found'),
        })
      );

      // Act & Assert
      await expect(
        quoteApi.getQuoteByNumber('QT-9999999', 'token-123')
      ).rejects.toThrow('Quote not found');
    });
  });

  /**
   * GET My Quotes Tests
   */
  describe('getMyQuotes', () => {
    /**
     * POSITIVE TEST: Successfully fetch contractor's quotes
     */
    it('should fetch all quotes for current contractor', async () => {
      // Arrange
      const mockQuotes = [
        { quoteNumber: 'QT-0000001', projectIdentifier: 'proj-001' },
        { quoteNumber: 'QT-0000003', projectIdentifier: 'proj-002' },
      ];

      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: true,
          json: () => Promise.resolve(mockQuotes),
        })
      );

      // Act
      const result = await quoteApi.getMyQuotes('token-123');

      // Assert
      expect(result).toHaveLength(2);
      expect(global.fetch).toHaveBeenCalledWith(
        expect.stringContaining('my-quotes'),
        expect.any(Object)
      );
    });

    /**
     * NEGATIVE TEST: Handle 401 Unauthorized
     */
    it('should throw error when not authenticated', async () => {
      // Arrange
      global.fetch = vi.fn(() =>
        Promise.resolve({
          ok: false,
          status: 401,
          text: () => Promise.resolve('Unauthorized'),
        })
      );

      // Act & Assert
      await expect(quoteApi.getMyQuotes('invalid-token')).rejects.toThrow(
        'Failed to fetch quotes'
      );
    });
  });
});
