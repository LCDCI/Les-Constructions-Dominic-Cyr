package mailer

import (
	"bytes"
	"encoding/json"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	pkg "mailer-service/pkg/mailer"
)

func TestNewService_CreatesServiceWithCorrectConfig(t *testing.T) {
	apiKey := "test-api-key"
	from := "test@example.com"

	svc := NewService(apiKey, from).(*service)

	if svc.apiKey != apiKey {
		t.Errorf("expected apiKey %s, got %s", apiKey, svc.apiKey)
	}
	if svc.from != from {
		t.Errorf("expected from %s, got %s", from, svc.from)
	}
	if svc.apiURL != "https://api.brevo.com/v3/smtp/email" {
		t.Errorf("expected apiURL https://api.brevo.com/v3/smtp/email, got %s", svc.apiURL)
	}
	if svc.httpClient == nil {
		t.Error("expected httpClient to be set")
	}
}

func TestService_Send_SuccessfulSend_ReturnsNil(t *testing.T) {
	// Create test server
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Verify request
		if r.Method != "POST" {
			t.Errorf("expected POST, got %s", r.Method)
		}
		if r.Header.Get("api-key") != "test-key" {
			t.Errorf("expected api-key header test-key, got %s", r.Header.Get("api-key"))
		}
		if r.Header.Get("Content-Type") != "application/json" {
			t.Errorf("expected Content-Type application/json, got %s", r.Header.Get("Content-Type"))
		}

		// Read and verify request body
		body, _ := io.ReadAll(r.Body)
		var brevoReq brevoRequest
		if err := json.Unmarshal(body, &brevoReq); err != nil {
			t.Fatalf("failed to unmarshal request: %v", err)
		}
		if brevoReq.Sender.Email != "from@example.com" {
			t.Errorf("expected sender email from@example.com, got %s", brevoReq.Sender.Email)
		}
		if len(brevoReq.To) != 1 || brevoReq.To[0].Email != "to@example.com" {
			t.Errorf("expected recipient to@example.com, got %v", brevoReq.To)
		}
		if brevoReq.Subject != "Test Subject" {
			t.Errorf("expected subject Test Subject, got %s", brevoReq.Subject)
		}
		if brevoReq.HTMLContent != "<p>Test Body</p>" {
			t.Errorf("expected body <p>Test Body</p>, got %s", brevoReq.HTMLContent)
		}

		// Return successful response
		brevoResp := brevoResponse{MessageID: "test-message-id"}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(brevoResp)
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test Subject",
		Body:    "<p>Test Body</p>",
	})

	if err != nil {
		t.Errorf("expected nil error, got %v", err)
	}
}

func TestService_Send_WithSenderName_IncludesNameInRequest(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		body, _ := io.ReadAll(r.Body)
		var brevoReq brevoRequest
		json.Unmarshal(body, &brevoReq)

		if brevoReq.Sender.Name != "Test Sender" {
			t.Errorf("expected sender name Test Sender, got %s", brevoReq.Sender.Name)
		}

		brevoResp := brevoResponse{MessageID: "test-id"}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		json.NewEncoder(w).Encode(brevoResp)
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:         "to@example.com",
		Subject:    "Test",
		Body:       "<p>Test</p>",
		SenderName: "Test Sender",
	})

	if err != nil {
		t.Errorf("expected nil error, got %v", err)
	}
}

func TestService_Send_WithoutSenderName_OmitsNameInRequest(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		body, _ := io.ReadAll(r.Body)
		var brevoReq brevoRequest
		json.Unmarshal(body, &brevoReq)

		if brevoReq.Sender.Name != "" {
			t.Errorf("expected empty sender name, got %s", brevoReq.Sender.Name)
		}

		brevoResp := brevoResponse{MessageID: "test-id"}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		json.NewEncoder(w).Encode(brevoResp)
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	if err != nil {
		t.Errorf("expected nil error, got %v", err)
	}
}

func TestService_Send_Non200StatusCode_ReturnsError(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusBadRequest)
		w.Write([]byte("Invalid API key"))
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	if err == nil {
		t.Error("expected error, got nil")
	}
	if !strings.Contains(err.Error(), "brevo API error") {
		t.Errorf("expected error to contain 'brevo API error', got %v", err)
	}
	if !strings.Contains(err.Error(), "400") {
		t.Errorf("expected error to contain status code 400, got %v", err)
	}
}

func TestService_Send_StatusCreated_ReturnsNil(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		brevoResp := brevoResponse{MessageID: "created-id"}
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		json.NewEncoder(w).Encode(brevoResp)
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	if err != nil {
		t.Errorf("expected nil error for StatusCreated, got %v", err)
	}
}

func TestService_Send_InvalidJSONResponse_LogsWarningButReturnsNil(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// Return OK status but invalid JSON
		w.WriteHeader(http.StatusOK)
		w.Write([]byte("invalid json"))
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	// Should return nil even if JSON unmarshal fails (status was OK)
	if err != nil {
		t.Errorf("expected nil error (status OK), got %v", err)
	}
}

func TestService_Send_500StatusCode_ReturnsError(t *testing.T) {
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Internal server error"))
	}))
	defer server.Close()

	svc := newServiceWithClient("test-key", "from@example.com", server.URL, server.Client())

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	if err == nil {
		t.Error("expected error, got nil")
	}
	if !strings.Contains(err.Error(), "500") {
		t.Errorf("expected error to contain status code 500, got %v", err)
	}
}

func TestService_Send_HTTPClientError_ReturnsError(t *testing.T) {
	// Create a client with a RoundTripper that always returns an error
	errorClient := &http.Client{
		Transport: &errorRoundTripper{},
	}

	svc := newServiceWithClient("test-key", "from@example.com", "https://api.brevo.com/v3/smtp/email", errorClient)

	err := svc.Send(&pkg.Mail{
		To:      "to@example.com",
		Subject: "Test",
		Body:    "<p>Test</p>",
	})

	if err == nil {
		t.Error("expected error, got nil")
	}
	if !strings.Contains(err.Error(), "failed to send request") {
		t.Errorf("expected error to contain 'failed to send request', got %v", err)
	}
}

// errorRoundTripper is a RoundTripper that always returns an error
type errorRoundTripper struct{}

func (e *errorRoundTripper) RoundTrip(*http.Request) (*http.Response, error) {
	return nil, &netError{message: "network error"}
}

type netError struct {
	message string
}

func (e *netError) Error() string {
	return e.message
}
