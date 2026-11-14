const petRows = document.querySelector("#pet-rows");
const listStatus = document.querySelector("#list-status");
const detailStatus = document.querySelector("#detail-status");
const detailContainer = document.querySelector("#pet-detail");
const previousButton = document.querySelector("#previous-button");
const nextButton = document.querySelector("#next-button");
const pageLabel = document.querySelector("#page-label");

let currentPage = 0;
let totalPages = 0;

async function getJson(path) {
  const response = await fetch(path, {
    headers: { Accept: "application/json" }
  });

  if (!response.ok) {
    if (response.status === 404) throw new Error("The requested record was not found (404).");
    throw new Error(`The server returned HTTP ${response.status}.`);
  }
  return response.json();
}

function makeCell(value) {
  const cell = document.createElement("td");
  cell.textContent = value ?? "—";
  return cell;
}

function renderPets(rows) {
  petRows.replaceChildren();
  for (const pet of rows) {
    const row = document.createElement("tr");
    row.append(makeCell(pet.name));
    row.append(makeCell(pet.species));
    row.append(makeCell(pet.breed));
    row.append(makeCell(pet.ownerName));

    const actionCell = document.createElement("td");
    const button = document.createElement("button");
    button.type = "button";
    button.className = "button button-small";
    button.textContent = "View details";
    button.addEventListener("click", () => loadPetDetails(pet.id));
    actionCell.append(button);
    row.append(actionCell);
    petRows.append(row);
  }
}

async function loadPets(page = 0) {
  listStatus.textContent = "Loading pet records…";
  petRows.replaceChildren();
  try {
    const result = await getJson(`/api/v1/pets?page=${page}&size=10&sort=name,asc`);
    if (!result || !Array.isArray(result.content)) {
      throw new Error("Unexpected API response: expected a paginated object with a content array.");
    }
    renderPets(result.content);
    currentPage = result.number ?? page;
    totalPages = result.totalPages ?? 0;
    listStatus.textContent = result.content.length === 0 ? "No pets have been registered yet." : `${result.totalElements} pet record(s) found.`;
    pageLabel.textContent = totalPages === 0 ? "Page 0 of 0" : `Page ${currentPage + 1} of ${totalPages}`;
    previousButton.disabled = currentPage <= 0;
    nextButton.disabled = totalPages === 0 || currentPage >= totalPages - 1;
  } catch (error) {
    listStatus.textContent = error.message;
    previousButton.disabled = true;
    nextButton.disabled = true;
    pageLabel.textContent = "Page unavailable";
  }
}

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
  detailStatus.textContent = "Loading selected pet…";
  detailContainer.hidden = true;
  detailContainer.replaceChildren();
  try {
    const pet = await getJson(`/api/v1/pets/${encodeURIComponent(id)}`);
    renderDetail(pet);
    detailStatus.textContent = `Showing record #${pet.id}.`;
  } catch (error) {
    detailStatus.textContent = error.message;
  }
}

 document.querySelector("#refresh-button").addEventListener("click", () => loadPets(currentPage));
previousButton.addEventListener("click", () => loadPets(Math.max(0, currentPage - 1)));
nextButton.addEventListener("click", () => loadPets(Math.min(totalPages - 1, currentPage + 1)));

loadPets(0);
