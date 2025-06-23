// Smooth animation for <details>
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('details').forEach(function (detail) {
        detail.addEventListener('toggle', function () {
            if (detail.open) {
                detail.classList.add('open');
            } else {
                detail.classList.remove('open');
            }
        });
    });
});
