const API_BASE = "";
const clinicContent = document.querySelector("#clinic-content");
const loginForm = document.querySelector("#login-form");
const loginButton = document.querySelector("#login-button");
const loginView = document.querySelector("#login-view");
const sessionView = document.querySelector("#session-view");
const currentUserLabel = document.querySelector("#current-user-label");
const roleNote = document.querySelector("#role-note");
const authStatus = document.querySelector("#auth-status");
const logoutButton = document.querySelector("#logout-button");
const petRows = document.querySelector("#pet-rows");
const petListStatus = document.querySelector("#pet-list-status");
const appointmentRows = document.querySelector("#appointment-rows");
const appointmentListStatus = document.querySelector("#appointment-list-status");
const detailStatus = document.querySelector("#detail-status");
const detailContainer = document.querySelector("#pet-detail");
const previousButton = document.querySelector("#previous-button");
const nextButton = document.querySelector("#next-button");
const pageLabel = document.querySelector("#page-label");

const petForm = document.querySelector("#pet-form");
const petSubmit = document.querySelector("#pet-submit");
const petCancel = document.querySelector("#pet-cancel");
const petFormStatus = document.querySelector("#pet-form-status");
const ownerSelect = document.querySelector("#pet-owner");
const appointmentForm = document.querySelector("#appointment-form");
const appointmentSubmit = document.querySelector("#appointment-submit");
const appointmentCancel = document.querySelector("#appointment-cancel");
const appointmentFormStatus = document.querySelector("#appointment-form-status");
const appointmentDate = document.querySelector("#appointment-date");
const appointmentVet = document.querySelector("#appointment-vet");
const availabilityHint = document.querySelector("#availability-hint");
const appointmentPet = document.querySelector("#appointment-pet");

let currentPage = 0;
let totalPages = 0;
let editingPetId = null;
let editingAppointmentId = null;
let currentAppointments = [];
let currentPets = [];
let petSaving = false;
let appointmentSaving = false;
let csrfState = null;
let currentUser = null;
let clinicInitialized = false;

async function refreshCsrfToken() {
  const response = await fetch(`${API_BASE}/api/auth/csrf`, {
    method: "GET",
    credentials: "same-origin",
    headers: { Accept: "application/json" }
  });
  if (!response.ok) throw new Error(`Could not obtain CSRF token (${response.status}).`);
  csrfState = await response.json();
  return csrfState;
}

async function request(path, options = {}) {
  const method = (options.method || "GET").toUpperCase();
  const headers = new Headers(options.headers || {});
  if (["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
    if (!csrfState) await refreshCsrfToken();
    headers.set(csrfState.headerName, csrfState.token);
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    method,
    headers,
    credentials: "same-origin"
  });
  const type = response.headers.get("content-type") || "";
  const body = response.status === 204 ? null
    : type.includes("application/json") ? await response.json()
    : await response.text();

  if (!response.ok) {
    if (response.status === 401 && currentUser && path !== "/api/auth/me") {
      applyAuthenticatedUser(null);
      setStatus(authStatus, "Your session expired. Please sign in again.", true);
    }
    const message = typeof body === "object" && body !== null
      ? (body.message || body.detail || `Request failed (${response.status})`)
      : `Request failed (${response.status})`;
    const error = new Error(message);
    error.status = response.status;
    error.details = body;
    throw error;
  }
  return body;
}

function jsonOptions(method, payload) {
  return {
    method,
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: JSON.stringify(payload)
  };
}

function makeCell(value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "—";
  return cell;
}


function clearFieldErrors(form) {
  for (const node of form.querySelectorAll("[data-error-for]")) node.textContent = "";
}

function showFormError(form, statusNode, error) {
  clearFieldErrors(form);
  const message = error?.message || "The request could not be completed.";
  const raw = error?.details?.message;
  if (typeof raw === "string") {
    for (const part of raw.split(";")) {
      const match = part.trim().match(/^([A-Za-z][A-Za-z0-9]*):\s*(.+)$/);
      if (!match) continue;
      const fieldError = form.querySelector(`[data-error-for="${match[1]}"]`);
      if (fieldError) fieldError.textContent = match[2];
    }
  }
  setStatus(statusNode, message, true);
}

function makeButton(label, className, onClick) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = className;
  button.textContent = label;
  button.addEventListener("click", onClick);
  return button;
}

function setStatus(target, message, isError = false) {
  target.textContent = message;
  target.classList.toggle("status-error", isError);
}

function resetPetForm() {
  petForm.reset();
  clearFieldErrors(petForm);
  editingPetId = null;
  petSubmit.textContent = "Add pet";
  petCancel.hidden = true;
  setStatus(petFormStatus, "");
}

function resetAppointmentForm() {
  appointmentForm.reset();
  clearFieldErrors(appointmentForm);
  editingAppointmentId = null;
  appointmentSubmit.textContent = "Book appointment";
  appointmentCancel.hidden = true;
  setStatus(appointmentFormStatus, "");
  setStatus(availabilityHint, "Choose a date to load veterinarian availability.");
  appointmentVet.replaceChildren(new Option("Choose a date first", ""));
}

async function loadOwners() {
  const owners = await request("/api/owners", { headers: { Accept: "application/json" } });
  ownerSelect.replaceChildren(new Option("Select an owner", ""));
  for (const owner of owners) {
    ownerSelect.add(new Option(`${owner.fullName} · ${owner.phoneNumber}`, String(owner.id)));
  }
}

async function loadPetChoices() {
  currentPets = await request("/api/pets", { headers: { Accept: "application/json" } });
  appointmentPet.replaceChildren(new Option("Select a pet", ""));
  for (const pet of currentPets) {
    appointmentPet.add(new Option(`${pet.name} · ${pet.species} · ${pet.microchipNumber}`, String(pet.id)));
  }
}

function renderPets(rows) {
  petRows.replaceChildren();
  for (const pet of rows) {
    const row = document.createElement("tr");
    row.append(makeCell(pet.name));
    row.append(makeCell(pet.microchipNumber));
    row.append(makeCell(pet.species));
    row.append(makeCell(pet.breed));
    row.append(makeCell(pet.ownerName));

    const actions = document.createElement("td");
    actions.className = "actions-cell";
    actions.append(makeButton("View", "button button-small", () => loadPetDetails(pet.id)));
    if (currentUser?.roles?.includes("MANAGER")) {
      actions.append(makeButton("Edit", "button button-small button-secondary", () => beginPetEdit(pet.id)));
      actions.append(makeButton("Delete", "button button-small button-danger", () => deletePet(pet.id, pet.name)));
    }
    row.append(actions);
    petRows.append(row);
  }
}

async function loadPets(page = 0) {
  setStatus(petListStatus, "Loading pet records…");
  try {
    const result = await request(`/api/v1/pets?page=${page}&size=10&sort=name,asc`, {
      headers: { Accept: "application/json" }
    });
    if (!result || !Array.isArray(result.content)) {
      throw new Error("Unexpected API response: expected a paginated object with a content array.");
    }
    renderPets(result.content);
    currentPage = result.number ?? page;
    totalPages = result.totalPages ?? 0;
    setStatus(petListStatus, result.content.length === 0
      ? "No pets have been registered yet."
      : `${result.totalElements} pet record(s) found.`);
    pageLabel.textContent = totalPages === 0 ? "Page 0 of 0" : `Page ${currentPage + 1} of ${totalPages}`;
    previousButton.disabled = currentPage <= 0;
    nextButton.disabled = totalPages === 0 || currentPage >= totalPages - 1;
  } catch (error) {
    setStatus(petListStatus, error.message, true);
    previousButton.disabled = true;
    nextButton.disabled = true;
    pageLabel.textContent = "Page unavailable";
  }
}

async function beginPetEdit(id) {
  setStatus(petFormStatus, "Loading selected pet…");
  try {
    const pet = await request(`/api/pets/${encodeURIComponent(id)}`, { headers: { Accept: "application/json" } });
    petForm.elements.microchipNumber.value = pet.microchipNumber;
    petForm.elements.name.value = pet.name;
    petForm.elements.species.value = pet.species;
    petForm.elements.breed.value = pet.breed;
    petForm.elements.age.value = pet.age;
    ownerSelect.value = String(pet.ownerId);
    editingPetId = id;
    petSubmit.textContent = "Save pet changes";
    petCancel.hidden = false;
    setStatus(petFormStatus, `Editing pet #${id}.`);
    petForm.scrollIntoView({ behavior: "smooth", block: "center" });
  } catch (error) {
    setStatus(petFormStatus, error.message, true);
  }
}

async function deletePet(id, petName) {
  if (!window.confirm(`Delete pet "${petName}"? This cannot be undone.`)) return;
  try {
    await request(`/api/pets/${encodeURIComponent(id)}`, { method: "DELETE" });
    setStatus(petListStatus, `Pet "${petName}" was deleted.`);
    await reloadAfterPetChange();
  } catch (error) {
    setStatus(petListStatus, error.message, true);
  }
}

async function reloadAfterPetChange() {
  await Promise.all([loadPets(currentPage), loadPetChoices()]);
  if (currentPage > 0 && petRows.children.length === 0) await loadPets(currentPage - 1);
}

petForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (petSaving || !petForm.reportValidity()) return;
  const payload = {
    microchipNumber: petForm.elements.microchipNumber.value.trim(),
    name: petForm.elements.name.value.trim(),
    species: petForm.elements.species.value.trim(),
    breed: petForm.elements.breed.value.trim(),
    age: Number(petForm.elements.age.value),
    ownerId: Number(ownerSelect.value)
  };
  if (!Number.isInteger(payload.age) || payload.age < 0 || !Number.isInteger(payload.ownerId)) {
    setStatus(petFormStatus, "Enter a non-negative whole-number age and select an owner.", true);
    return;
  }

  petSaving = true;
  petSubmit.disabled = true;
  petCancel.disabled = true;
  setStatus(petFormStatus, editingPetId === null ? "Saving pet…" : "Updating pet…");
  try {
    if (editingPetId === null) {
      await request("/api/pets", jsonOptions("POST", payload));
      setStatus(petFormStatus, "Pet registered successfully.");
    } else {
      await request(`/api/pets/${encodeURIComponent(editingPetId)}`, jsonOptions("PUT", payload));
      setStatus(petFormStatus, "Pet updated successfully.");
    }
    resetPetForm();
    await reloadAfterPetChange();
  } catch (error) {
    showFormError(petForm, petFormStatus, error);
  } finally {
    petSaving = false;
    petSubmit.disabled = false;
    petCancel.disabled = false;
  }
});

petCancel.addEventListener("click", resetPetForm);
petForm.addEventListener("input", () => clearFieldErrors(petForm));
petForm.addEventListener("change", () => clearFieldErrors(petForm));

async function loadAvailability(date, selectedVetId = null) {
  appointmentVet.replaceChildren(new Option("Loading veterinarians…", ""));
  if (!date) {
    appointmentVet.replaceChildren(new Option("Choose a date first", ""));
    setStatus(availabilityHint, "Choose a date to load veterinarian availability.");
    return;
  }
  try {
    const rows = await request(`/api/vet-availability?date=${encodeURIComponent(date)}`, {
      headers: { Accept: "application/json" }
    });
    appointmentVet.replaceChildren(new Option("Select an available veterinarian", ""));
    let enabledCount = 0;
    for (const row of rows) {
      const option = new Option(
        `${row.veterinarianName} · ${row.specialization} · ${row.availableSlots}/${row.totalSlots} slots`,
        String(row.vetId)
      );
      if (row.availableSlots <= 0 && String(row.vetId) !== String(selectedVetId ?? "")) {
        option.disabled = true;
      } else {
        enabledCount += 1;
      }
      appointmentVet.add(option);
    }
    if (selectedVetId !== null) appointmentVet.value = String(selectedVetId);
    setStatus(availabilityHint, rows.length === 0
      ? "No veterinarian schedule is configured for this date."
      : `${enabledCount} veterinarian choice(s) currently have capacity or match the appointment being edited.`);
  } catch (error) {
    appointmentVet.replaceChildren(new Option("Availability could not be loaded", ""));
    setStatus(availabilityHint, error.message, true);
  }
}

appointmentDate.addEventListener("change", () => loadAvailability(appointmentDate.value));

function renderAppointments(rows) {
  appointmentRows.replaceChildren();
  for (const appointment of rows) {
    const row = document.createElement("tr");
    row.append(makeCell(appointment.appointmentDate));
    row.append(makeCell(appointment.petName));
    row.append(makeCell(appointment.veterinarianName));
    row.append(makeCell(appointment.reasonForVisit));
    const actions = document.createElement("td");
    actions.className = "actions-cell";
    if (currentUser?.roles?.includes("MANAGER")) {
      const editButton = makeButton("Edit", "button button-small button-secondary", () => beginAppointmentEdit(appointment.id));
      if (appointment.appointmentDate < todayIso()) {
        editButton.disabled = true;
        editButton.title = "Past appointments are read-only history.";
      }
      actions.append(editButton);
      actions.append(makeButton("Cancel booking", "button button-small button-danger", () => cancelAppointment(appointment.id)));
    } else {
      actions.append(document.createTextNode("Read only"));
    }
    row.append(actions);
    appointmentRows.append(row);
  }
}

async function loadAppointments() {
  setStatus(appointmentListStatus, "Loading appointments…");
  try {
    currentAppointments = await request("/api/appointments", { headers: { Accept: "application/json" } });
    renderAppointments(currentAppointments);
    setStatus(appointmentListStatus, currentAppointments.length === 0
      ? "No appointments have been booked yet."
      : `${currentAppointments.length} appointment(s) found.`);
  } catch (error) {
    setStatus(appointmentListStatus, error.message, true);
  }
}

async function beginAppointmentEdit(id) {
  setStatus(appointmentFormStatus, "Loading appointment…");
  try {
    const appointment = await request(`/api/appointments/${encodeURIComponent(id)}`, {
      headers: { Accept: "application/json" }
    });
    editingAppointmentId = id;
    appointmentDate.value = appointment.appointmentDate;
    appointmentPet.value = String(appointment.petId);
    appointmentForm.elements.reasonForVisit.value = appointment.reasonForVisit;
    await loadAvailability(appointment.appointmentDate, appointment.vetId);
    appointmentVet.value = String(appointment.vetId);
    appointmentSubmit.textContent = "Save appointment changes";
    appointmentCancel.hidden = false;
    setStatus(appointmentFormStatus, `Editing appointment #${id}.`);
    appointmentForm.scrollIntoView({ behavior: "smooth", block: "center" });
  } catch (error) {
    setStatus(appointmentFormStatus, error.message, true);
  }
}

async function cancelAppointment(id) {
  const appointment = currentAppointments.find((item) => item.id === id);
  if (!window.confirm(`Cancel the appointment for ${appointment?.petName ?? "this pet"}? The reserved slot will be released.`)) return;
  try {
    await request(`/api/appointments/${encodeURIComponent(id)}`, { method: "DELETE" });
    setStatus(appointmentListStatus, "Appointment canceled and veterinarian capacity restored.");
    await loadAppointments();
    if (appointmentDate.value) await loadAvailability(appointmentDate.value);
  } catch (error) {
    setStatus(appointmentListStatus, error.message, true);
  }
}

appointmentForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (appointmentSaving || !appointmentForm.reportValidity()) return;
  const payload = {
    appointmentDate: appointmentDate.value,
    reasonForVisit: appointmentForm.elements.reasonForVisit.value.trim(),
    petId: Number(appointmentPet.value),
    vetId: Number(appointmentVet.value)
  };
  if (!payload.appointmentDate || !payload.reasonForVisit
      || !Number.isInteger(payload.petId) || !Number.isInteger(payload.vetId)) {
    setStatus(appointmentFormStatus, "Choose a date, pet, available veterinarian, and enter a reason.", true);
    return;
  }

  appointmentSaving = true;
  appointmentSubmit.disabled = true;
  appointmentCancel.disabled = true;
  setStatus(appointmentFormStatus, editingAppointmentId === null ? "Booking appointment…" : "Updating appointment…");
  try {
    if (editingAppointmentId === null) {
      await request("/api/appointments", jsonOptions("POST", payload));
      setStatus(appointmentFormStatus, "Appointment booked. The slot was reserved.");
    } else {
      await request(`/api/appointments/${encodeURIComponent(editingAppointmentId)}`, jsonOptions("PUT", payload));
      setStatus(appointmentFormStatus, "Appointment updated. Schedule capacity was adjusted.");
    }
    resetAppointmentForm();
    appointmentDate.value = payload.appointmentDate;
    await loadAppointments();
    if (payload.appointmentDate) await loadAvailability(payload.appointmentDate);
  } catch (error) {
    showFormError(appointmentForm, appointmentFormStatus, error);
  } finally {
    appointmentSaving = false;
    appointmentSubmit.disabled = false;
    appointmentCancel.disabled = false;
  }
});

appointmentCancel.addEventListener("click", resetAppointmentForm);
appointmentForm.addEventListener("input", () => clearFieldErrors(appointmentForm));
appointmentForm.addEventListener("change", () => clearFieldErrors(appointmentForm));

function addDetailLine(container, label, value) {
  const line = document.createElement("p");
  const strong = document.createElement("strong");
  strong.textContent = `${label}: `;
  line.append(strong, document.createTextNode(value ?? "—"));
  container.append(line);
}

function renderDetail(pet) {
  detailContainer.replaceChildren();
  detailContainer.hidden = false;
  const heading = document.createElement("h3");
  heading.textContent = pet.name;
  detailContainer.append(heading);
  addDetailLine(detailContainer, "Microchip", pet.microchipNumber);
  addDetailLine(detailContainer, "Species", pet.species);
  addDetailLine(detailContainer, "Breed", pet.breed);
  addDetailLine(detailContainer, "Age", pet.age);
  addDetailLine(detailContainer, "Owner", pet.ownerName);
  addDetailLine(detailContainer, "Owner phone", pet.ownerPhoneNumber);

  const appointmentHeading = document.createElement("h4");
  appointmentHeading.textContent = "Appointments";
  detailContainer.append(appointmentHeading);
  const appointmentList = document.createElement("ul");
  for (const appointment of pet.appointments ?? []) {
    const item = document.createElement("li");
    item.textContent = `${appointment.appointmentDate} · ${appointment.reasonForVisit} · Dr. ${appointment.veterinarianName}`;
    appointmentList.append(item);
  }
  if (appointmentList.children.length === 0) {
    const item = document.createElement("li");
    item.textContent = "No appointments recorded.";
    appointmentList.append(item);
  }
  detailContainer.append(appointmentList);

  const vaccineHeading = document.createElement("h4");
  vaccineHeading.textContent = "Vaccination history";
  detailContainer.append(vaccineHeading);
  const vaccineList = document.createElement("ul");
  for (const vaccination of pet.vaccinations ?? []) {
    const item = document.createElement("li");
    item.textContent = `${vaccination.vaccineName} · ${vaccination.vaccinationDate}`;
    vaccineList.append(item);
  }
  if (vaccineList.children.length === 0) {
    const item = document.createElement("li");
    item.textContent = "No vaccination records.";
    vaccineList.append(item);
  }
  detailContainer.append(vaccineList);
}

async function loadPetDetails(id) {
  setStatus(detailStatus, "Loading selected pet…");
  detailContainer.hidden = true;
  detailContainer.replaceChildren();
  try {
    const pet = await request(`/api/v1/pets/${encodeURIComponent(id)}`, { headers: { Accept: "application/json" } });
    renderDetail(pet);
    setStatus(detailStatus, `Showing record #${pet.id}.`);
  } catch (error) {
    setStatus(detailStatus, error.message, true);
  }
}

document.querySelector("#refresh-pets").addEventListener("click", () => reloadAfterPetChange());
document.querySelector("#refresh-appointments").addEventListener("click", loadAppointments);
previousButton.addEventListener("click", () => loadPets(Math.max(0, currentPage - 1)));
nextButton.addEventListener("click", () => loadPets(Math.min(totalPages - 1, currentPage + 1)));

function todayIso() {
  const now = new Date();
  const local = new Date(now.getTime() - now.getTimezoneOffset() * 60000);
  return local.toISOString().slice(0, 10);
}

async function initialize() {
  appointmentDate.min = todayIso();
  appointmentDate.value = todayIso();
  try {
    await Promise.all([loadOwners(), loadPetChoices()]);
  } catch (error) {
    setStatus(petFormStatus, `Relationship choices could not be loaded: ${error.message}`, true);
  }
  await Promise.all([loadPets(0), loadAppointments(), loadAvailability(appointmentDate.value)]);
}

function applyAuthenticatedUser(user) {
  currentUser = user;
  const signedIn = Boolean(user);
  loginView.hidden = signedIn;
  sessionView.hidden = !signedIn;
  clinicContent.hidden = !signedIn;

  if (!signedIn) {
    currentUserLabel.textContent = "";
    roleNote.textContent = "";
    clinicInitialized = false;
    return;
  }

  const roles = Array.isArray(user.roles) ? user.roles : [];
  const manager = roles.includes("MANAGER");
  currentUserLabel.textContent = `${user.username} · ${roles.join(", ")}`;
  roleNote.textContent = manager
    ? "Manager access: view and manage pet and appointment records."
    : "Reader access: view-only. Create, update, and delete actions are restricted.";
  petForm.hidden = !manager;
  appointmentForm.hidden = !manager;

  if (!clinicInitialized) {
    clinicInitialized = true;
    initialize();
  }
}

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!loginForm.reportValidity()) return;
  loginButton.disabled = true;
  setStatus(authStatus, "Signing in…");
  try {
    if (!csrfState) await refreshCsrfToken();
    const formBody = new URLSearchParams({
      username: loginForm.elements.username.value.trim(),
      password: loginForm.elements.password.value
    });
    const response = await fetch(`${API_BASE}/api/auth/login`, {
      method: "POST",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
        Accept: "application/json",
        [csrfState.headerName]: csrfState.token
      },
      body: formBody.toString()
    });
    const body = response.headers.get("content-type")?.includes("application/json")
      ? await response.json() : null;
    if (!response.ok) throw new Error(body?.message || `Sign-in failed (${response.status}).`);

    // Spring Security clears the old CSRF token after authentication; get a fresh one.
    await refreshCsrfToken();
    const user = await request("/api/auth/me", { headers: { Accept: "application/json" } });
    loginForm.reset();
    setStatus(authStatus, "Signed in successfully.");
    applyAuthenticatedUser(user);
  } catch (error) {
    setStatus(authStatus, error.message, true);
  } finally {
    loginButton.disabled = false;
  }
});

logoutButton.addEventListener("click", async () => {
  logoutButton.disabled = true;
  try {
    await request("/api/auth/logout", { method: "POST", headers: { Accept: "application/json" } });
    applyAuthenticatedUser(null);
    await refreshCsrfToken();
    setStatus(authStatus, "You have signed out. Sign in to continue.");
  } catch (error) {
    setStatus(authStatus, error.message, true);
  } finally {
    logoutButton.disabled = false;
  }
});

async function bootstrapAuthentication() {
  try {
    await refreshCsrfToken();
    const user = await request("/api/auth/me", { headers: { Accept: "application/json" } });
    setStatus(authStatus, "Existing session restored.");
    applyAuthenticatedUser(user);
  } catch (error) {
    if (error.status === 401) {
      applyAuthenticatedUser(null);
      setStatus(authStatus, "Please sign in with your clinic application account.");
    } else {
      setStatus(authStatus, `Authentication setup failed: ${error.message}`, true);
    }
  }
}

bootstrapAuthentication();
