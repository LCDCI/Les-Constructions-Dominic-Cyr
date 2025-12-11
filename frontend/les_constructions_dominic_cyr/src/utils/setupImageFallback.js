const FALLBACK_SRC =
    (import.meta.env.BASE_URL ?? '') + 'fallback.jpg';

function isImage(el) {
    return el?.tagName?.toUpperCase() === 'IMG';
}

function clearResponsiveSources(img) {
    try { img.removeAttribute('srcset'); } catch {}
    const picture = img.closest?.('picture');
    if (picture) {
        picture.querySelectorAll('source').forEach(source => {
            try { source.removeAttribute('srcset'); } catch {}
        });
    }
}

function swapToFallback(img) {
    if (img.dataset?.fallbackHandled === 'true') return;
    img.dataset.fallbackHandled = 'true';
    clearResponsiveSources(img);
    img.src = FALLBACK_SRC;
}

function handleImageError(event) {
    const target = event.target;
    if (!isImage(target)) return;
    swapToFallback(target);
}

window.addEventListener('error', handleImageError, true);

export default function initImageFallback() {
    /* already initialized on import */
}
