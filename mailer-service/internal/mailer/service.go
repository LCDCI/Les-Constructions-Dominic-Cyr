package mailer

import (
	"bytes"
	"crypto/tls"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"log"
	"net/http"
	"strings"

	pkg "mailer-service/pkg/mailer"
)

type Service interface {
	Send(m *pkg.Mail) error
}

type service struct {
	apiKey           string
	from             string
	apiURL           string
	blockedRecipient emailBlocker
	httpClient       *http.Client
}

type emailBlocker map[string]struct{}

func newEmailBlocker(blockedRecipients []string) emailBlocker {
	blocker := make(emailBlocker, len(blockedRecipients))
	for _, recipient := range blockedRecipients {
		normalized := strings.ToLower(strings.TrimSpace(recipient))
		if normalized != "" {
			blocker[normalized] = struct{}{}
		}
	}
	return blocker
}

func (b emailBlocker) blocks(recipient string) bool {
	_, blocked := b[strings.ToLower(strings.TrimSpace(recipient))]
	return blocked
}

var ErrRecipientBlocked = errors.New("email recipient is blocked")

func NewService(apiKey string, from string, blockedRecipients ...[]string) Service {
	var recipients []string
	if len(blockedRecipients) > 0 {
		recipients = blockedRecipients[0]
	}

	tr := &http.Transport{
		TLSClientConfig: &tls.Config{InsecureSkipVerify: true},
	}
	return &service{
		apiKey:           apiKey,
		from:             from,
		apiURL:           "https://api.brevo.com/v3/smtp/email",
		blockedRecipient: newEmailBlocker(recipients),
		httpClient:       &http.Client{Transport: tr},
	}
}

// newServiceWithClient creates a service with a custom HTTP client (for testing)
func newServiceWithClient(apiKey, from, apiURL string, client *http.Client, blockedRecipients ...[]string) Service {
	var recipients []string
	if len(blockedRecipients) > 0 {
		recipients = blockedRecipients[0]
	}
	return &service{
		apiKey:           apiKey,
		from:             from,
		apiURL:           apiURL,
		blockedRecipient: newEmailBlocker(recipients),
		httpClient:       client,
	}
}

type brevoRequest struct {
	Sender      brevoSender      `json:"sender"`
	To          []brevoRecipient `json:"to"`
	Subject     string         `json:"subject"`
	HTMLContent string         `json:"htmlContent"`
}

type brevoSender struct {
	Name  string `json:"name,omitempty"`
	Email string `json:"email"`
}

type brevoRecipient struct {
	Email string `json:"email"`
	Name  string `json:"name,omitempty"`
}

type brevoResponse struct {
	MessageID string `json:"messageId"`
}

func (s *service) Send(m *pkg.Mail) error {
	if s.blockedRecipient.blocks(m.To) {
		log.Printf("Blocked email delivery to configured recipient")
		return ErrRecipientBlocked
	}

	log.Printf("Attempting to send email to %s with subject: %s via Brevo API", m.To, m.Subject)

	sender := brevoSender{
		Email: s.from,
	}
	if m.SenderName != "" {
		sender.Name = m.SenderName
	}

	reqBody := brevoRequest{
		Sender: sender,
		To: []brevoRecipient{
			{
				Email: m.To,
			},
		},
		Subject:     m.Subject,
		HTMLContent: m.Body,
	}

	jsonData, err := json.Marshal(reqBody)
	if err != nil {
		log.Printf("ERROR marshaling request: %v", err)
		return fmt.Errorf("failed to marshal request: %w", err)
	}

	req, err := http.NewRequest("POST", s.apiURL, bytes.NewBuffer(jsonData))
	if err != nil {
		log.Printf("ERROR creating request: %v", err)
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("api-key", s.apiKey)
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.httpClient.Do(req)
	if err != nil {
		log.Printf("ERROR sending request to Brevo API: %v", err)
		return fmt.Errorf("failed to send request: %w", err)
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		log.Printf("ERROR reading response: %v", err)
		return fmt.Errorf("failed to read response: %w", err)
	}

	if resp.StatusCode != http.StatusOK && resp.StatusCode != http.StatusCreated {
		log.Printf("ERROR: Brevo API returned status %d: %s", resp.StatusCode, string(body))
		return fmt.Errorf("brevo API error (status %d): %s", resp.StatusCode, string(body))
	}

	var brevoResp brevoResponse
	if err := json.Unmarshal(body, &brevoResp); err != nil {
		log.Printf("WARNING: Could not parse response, but status was OK. Response: %s", string(body))
	} else {
		log.Printf("SUCCESS: Email sent to %s via Brevo API. Message ID: %s", m.To, brevoResp.MessageID)
	}

	return nil
}

var _ Service = (*service)(nil)
