/** Small shared helpers used across every page. */
const SmartCareUI = (() => {

  function ensureToastStack() {
    let stack = document.querySelector('.toast-stack');
    if (!stack) {
      stack = document.createElement('div');
      stack.className = 'toast-stack';
      document.body.appendChild(stack);
    }
    return stack;
  }

  function toast(message, type = 'info') {
    const stack = ensureToastStack();
    const el = document.createElement('div');
    el.className = `toast${type === 'error' ? ' toast-error' : ''}${type === 'success' ? ' toast-success' : ''}`;
    el.textContent = message;
    stack.appendChild(el);
    setTimeout(() => el.remove(), 4200);
  }

  function handleError(err, fallback = 'Something went wrong. Please try again.') {
    console.error(err);
    toast(err && err.message ? err.message : fallback, 'error');
  }

  function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString(undefined, { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
  }

  function formatTime(timeStr) {
    if (!timeStr) return '—';
    const [h, m] = timeStr.split(':');
    const hour = parseInt(h, 10);
    const suffix = hour >= 12 ? 'PM' : 'AM';
    const displayHour = ((hour + 11) % 12) + 1;
    return `${displayHour}:${m} ${suffix}`;
  }

  function statusBadgeClass(status) {
    const map = {
      BOOKED: 'badge-neutral',
      CONFIRMED: 'badge-teal',
      CHECKED_IN: 'badge-amber',
      IN_PROGRESS: 'badge-amber',
      COMPLETED: 'badge-green',
      CANCELLED: 'badge-brick',
      NO_SHOW: 'badge-brick',
      WAITING: 'badge-amber',
      CALLED: 'badge-teal',
      SKIPPED: 'badge-neutral',
      LOW: 'badge-green',
      MEDIUM: 'badge-amber',
      HIGH: 'badge-brick',
      SUCCESS: 'badge-green',
      PENDING: 'badge-amber',
      FAILED: 'badge-brick',
      REFUNDED: 'badge-neutral',
      AVAILABLE: 'badge-teal',
    };
    return map[status] || 'badge-neutral';
  }

  function requireAuth(allowedRoles) {
    const user = SmartCareAPI.getCurrentUser();
    if (!SmartCareAPI.isLoggedIn() || !user) {
      window.location.href = '/pages/public/login.html';
      return null;
    }
    if (allowedRoles && !allowedRoles.includes(user.role)) {
      window.location.href = '/index.html';
      return null;
    }
    return user;
  }

  function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  return { toast, handleError, formatDate, formatTime, statusBadgeClass, requireAuth, escapeHtml };
})();
