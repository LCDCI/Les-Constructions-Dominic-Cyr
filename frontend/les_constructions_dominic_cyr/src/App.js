import './App.css';
import InquiryForm from './components/InquiryForm';

function App() {

  return (
    <div className="App">
      {/* Hero Section */}
      <section className="hero">
        <div className="hero-content">
          <h1>Les Constructions Dominic Cyr</h1>
          <p className="tagline">Building excellence and comfort — one home at a time</p>
        </div>
      </section>

      {/* Contact Section */}
      <section className="contact-section">
        <div className="contact-container">
          <div className="contact-info">
            <h2>Contact</h2>
            <div className="info-item">
              <strong>Isabelle Mousseau</strong>
              <p>514-123-4567</p>
              <p>isabelle.mousseau@foresta.ca</p>
            </div>
            <div className="info-item">
              <strong>Office</strong>
              <p>104 rue du Boise</p>
              <p>St-Alphonse-de-Granby</p>
              <p>Granby, QC J2J 2X4</p>
            </div>
            <div className="info-item">
              <strong>Opening Hours</strong>
              <p>Monday to Wednesday: 1 p.m to 7 p.m</p>
              <p>Saturday and Sunday: 11 a.m. to 5 p.m.</p>
            </div>
          </div>

          <InquiryForm />
        </div>
      </section>
    </div>
  );
}

export default App;
