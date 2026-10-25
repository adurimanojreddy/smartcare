/**
 * SmartCare API client. Wraps fetch() with the JWT bearer token, JSON
 * handling, and consistent error surfacing from the backend's
 * ApiErrorResponse shape (see GlobalExceptionHandler on the server).
 */
const SmartCareAPI = (() => {
  const BASE_URL = '/api';
  const TOKEN_KEY = 'smartcare_token';
  const USER_KEY = 'smartcare_user';

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getCurrentUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function setSession(authResponse) {
    localStorage.setItem(TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(USER_KEY, JSON.stringify({
      userId: authResponse.userId,
      email: authResponse.email,
      fullName: authResponse.fullName,
      role: authResponse.role,
    }));
  }

  function clearSession() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  function isLoggedIn() {
    return !!getToken();
  }

  async function request(path, { method = 'GET', body, query } = {}) {
    let url = BASE_URL + path;
    if (query) {
      const params = Object.entries(query)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
      if (params) url += (url.includes('?') ? '&' : '?') + params;
    }

    const headers = { 'Content-Type': 'application/json' };
    const token = getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(url, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    if (response.status === 204) return null;

    let payload = null;
    const text = await response.text();
    if (text) {
      try { payload = JSON.parse(text); } catch (e) { payload = text; }
    }

    if (!response.ok) {
      const message = (payload && payload.message) ? payload.message : `Request failed (${response.status})`;
      const error = new Error(message);
      error.status = response.status;
      error.fieldErrors = payload && payload.fieldErrors;
      throw error;
    }

    return payload;
  }

  return {
    // ---- session ----
    getToken, getCurrentUser, setSession, clearSession, isLoggedIn,

    // ---- auth ----
    register: (data) => request('/auth/register', { method: 'POST', body: data }),
    login: (data) => request('/auth/login', { method: 'POST', body: data }),

    // ---- departments ----
    getDepartments: () => request('/departments'),
    getDepartment: (id) => request(`/departments/${id}`),
    createDepartment: (data) => request('/departments', { method: 'POST', body: data }),
    updateDepartment: (id, data) => request(`/departments/${id}`, { method: 'PUT', body: data }),
    deleteDepartment: (id) => request(`/departments/${id}`, { method: 'DELETE' }),

    // ---- doctors ----
    getDoctors: () => request('/doctors'),
    searchDoctors: (query) => request('/doctors/search', { query }),
    getDoctor: (id) => request(`/doctors/${id}`),
    getMyDoctorProfile: () => request('/doctors/me'),
    createDoctor: (data) => request('/doctors', { method: 'POST', body: data }),
    updateDoctor: (id, data) => request(`/doctors/${id}`, { method: 'PUT', body: data }),
    deleteDoctor: (id) => request(`/doctors/${id}`, { method: 'DELETE' }),

    // ---- doctor schedules & slots ----
    getSchedules: (doctorId) => request(`/doctors/${doctorId}/schedules`),
    addSchedule: (doctorId, data) => request(`/doctors/${doctorId}/schedules`, { method: 'POST', body: data }),
    removeSchedule: (doctorId, scheduleId) => request(`/doctors/${doctorId}/schedules/${scheduleId}`, { method: 'DELETE' }),
    generateSlots: (doctorId, from, to) => request(`/doctors/${doctorId}/slots/generate`, { method: 'POST', query: { from, to } }),
    getAvailableSlots: (doctorId, date) => request(`/doctors/${doctorId}/slots`, { query: { date } }),

    // ---- recommendations ----
    recommendDepartment: (symptoms) => request('/recommendations/department', { method: 'POST', body: { symptoms } }),
    recommendSlots: (params) => request('/recommendations/slots', { query: params }),

    // ---- patients ----
    getMyPatientProfile: () => request('/patients/me'),
    getPatient: (id) => request(`/patients/${id}`),
    updatePatient: (id, data) => request(`/patients/${id}`, { method: 'PUT', body: data }),
    getPatientAppointments: (id) => request(`/patients/${id}/appointments`),
    getPatientMedicalHistory: (id) => request(`/patients/${id}/medical-history`),

    // ---- family members ----
    getFamilyMembers: () => request('/family-members'),
    addFamilyMember: (data) => request('/family-members', { method: 'POST', body: data }),
    updateFamilyMember: (id, data) => request(`/family-members/${id}`, { method: 'PUT', body: data }),
    deleteFamilyMember: (id) => request(`/family-members/${id}`, { method: 'DELETE' }),
    getFamilyMemberHistory: (id) => request(`/family-members/${id}/medical-history`),

    // ---- appointments ----
    bookAppointment: (data) => request('/appointments', { method: 'POST', body: data }),
    getAppointment: (id) => request(`/appointments/${id}`),
    getAppointmentByCode: (code) => request(`/appointments/code/${code}`),
    getAppointmentsForDoctor: (doctorId, date) => request('/appointments', { query: { doctorId, date } }),
    cancelAppointment: (id, reason) => request(`/appointments/${id}/cancel`, { method: 'POST', body: { reason } }),
    rescheduleAppointment: (id, newSlotId) => request(`/appointments/${id}/reschedule`, { method: 'POST', body: { newSlotId } }),
    checkInById: (id) => request(`/appointments/${id}/check-in`, { method: 'POST' }),
    checkInByCode: (code) => request(`/appointments/qr-check-in/${code}`, { method: 'POST' }),

    // ---- queue ----
    getDoctorQueue: (doctorId) => request(`/queue/${doctorId}`),
    getQueueStatus: (appointmentId) => request(`/queue/appointment/${appointmentId}/status`),
    callNext: (doctorId) => request(`/queue/${doctorId}/call-next`, { method: 'POST' }),
    startConsultation: (appointmentId) => request(`/queue/${appointmentId}/start`, { method: 'POST' }),
    completeConsultation: (appointmentId) => request(`/queue/${appointmentId}/complete`, { method: 'POST' }),
    skipPatient: (appointmentId) => request(`/queue/${appointmentId}/skip`, { method: 'POST' }),
    markNoShow: (appointmentId) => request(`/queue/${appointmentId}/no-show`, { method: 'POST' }),

    // ---- payments ----
    createPayment: (data) => request('/payments', { method: 'POST', body: data }),
    processPayment: (paymentId) => request(`/payments/${paymentId}/process`, { method: 'POST' }),
    refundPayment: (paymentId) => request(`/payments/${paymentId}/refund`, { method: 'POST' }),
    getPaymentForAppointment: (appointmentId) => request(`/payments/appointment/${appointmentId}`),

    // ---- prescriptions ----
    createPrescription: (data) => request('/prescriptions', { method: 'POST', body: data }),
    getPrescription: (id) => request(`/prescriptions/${id}`),
    getPrescriptionForAppointment: (appointmentId) => request(`/prescriptions/appointment/${appointmentId}`),
    getPrescriptionsForPatient: (patientId) => request(`/prescriptions/patient/${patientId}`),

    // ---- lab tests ----
    requestLabTest: (data) => request('/lab-tests', { method: 'POST', body: data }),
    getLabTestsForPatient: (patientId) => request(`/lab-tests/patient/${patientId}`),
    updateLabTestResult: (id, resultNotes, status) => request(`/lab-tests/${id}/result`, { method: 'PUT', query: { resultNotes, status } }),

    // ---- notifications ----
    getNotifications: () => request('/notifications'),
    getUnreadNotifications: () => request('/notifications/unread'),
    markNotificationRead: (id) => request(`/notifications/${id}/read`, { method: 'PUT' }),

    // ---- admin analytics ----
    getAdminDashboard: () => request('/admin/dashboard'),
    getAdminAnalytics: (from, to) => request('/admin/analytics', { query: { from, to } }),
  };
})();
