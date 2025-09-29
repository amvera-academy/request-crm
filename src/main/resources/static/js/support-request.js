// /static/js/support-request.js

import { authenticatedFetch } from './global.js'; // Импорт утилиты

// --- Логика сохранения заметок ---
function initializeNoteSaving() {
    const saveButton = document.getElementById('save-note-button');
    const noteTextarea = document.getElementById('note-textarea');

    if (!saveButton || !noteTextarea) return;

    saveButton.addEventListener('click', function() {
        const authorId = saveButton.dataset.authorId;
        const creatorId = saveButton.dataset.creatorId;
        const noteText = noteTextarea.value;

        const noteData = {
            noteText: noteText,
            authorId: Number(authorId),
            creatorId: Number(creatorId)
        };

        authenticatedFetch('/support-request/update-note', {
            method: 'POST',
            body: JSON.stringify(noteData),
        })
            .then(response => {
                if (!response.ok) {
                    // Если статус 4xx или 5xx
                    throw new Error('Ошибка сохранения заметки. Статус: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                console.log('Заметка успешно сохранена:', data);
                alert('Заметка сохранена!');
            })
            .catch((error) => {
                console.error('Ошибка:', error);
                alert('Ошибка при сохранении заметки. Проверьте консоль.');
            });
    });
}


// --- Логика отправки ответного сообщения ---
function initializeMessageSending() {
    const sendButton = document.getElementById('send-message-button');
    const formContainer = document.getElementById('send-message-form'); // Используем контейнер с data-request-id
    const messageTextInput = document.getElementById('message-text-input');

    if (!sendButton || !formContainer || !messageTextInput) return;

    sendButton.addEventListener('click', function() {
        const messageText = messageTextInput.value.trim();
        const supportRequestId = formContainer.getAttribute('data-request-id');

        if (messageText.length === 0) {
            alert('Сообщение не может быть пустым.');
            return;
        }

        const requestData = {
            supportRequestId: Number(supportRequestId),
            text: messageText
        };

        authenticatedFetch('/support-request/answer-to-request', {
            method: 'POST',
            body: JSON.stringify(requestData)
        })
            .then(response => {
                if (response.ok) {
                    alert('Сообщение успешно отправлено!');
                    messageTextInput.value = ''; // Очистка поля
                    // 💡 Идея: После успешной отправки можно сделать AJAX-запрос
                    // для обновления истории сообщений без перезагрузки страницы.
                } else {
                    alert('Ошибка отправки сообщения. Сервер вернул ошибку.');
                }
            })
            .catch(error => {
                console.error('Ошибка:', error);
                alert('Ошибка сети или сервера при отправке сообщения.');
            });
    });
}


// Центральная точка входа для логики страницы
document.addEventListener('DOMContentLoaded', function() {
    initializeNoteSaving();
    initializeMessageSending();
});