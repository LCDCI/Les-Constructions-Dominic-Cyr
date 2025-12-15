// src/components/LogoutButton.jsx
import React from "react";
import { useAuth0 } from "@auth0/auth0-react";
import { clearAppSession } from "../auth/clearAppSession";

export default function LogoutButton({ className = "btn-portal", children = "Logout" }) {
    const { logout, isAuthenticated } = useAuth0();

    if (!isAuthenticated) return null;

    const handleLogout = () => {
        // Clear any cached user info (your app cache)
        clearAppSession();

        // Then end Auth0 session + clear Auth0 SDK cache and redirect to public landing page
        // IMPORTANT: the returnTo URL must be in Auth0 Allowed Logout URLs.
        logout({
            logoutParams: {
                returnTo: `${window.location.origin}/`, // your public landing page
            },
        });
    };

    return (
        <button type="button" className={className} onClick={handleLogout}>
            {children}
        </button>
    );
}
