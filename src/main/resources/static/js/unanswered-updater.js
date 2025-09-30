/**
 * Файл: unanswered-updater.js
 * Обновляет список неотвеченных обращений и счетчик в фавиконе.
 */

const CONTAINER_ID = 'unanswered-requests-container';
const API_URL = '/request-reviews/unanswered-fragment';
const COUNT_API_URL = '/request-reviews/unanswered-count';
const REFRESH_INTERVAL_MS = 60000; // 1 минута (60000 мс)

/**
 * === ФУНКЦИЯ ОБНОВЛЕНИЯ ФАВИКОНА ===
 * Динамически рисует счетчик на исходной иконке сайта.
 */
function updateFaviconCount(count) {
    const favicon = document.querySelector('link[rel="icon"]');
    if (!favicon) return;

    // Сохраняем оригинальный путь к иконке
    const originalHref = favicon.href;

    // 1. Если счетчик равен 0, восстанавливаем исходную иконку и заголовок
    if (count === 0) {
        // Восстанавливаем оригинальный href, если он был изменен Canvas Data URL
        if (favicon.dataset.originalHref) {
            favicon.href = favicon.dataset.originalHref;
        }
        document.title = document.title.replace(/\s*\(\d+\)/, ''); // Очистка заголовка
        return;
    }

    // Сохраняем оригинальный путь, если его нет
    if (!favicon.dataset.originalHref) {
        favicon.dataset.originalHref = originalHref;
    }

    // 2. Создаем холст и контекст
    const canvas = document.createElement('canvas');
    const size = 32; // Размер фавикона
    canvas.width = size;
    canvas.height = size;
    const ctx = canvas.getContext('2d');

    // 3. Создаем объект Image для загрузки исходной иконки
    const img = new Image();
    img.crossOrigin = 'Anonymous';

    // Этот код выполнится после загрузки изображения
    img.onload = function() {
        // 4. Рисуем исходную иконку как фон
        ctx.drawImage(img, 0, 0, size, size);

        // 5. Рисуем фон (красный круг) в углу
        const centerX = size - 10;
        const centerY = size - 10;
        const radius = 10;
        ctx.beginPath();
        ctx.arc(centerX, centerY, radius, 0, 2 * Math.PI);
        ctx.fillStyle = '#dc3545'; // Красный
        ctx.fill();

        // 6. Рисуем текст (счетчик)
        ctx.font = 'bold 14px Arial';
        ctx.fillStyle = 'white';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        const countText = count > 99 ? '99+' : String(count);

        ctx.fillText(countText, centerX, centerY + 1);

        // 7. Заменяем фавикон Data URL
        favicon.href = canvas.toDataURL('image/png');
    };

    img.onerror = function() {
        console.warn('Не удалось загрузить исходную иконку для фона.');
        // В случае ошибки, можно реализовать fallback без фона
    };

    // Указываем источник изображения и запускаем загрузку
    img.src = originalHref;

    // 8. Обновляем заголовок страницы (происходит сразу)
    document.title = `(${count}) ${document.title.replace(/\s*\(\d+\)/, '')}`;
}

/**
 * Асинхронно получает только счетчик и обновляет фавикон.
 */
function refreshCountAndFavicon() {
    fetch(COUNT_API_URL)
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch count.');
            return response.json(); // Ожидаем число
        })
        .then(count => {
            updateFaviconCount(count);
        })
        .catch(error => {
            console.error('Ошибка при обновлении счетчика:', error);
        });
}

// Переменная для хранения интервала, чтобы можно было его остановить
let updateInterval;

/**
 * Асинхронно получает обновленный HTML-фрагмент и заменяет его.
 */
function refreshUnansweredList() {
    const container = document.getElementById(CONTAINER_ID);

    if (!container) {
        console.warn(`Элемент с ID ${CONTAINER_ID} не найден. Обновление списка остановлено.`);
        // Останавливаем интервал, если не нашли контейнер, чтобы не спамить запросами
        clearInterval(updateInterval);
        return;
    }

    fetch(API_URL)
        .then(response => {
            if (!response.ok) {
                throw new Error(`Ошибка сети или сервера: ${response.status}`);
            }
            return response.text();
        })
        .then(htmlFragment => {
            container.innerHTML = htmlFragment;
            console.log('Список "Неотвеченные" обновлен успешно.');
            initClickableRows();
        })
        .catch(error => {
            console.error('Ошибка при обновлении списка неотвеченных обращений:', error);
        });
}

/**
 * Инициализирует функционал кликабельных строк.
 */
function initClickableRows() {
    document.querySelectorAll('.clickable-row').forEach(row => {
        // Убеждаемся, что обработчик не вешается повторно
        if (!row.dataset.listenerAttached) {
            row.addEventListener('click', () => {
                const url = row.dataset.href;
                if (url) {
                    window.location.href = url;
                }
            });
            row.dataset.listenerAttached = 'true';
        }
    });
}

/**
 * Объединенная функция для запуска всех обновлений.
 */
function refreshAll() {
    refreshUnansweredList();
    refreshCountAndFavicon();
}


// === ЗАПУСК ===

// 1. Устанавливаем таймер для выполнения функции refreshAll каждую минуту
updateInterval = setInterval(refreshAll, REFRESH_INTERVAL_MS);

// 2. Вызываем инициализацию сразу после загрузки страницы
document.addEventListener('DOMContentLoaded', () => {
    // Инициализируем кликабельные строки, загруженные Thymeleaf'ом
    initClickableRows();
    // Запускаем первое обновление, чтобы не ждать минуту
    refreshAll();
});