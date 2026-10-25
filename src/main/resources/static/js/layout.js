/**
 * Renders the shared top navbar and (on dashboard pages) the role sidebar.
 * Each page sets `window.SC_BASE` to the relative path back to /static
 * before including this script (e.g. '../../' for pages two folders deep,
 * '' for the root index.html).
 */
(function () {
  const BASE = typeof window.SC_BASE === 'string' ? window.SC_BASE : '';

  function publicNav() {
    const user = SmartCareAPI.getCurrentUser();
    const loggedIn = SmartCareAPI.isLoggedIn() && user;

    const dashboardHref = !loggedIn ? '#' :
      user.role === 'ROLE_PATIENT' ? `${BASE}pages/patient/dashboard.html` :
      user.role === 'ROLE_DOCTOR' ? `${BASE}pages/doctor/dashboard.html` :
      `${BASE}pages/admin/dashboard.html`;

    const actions = loggedIn
      ? `<span class="text-small text-muted">${SmartCareUI.escapeHtml(user.fullName)}</span>
         <a class="btn btn-outline btn-sm" href="${dashboardHref}">Dashboard</a>
         <button class="btn btn-ghost btn-sm" id="sc-logout-btn" type="button">Log out</button>`
      : `<a class="btn btn-ghost btn-sm" href="${BASE}pages/public/login.html">Log in</a>
         <a class="btn btn-primary btn-sm" href="${BASE}pages/public/register.html">Book an appointment</a>`;

    return `
      <nav class="sc-navbar">
        <div class="container-app">
          <a class="sc-logo" href="${BASE}index.html">
            <span class="mark">SC</span> SmartCare
          </a>
          <ul class="sc-nav-links">
            <li><a href="${BASE}index.html">Home</a></li>
            <li><a href="${BASE}pages/public/departments.html">Departments</a></li>
            <li><a href="${BASE}pages/public/doctors.html">Doctors</a></li>
            <li><a href="${BASE}pages/public/about.html">About</a></li>
            <li><a href="${BASE}pages/public/contact.html">Contact</a></li>
          </ul>
          <div class="sc-nav-actions">${actions}</div>
        </div>
      </nav>`;
  }

  function footer() {
    return `
      <footer class="sc-footer">
        <div class="container-app">
          <span>&copy; ${new Date().getFullYear()} SmartCare. A student/portfolio project - not a real hospital system.</span>
          <span>
            <a href="${BASE}pages/public/about.html">About</a> &nbsp;·&nbsp;
            <a href="${BASE}pages/public/contact.html">Contact</a>
          </span>
        </div>
      </footer>`;
  }

  const SIDEBARS = {
    ROLE_PATIENT: [
      ['Dashboard', 'dashboard.html'],
      ['Find a Doctor', 'find-doctor.html'],
      ['Book Appointment', 'book-appointment.html'],
      ['My Appointments', 'my-appointments.html'],
      ['Live Queue', 'live-queue.html'],
      ['Family Members', 'family-members.html'],
      ['Medical Timeline', 'medical-timeline.html'],
      ['Prescriptions', 'prescriptions.html'],
      ['Notifications', 'notifications.html'],
      ['My Profile', 'profile.html'],
    ],
    ROLE_DOCTOR: [
      ['Dashboard', 'dashboard.html'],
      ['Today\u2019s Appointments', 'todays-appointments.html'],
      ['Check-In', 'check-in.html'],
      ['Live Queue', 'live-queue.html'],
      ['Schedule', 'schedule.html'],
      ['My Profile', 'profile.html'],
    ],
    ROLE_ADMIN: [
      ['Dashboard', 'dashboard.html'],
      ['Doctors', 'manage-doctors.html'],
      ['Departments', 'manage-departments.html'],
      ['Appointments', 'manage-appointments.html'],
      ['Check-In', 'check-in.html'],
      ['Analytics', 'analytics.html'],
    ],
  };

  function sidebar(role, currentFile) {
    const items = SIDEBARS[role] || [];
    const links = items.map(([label, file]) => {
      const active = file === currentFile ? ' class="active"' : '';
      return `<li><a${active} href="${file}">${label}</a></li>`;
    }).join('');
    return `<aside class="dash-sidebar"><ul class="sc-nav-links">${links}</ul></aside>`;
  }

  function mount() {
    const navSlot = document.getElementById('sc-navbar-slot');
    const footerSlot = document.getElementById('sc-footer-slot');
    if (navSlot) navSlot.outerHTML = publicNav();
    if (footerSlot) footerSlot.outerHTML = footer();

    const sidebarSlot = document.getElementById('sc-sidebar-slot');
    if (sidebarSlot) {
      const user = SmartCareAPI.getCurrentUser();
      const currentFile = window.location.pathname.split('/').pop();
      if (user) sidebarSlot.outerHTML = sidebar(user.role, currentFile);
    }

    const logoutBtn = document.getElementById('sc-logout-btn');
    if (logoutBtn) {
      logoutBtn.addEventListener('click', () => {
        SmartCareAPI.clearSession();
        window.location.href = `${BASE}index.html`;
      });
    }
  }

  document.addEventListener('DOMContentLoaded', mount);
})();
