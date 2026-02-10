const CACHE_NAME = 'mycar-log-v1';
const STATIC_ASSETS = [
    '/css/style.css',
    '/js/app.js',
    '/offline.html',
    '/icons/icon.svg'
];

// Install: pre-cache static assets
self.addEventListener('install', event => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(STATIC_ASSETS))
            .then(() => self.skipWaiting())
    );
});

// Activate: clean up old caches
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys()
            .then(keys => Promise.all(
                keys.filter(key => key !== CACHE_NAME)
                    .map(key => caches.delete(key))
            ))
            .then(() => self.clients.claim())
    );
});

// Fetch: strategy per request type
self.addEventListener('fetch', event => {
    const { request } = event;

    // Skip non-GET requests
    if (request.method !== 'GET') return;

    // Skip OAuth/auth related requests
    if (request.url.includes('/oauth2/') || request.url.includes('/login/oauth2/')) {
        return;
    }

    // Navigation requests: network first, offline fallback
    if (request.mode === 'navigate') {
        event.respondWith(
            fetch(request)
                .catch(() => caches.match('/offline.html'))
        );
        return;
    }

    // Static assets (CSS, JS, images, icons): cache first
    if (request.destination === 'style' ||
        request.destination === 'script' ||
        request.destination === 'image' ||
        request.url.includes('/icons/')) {
        event.respondWith(
            caches.match(request).then(cached => {
                if (cached) return cached;
                return fetch(request).then(response => {
                    if (response.ok) {
                        const clone = response.clone();
                        caches.open(CACHE_NAME).then(cache => cache.put(request, clone));
                    }
                    return response;
                });
            })
        );
        return;
    }

    // All other requests: network only
    event.respondWith(fetch(request));
});
