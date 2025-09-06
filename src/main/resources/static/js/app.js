document.addEventListener("DOMContentLoaded", () => {
    const rows = document.querySelectorAll("tr.clickable-row");
    rows.forEach(row => {
        row.addEventListener("click", () => {
            const href = row.getAttribute("th:data-href") || row.querySelector("a").href;
            if (href) {
                window.location.href = href;
            }
        });
    });
});