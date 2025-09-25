document.addEventListener('DOMContentLoaded', function() {
    // Обработчик для кликабельных строк (первая часть)
    const rows = document.querySelectorAll("tr.clickable-row");
    rows.forEach(row => {
        row.addEventListener("click", () => {
            const href = row.getAttribute("th:data-href") || row.querySelector("a").href;
            if (href) {
                window.location.href = href;
            }
        });
    });

    // Обработчик для кнопки сохранения (вторая часть)
    const saveButton = document.getElementById('save-note-button');
    const noteTextarea = document.getElementById('note-textarea');

    if (saveButton && noteTextarea) {
        saveButton.addEventListener('click', function() {
            const authorId = saveButton.dataset.authorId;
            const creatorId = saveButton.dataset.creatorId;
            const noteText = noteTextarea.value;

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            const noteData = {
                noteText: noteText,
                authorId: Number(authorId),
                creatorId: Number(creatorId)
            };

            const headers = {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            };

            fetch('/support-request/update-note', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(noteData),
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error('Ошибка сохранения заметки');
                    }
                    return response.json();
                })
                .then(data => {
                    console.log('Заметка успешно сохранена:', data);
                    alert('Заметка сохранена!');
                })
                .catch((error) => {
                    console.error('Ошибка:', error);
                    alert('Ошибка при сохранении заметки.');
                });
        });
    }
});