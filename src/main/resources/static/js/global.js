// /static/js/global.js

/**
 * Получает CSRF-токены из мета-тегов и готовит их для HTTP-запросов.
 * @returns {object} Объект с функциями для получения токенов и заголовков.
 */
function getCsrfHeaders() {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');

    if (!tokenMeta || !headerMeta) {
        console.error("CSRF tokens not found in meta tags.");
        return { getHeaders: () => ({}) };
    }

    const token = tokenMeta.content;
    const headerName = headerMeta.content;

    // Возвращает готовый объект заголовков для использования в fetch
    return {
        getHeaders: function(additionalHeaders = {}) {
            return {
                ...additionalHeaders,
                [headerName]: token
            };
        }
    };
}

/**
 * Обертка над fetch, автоматически добавляющая CSRF-токены и Content-Type.
 * Предполагает, что тело запроса (body) - это JSON.
 * @param {string} url - URL для запроса.
 * @param {object} options - Опции Fetch.
 * @returns {Promise<Response>}
 */
export function authenticatedFetch(url, options = {}) {
    const csrf = getCsrfHeaders();

    const defaultHeaders = {
        'Content-Type': 'application/json',
    };

    // Объединяем все заголовки
    const finalHeaders = csrf.getHeaders({
        ...defaultHeaders,
        ...options.headers
    });

    const finalOptions = {
        ...options,
        headers: finalHeaders
    };

    return fetch(url, finalOptions);
}


/**
 * Инициализирует обработку кликабельных строк в таблицах.
 */
function initializeClickableRows() {
    const rows = document.querySelectorAll("tr.clickable-row");
    rows.forEach(row => {
        row.addEventListener("click", () => {
            // Ищем атрибут data-href, который Thymeleaf оставляет после обработки
            const href = row.getAttribute("data-href") || row.querySelector("a")?.href;
            if (href) {
                window.location.href = href;
            }
        });
    });
}

// Запуск общей логики при загрузке DOM
document.addEventListener('DOMContentLoaded', initializeClickableRows);