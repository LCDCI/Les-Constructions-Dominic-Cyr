import React from 'react';
import InquiryForm from '../components/InquiryForm';
import '../styles/contact.css';

export default function ContactPage() {
    return (
        <div className="contact-page">
            <section className="contact-hero">
                <div className="contact-hero__content">
                    <p className="eyebrow">Get in touch</p>
                    <h1>We would love to hear about your project</h1>
                    <p className="subhead">
                        Share your vision and we will follow up promptly with answers, timelines, and next steps.
                    </p>
                </div>
            </section>

            <section className="contact-body">
                <div className="contact-grid">
                    <div className="contact-card">
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
                            <p>Mon – Wed: 1 p.m to 7 p.m</p>
                            <p>Sat – Sun: 11 a.m to 5 p.m</p>
                        </div>
                    </div>

                    <InquiryForm className="contact-form-wrapper" />
                </div>
            </section>
        </div>
    );
}
