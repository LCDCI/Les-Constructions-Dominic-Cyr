-- data for lots
INSERT INTO lots (lot_identifier, location, price, dimensions, lot_status) VALUES ('c80a5ba7-2a24-42ad-a989-27bb1cb1bbd6', 'Location A', 100000.0, '50x100', 'AVAILABLE');
INSERT INTO lots (lot_identifier, location, price, dimensions, lot_status) VALUES ('44b2358d-478c-40e1-b85c-de6b917392eb', 'Location B', 150000.0, '60x120', 'SOLD');
INSERT INTO lots (lot_identifier, location, price, dimensions, lot_status) VALUES ('15b1d772-c770-4dec-8d26-e27001fffbc5', 'Location C', 200000.0, '70x140', 'PENDING');

INSERT INTO footer_content (section, label, value, display_order) VALUES
('contact', 'name', 'Isabelle Misiazeck', 1),
('contact', 'phone', '514-123-4567', 2),
('contact', 'email', 'isabelle.misiazeck@foresta.ca', 3),
('hours', 'weekday', 'Monday to Wednesday: 1 p.m to 7 p.m', 1),
('hours', 'weekend', 'Saturday and Sunday: 11 a.m. to 5 p.m.', 2),
('office', 'address_line1', '104 rue du Boisé', 1),
('office', 'address_line2', 'St-Alphonse de Granby', 2),
('office', 'address_line3', 'Granby, QC J2J 2X4', 3);

INSERT INTO projects (
    project_identifier,
    project_name,
    project_description,
    status,
    start_date,
    end_date,
    primary_color,
    tertiary_color,
    buyer_color,
    buyer_name,
    customer_id,
    lot_identifier,
    progress_percentage
) VALUES
      (
          'proj-001-foresta',
          'Foresta',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant houses.',
          'IN_PROGRESS',
          '2025-01-15',
          '2025-12-31',
          '#C8D5B9',
          '#8FA383',
          '#6B8E6F',
          'FORESTA RÉSIDENCES SECONDAIRES',
          'cust-001',
          'lot-001',
          35
      ),
      (
          'proj-002-naturest',
          'Naturest',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature. The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant houses.',
          'IN_PROGRESS',
          '2025-02-01',
          '2026-01-31',
          '#8B7355',
          '#C19A6B',
          '#D4AF7A',
          'NATUREST RÉSIDENCES SECONDAIRES',
          'cust-002',
          'lot-002',
          20
      ),
      (
          'proj-003-otryminc',
          'Otryminc',
          'Nestled in the serene woodlands of St-Alphonse-de-Granby, Foresta offers an inviting escape surrounded by nature.  The most recent project in our collection, Foresta began in 2025 and joins Les Construction Dominic Cyrs catalogue of modern and elegant houses.',
          'PLANNED',
          '2025-03-01',
          '2026-02-28',
          '#5A7D8C',
          '#7B9FAE',
          '#9CB5C3',
          'OTRYMINC MAISON DE VUE EN BOIS DE M-B',
          'cust-003',
          'lot-003',
          0
      );