// /static/js/support-request.js

import { authenticatedFetch } from './global.js'; // Импорт утилиты

// --- Логика сохранения заметок (Без изменений) ---
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


// --- Логика отправки ответного сообщения (ОБНОВЛЕННАЯ ВЕРСИЯ) ---
function initializeMessageSending() {
    const sendButton = document.getElementById('send-message-button');
    const formContainer = document.getElementById('send-message-form');
    const messageTextInput = document.getElementById('message-text-input');

    if (!sendButton || !formContainer || !messageTextInput) return;

    // Используем async/await для более удобной работы с чтением тела ответа
    sendButton.addEventListener('click', async function() { // <-- Добавляем async
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

        try {
            const response = await authenticatedFetch('/support-request/answer-to-request', { // <-- await
                method: 'POST',
                body: JSON.stringify(requestData)
            });

            if (response.ok) {
                alert('Сообщение успешно отправлено!');
                messageTextInput.value = ''; // Очистка поля
                window.location.reload();
                // Здесь можно добавить AJAX-обновление истории сообщений

            } else {
                // Обработка ошибок сервера (4xx, 5xx)
                let errorMessage = `Ошибка отправки сообщения. Сервер вернул статус ${response.status}.`;

                // КЛЮЧЕВАЯ ЛОГИКА: Читаем JSON-тело для получения точной причины (если оно есть)
                if (response.status >= 400) {
                    try {
                        const errorData = await response.json();

                        if (errorData && errorData.message) {
                            // Если JSON содержит поле 'message' (отправленное из Java-контроллера)
                            errorMessage = errorData.message;
                        }
                    } catch (e) {
                        // Игнорируем ошибку парсинга, если тело не JSON
                        console.warn("Не удалось прочитать специфическое JSON-сообщение об ошибке:", e);
                    }
                }

                alert(errorMessage);
            }
        } catch (error) {
            // Ошибка сети или ошибка, брошенная самим authenticatedFetch
            console.error('Ошибка:', error);
            alert('Ошибка сети или сервера при отправке сообщения.');
        }
    });
}


// Центральная точка входа для логики страницы
document.addEventListener('DOMContentLoaded', function() {
    initializeNoteSaving();
    initializeMessageSending();
});