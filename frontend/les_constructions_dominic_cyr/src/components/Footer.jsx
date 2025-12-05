import '../styles/footer.css';

const Footer = () => {
    return (
        <footer className="footer">
            <div className="footer-container">
                <div className="footer-section">
                    <h3>
                        <span className="footer-icon">👤</span>
                        Contact
                    </h3>
                    <p>Isabelle Misiazeck</p>
                    <p>514-123-4567</p>
                    <a href="mailto:isabelle.misiazeck@foresta.ca">
                        isabelle.misiazeck@foresta.ca
                    </a>
                </div>

                <div className="footer-section">
                    <h3>
                        <span className="footer-icon">🕐</span>
                        Opening Hours
                    </h3>
                    <p>Monday to Wednesday: 1 p.m to 7 p.m</p>
                    <p>Saturday and Sunday: 11 a.m. to 5 p.m. </p>
                </div>

                <div className="footer-section">
                    <h3>
                        <span className="footer-icon">📍</span>
                        Office
                    </h3>
                    <p>104 rue du Boisé</p>
                    <p>St-Alphonse de Granby</p>
                    <p>Granby, QC J2J 2X4</p>
                </div>
            </div>

            <div className="footer-bottom">
                <p>&copy; 2025 Les Constructions Dominic Cyr. All rights reserved.</p>
            </div>
        </footer>
    );
};

export default Footer;