const CACHE_NAME = 'lanhouse-v1';

self.addEventListener('install', event => {
    self.skipWaiting();
});

self.addEventListener('activate', event => {
    event.waitUntil(self.clients.claim());
});

// Recebe push notification do servidor
self.addEventListener('push', event => {
    let data = { title: 'LAN House', body: 'Notificação' };
    try {
        data = event.data.json();
    } catch (e) {
        data.body = event.data ? event.data.text() : 'Tempo esgotado!';
    }

    const options = {
        body: data.body,
        icon: '/icon.png',
        badge: '/icon.png',
        vibrate: [200, 100, 200],
        tag: 'lanhouse-notification',
        renotify: true,
        requireInteraction: true,
        actions: [
            { action: 'open', title: 'Abrir App' }
        ]
    };

    event.waitUntil(
        self.registration.showNotification(data.title, options)
    );
});

// Ao clicar na notificação, abre o app
self.addEventListener('notificationclick', event => {
    event.notification.close();
    event.waitUntil(
        self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then(clients => {
            if (clients.length > 0) {
                return clients[0].focus();
            }
            return self.clients.openWindow('/');
        })
    );
});
