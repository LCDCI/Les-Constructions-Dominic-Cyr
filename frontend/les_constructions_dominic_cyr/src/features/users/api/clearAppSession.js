// src/auth/clearAppSession.js

import axios from "axios";

/**
 * Clears any app-side cached user/session info.
 * Auth0 tokens are cleared by auth0-react's logout(), but apps often keep extra cache
 * (user profile, roles, portal state, axios headers, etc).
 */
export function clearAppSession() {
    // 1) Remove any app-specific storage keys you created
    // (adjust these to your real keys)
    const APP_KEYS = [
        "user",
        "userProfile",
        "roles",
        "portal",
        "selectedPortal",
        "lastVisitedRoute",
    ];
    APP_KEYS.forEach((k) => {
        localStorage.removeItem(k);
        sessionStorage.removeItem(k);
    });

    // 2) Clear ALL sessionStorage (safe if you don’t store important non-auth things there)
    // sessionStorage.clear();

    // 3) Clear Auth0 SPA cache keys if they exist (especially if cacheLocation=localstorage)
    // We don't rely on exact key names; we remove common prefixes.
    const purgeByPrefix = (storage, prefixes) => {
        const keys = [];
        for (let i = 0; i < storage.length; i++) keys.push(storage.key(i));
        keys
            .filter((k) => k && prefixes.some((p) => k.startsWith(p)))
            .forEach((k) => storage.removeItem(k));
    };

    purgeByPrefix(localStorage, [
        "auth0",         // common
        "@@auth0spajs@@",// auth0-spa-js cache prefix
    ]);
    purgeByPrefix(sessionStorage, [
        "auth0",
        "@@auth0spajs@@",
    ]);

    // 4) Clear axios auth header if you set one globally anywhere
    delete axios.defaults.headers.common.Authorization;
}
