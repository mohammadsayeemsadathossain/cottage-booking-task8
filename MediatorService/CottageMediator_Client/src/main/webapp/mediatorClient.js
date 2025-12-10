/**
 * Cottage Booking Mediator Client JavaScript
 * Task 8: FINAL VERSION - Based on actual backend response
 * Backend sends: { requiresMapping, cottages, inputMapping, candidates }
 */

function searchCottages() {
    const serviceURL = document.getElementById('serviceURL').value.trim();
    const bookerName = document.getElementById('bookerName').value.trim();
    const numPeople = document.getElementById('numPeople').value;
    const numBedrooms = document.getElementById('numBedrooms').value;
    const maxDistLake = document.getElementById('maxDistLake').value;
    const city = document.getElementById('city').value.trim();
    const maxDistCity = document.getElementById('maxDistCity').value;
    const numDays = document.getElementById('numDays').value;
    const startDate = document.getElementById('startDate').value;
    const dateShift = document.getElementById('dateShift').value;

    if (!serviceURL) {
        alert('⚠️ Please enter the SSWAP Service URL!');
        return;
    }
    
    if (!bookerName || !numPeople || !numBedrooms || !maxDistLake || 
        !maxDistCity || !numDays || !startDate || dateShift === '') {
        alert('⚠️ Please fill in all required fields before searching!');
        return;
    }

    document.getElementById('results').style.display = 'block';
    document.getElementById('cottageList').innerHTML = `
        <div class="loading-message">
            <div class="loading-spinner"></div>
            <p>Searching for available cottages via SSWAP service...</p>
        </div>
    `;

    const searchBtn = document.querySelector('.submit-btn');
    searchBtn.disabled = true;
    searchBtn.querySelector('.btn-text').style.display = 'none';
    searchBtn.querySelector('.btn-loader').style.display = 'inline-block';

    const formData = new URLSearchParams();
    formData.append('reqType', 'searchCottage');
    formData.append('serviceURL', serviceURL);
    formData.append('bookerName', bookerName);
    formData.append('numberOfPeople', numPeople);
    formData.append('numberOfBedrooms', numBedrooms);
    formData.append('maxDistanceFromLake', maxDistLake);
    formData.append('cityName', city);
    formData.append('maxCityDistance', maxDistCity);
    formData.append('numberOfDays', numDays);
    formData.append('startDate', startDate);
    formData.append('possibleShift', dateShift);

    fetch('MediatorServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        console.log('Backend response:', data);
        if (data.requiresMapping === true) {
            showMappingInterface(data);
        } else {
            displayResults(data, bookerName);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        document.getElementById('cottageList').innerHTML = `
            <div class="error-message">
                <strong>❌ Error:</strong> ${error.message}
            </div>
        `;
    })
    .finally(() => {
        searchBtn.disabled = false;
        searchBtn.querySelector('.btn-text').style.display = 'inline-block';
        searchBtn.querySelector('.btn-loader').style.display = 'none';
    });
}

function formatDateForDisplay(dateString) {
    if (!dateString) return 'N/A';
    let date;
    if (dateString.includes('-')) {
        date = new Date(dateString);
    } else if (dateString.includes('.')) {
        const [day, month, year] = dateString.split('.');
        date = new Date(year, month - 1, day);
    } else {
        return dateString;
    }
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

function calculateDuration(startDate, endDate) {
    if (!startDate || !endDate) return 'N/A';
    let start, end;
    if (startDate.includes('-')) {
        start = new Date(startDate);
    } else if (startDate.includes('.')) {
        const [day, month, year] = startDate.split('.');
        start = new Date(year, month - 1, day);
    }
    if (endDate.includes('-')) {
        end = new Date(endDate);
    } else if (endDate.includes('.')) {
        const [day, month, year] = endDate.split('.');
        end = new Date(year, month - 1, day);
    }
    const diffTime = Math.abs(end - start);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays;
}

function displayResults(data, bookerName) {
    const cottageList = document.getElementById('cottageList');
    if (data.error) {
        cottageList.innerHTML = `<div class="error-message"><strong>❌ Error:</strong> ${data.error}</div>`;
        return;
    }
    const cottages = data.cottages || [];
    if (cottages.length === 0) {
        cottageList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #7f8c8d;">
                <h3>No cottages found</h3>
                <p>No cottages match your search criteria.</p>
            </div>
        `;
        return;
    }

    let html = '';
    cottages.forEach((cottage, index) => {
        const bookingNumber = cottage.bookingNumber || 'BK-' + Date.now() + '-' + (index + 1);
        const startDateDisplay = formatDateForDisplay(cottage.bookingStartDate || cottage.startDate);
        const endDateDisplay = formatDateForDisplay(cottage.bookingEndDate || cottage.endDate);
        const duration = calculateDuration(cottage.bookingStartDate || cottage.startDate, cottage.bookingEndDate || cottage.endDate);
        
        html += `
            <div class="cottage-card">
                <img src="${cottage.imageURL || 'https://via.placeholder.com/400x220/3498db/ffffff?text=Cottage+Image'}" 
                     alt="${cottage.cottageName || 'Cottage'}" class="cottage-image">
                <div class="cottage-info">
                    <h3>${cottage.cottageName || cottage.cottageAddress || 'Cottage ' + cottage.cottageID}</h3>
                    <div class="booking-number">Booking #${bookingNumber}</div>
                    <div class="info-item"><span class="info-label">Booker:</span><span class="info-value">${cottage.bookerName || bookerName}</span></div>
                    ${cottage.cottageAddress ? `<div class="info-item"><span class="info-label">Address:</span><span class="info-value">${cottage.cottageAddress}</span></div>` : ''}
                    <div class="info-item"><span class="info-label">Capacity:</span><span class="info-value">${cottage.capacity || 'N/A'} people</span></div>
                    <div class="info-item"><span class="info-label">Bedrooms:</span><span class="info-value">${cottage.numberOfBedrooms || 'N/A'}</span></div>
                    <div class="info-item"><span class="info-label">Distance to Lake:</span><span class="info-value">${cottage.distanceFromLake || 'N/A'} meters</span></div>
                    <div class="info-item"><span class="info-label">City:</span><span class="info-value">${cottage.cityName || 'N/A'}</span></div>
                    <div class="info-item"><span class="info-label">Check-in:</span><span class="info-value">${startDateDisplay}</span></div>
                    <div class="info-item"><span class="info-label">Check-out:</span><span class="info-value">${endDateDisplay}</span></div>
                    <div class="info-item"><span class="info-label">Duration:</span><span class="info-value">${duration} nights</span></div>
                </div>
            </div>
        `;
    });
    cottageList.innerHTML = html;
}

window.addEventListener('DOMContentLoaded', () => {
    const startDateInput = document.getElementById('startDate');
    const today = new Date().toISOString().split('T')[0];
    startDateInput.setAttribute('min', today);
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 7);
    startDateInput.value = defaultDate.toISOString().split('T')[0];
});

/**
 * TASK 8: ONTOLOGY ALIGNMENT
 */

let originalSearchContext = null;
let allCandidates = [];

function storeMappingContext() {
    originalSearchContext = {
        serviceURL: document.getElementById('serviceURL').value.trim(),
        bookerName: document.getElementById('bookerName').value.trim(),
        numPeople: document.getElementById('numPeople').value,
        numBedrooms: document.getElementById('numBedrooms').value,
        maxDistLake: document.getElementById('maxDistLake').value,
        city: document.getElementById('city').value.trim(),
        maxDistCity: document.getElementById('maxDistCity').value,
        numDays: document.getElementById('numDays').value,
        startDate: document.getElementById('startDate').value,
        dateShift: document.getElementById('dateShift').value
    };
}

function showMappingInterface(data) {
    console.log('Showing mapping interface with data:', data);
    storeMappingContext();
    document.getElementById('results').style.display = 'none';
    const mappingSection = document.getElementById('mappingSection');
    mappingSection.style.display = 'block';

    const inputMapping = data.inputMapping || {};
    allCandidates = data.candidates || [];
    
    console.log('InputMapping:', inputMapping);
    console.log('Candidates:', allCandidates);

    const container = document.getElementById('allMappingFields');
    container.innerHTML = '';
    
    // ORDER: First 9 inputs (Task 7 order), then remaining outputs
    const inputFieldsOrder = [
        'bookerName', 'numberOfPeople', 'numberOfBedrooms', 
        'maxDistanceFromLake', 'cityName', 'maxCityDistance', 
        'numberOfDays', 'startDate', 'possibleShift'
    ];
    
    const allKeys = Object.keys(inputMapping);
    const outputFields = allKeys.filter(key => !inputFieldsOrder.includes(key));
    const orderedFields = [...inputFieldsOrder, ...outputFields];
    
    orderedFields.forEach(fieldName => {
        if (inputMapping[fieldName]) {
            generateMappingRow(container, fieldName, inputMapping[fieldName]);
        }
    });
    
    mappingSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function generateMappingRow(container, fieldName, candidate) {
    const descriptions = {
        'bookerName': 'Name of the person booking',
        'numberOfPeople': 'Number of people',
        'numberOfBedrooms': 'Number of bedrooms required',
        'maxDistanceFromLake': 'Maximum distance from lake (meters)',
        'cityName': 'Name of nearest city',
        'maxCityDistance': 'Maximum distance to city (meters)',
        'numberOfDays': 'Number of days for booking',
        'startDate': 'Start date of booking',
        'possibleShift': 'Possible date shift (+/- days)',
        'bookingNumber': 'Unique booking number',
        'cottageAddress': 'Address of the cottage',
        'imageURL': 'Image URL of the cottage',
        'capacity': 'Actual capacity (number of people)',
        'distanceFromLake': 'Actual distance to lake (meters)',
        'cityDistance': 'Distance to city (meters)',
        'bookingStartDate': 'Booking start date',
        'bookingEndDate': 'Booking end date'
    };

    const myName = candidate.myName;
    const remoteName = candidate.remoteName;
    const remoteUri = candidate.remoteUri;
    const score = candidate.score;
    const fieldDescription = descriptions[fieldName] || fieldName;

    const mappingRow = document.createElement('div');
    mappingRow.className = 'mapping-row';

    // Our field (left)
    const ourFieldDiv = document.createElement('div');
    ourFieldDiv.className = 'our-field';
    ourFieldDiv.innerHTML = `<strong>${myName}</strong><br><span class="field-description">${fieldDescription}</span>`;

    // Arrow
    const arrowDiv = document.createElement('div');
    arrowDiv.className = 'mapping-arrow';
    arrowDiv.textContent = '→';

    // Their field (right) - DROPDOWN
    const theirFieldDiv = document.createElement('div');
    theirFieldDiv.className = 'their-field';

    const dropdown = document.createElement('select');
    dropdown.className = 'field-dropdown';
    dropdown.dataset.myName = myName;
    dropdown.dataset.originalRemoteName = remoteName;
    dropdown.dataset.originalRemoteUri = remoteUri;
    dropdown.dataset.originalScore = score;

    // Default option
    const defaultOption = document.createElement('option');
    defaultOption.value = '';
    defaultOption.textContent = '-- Select Your Field --';
    dropdown.appendChild(defaultOption);

    // Add all candidates
    allCandidates.forEach(candidateName => {
        const option = document.createElement('option');
        option.value = candidateName;
        option.textContent = candidateName;
        if (candidateName === remoteName) {
            option.selected = true;
        }
        dropdown.appendChild(option);
    });

    dropdown.addEventListener('change', function() {
        updateMatchInfo(this);
    });

    theirFieldDiv.appendChild(dropdown);

    // Match info
    const matchInfo = document.createElement('div');
    matchInfo.className = 'match-info';
    matchInfo.dataset.myName = myName;

    if (remoteName && score > 0) {
        matchInfo.innerHTML = `
            <div class="match-score">${getSimilarityBadgeHTML(score)}</div>
            <div class="remote-uri">${remoteUri}</div>
        `;
    } else {
        matchInfo.innerHTML = `<div class="match-score"><span class="no-match">Please select a field</span></div>`;
    }

    theirFieldDiv.appendChild(matchInfo);

    mappingRow.appendChild(ourFieldDiv);
    mappingRow.appendChild(arrowDiv);
    mappingRow.appendChild(theirFieldDiv);
    container.appendChild(mappingRow);
}

function updateMatchInfo(dropdown) {
    const selectedField = dropdown.value;
    const myName = dropdown.dataset.myName;
    const matchInfo = dropdown.parentElement.querySelector('.match-info[data-my-name="' + myName + '"]');

    if (!selectedField) {
        matchInfo.innerHTML = `<div class="match-score"><span class="no-match">Please select a field</span></div>`;
        return;
    }

    const originalRemoteName = dropdown.dataset.originalRemoteName;
    const isOriginalMatch = (selectedField === originalRemoteName);

    let scoreHTML = '';
    if (isOriginalMatch) {
        const originalScore = parseFloat(dropdown.dataset.originalScore);
        scoreHTML = getSimilarityBadgeHTML(originalScore);
    } else {
        scoreHTML = '<span class="user-override">👤 User Selected</span>';
    }

    // We don't have URIs for all candidates, so use placeholder
    const uri = isOriginalMatch ? dropdown.dataset.originalRemoteUri : 'User-selected field';

    matchInfo.innerHTML = `
        <div class="match-score">${scoreHTML}</div>
        <div class="remote-uri">${uri}</div>
    `;
}

function getSimilarityBadgeHTML(score) {
    const percentage = Math.round(score * 100);
    let badgeClass = 'low';
    let emoji = '🔴';
    
    if (score >= 0.80) {
        badgeClass = 'high';
        emoji = '🟢';
    } else if (score >= 0.70) {
        badgeClass = 'medium';
        emoji = '🟡';
    }
    
    return `<span class="similarity-badge ${badgeClass}">${emoji} ${percentage}%</span>`;
}

function confirmMapping() {
    console.log('Confirming mapping...');
    
    const mappings = {};
    const dropdowns = document.querySelectorAll('.field-dropdown');
    
    let hasUnmapped = false;
    
    dropdowns.forEach(dropdown => {
        const myName = dropdown.dataset.myName;
        const selectedRemoteName = dropdown.value;
        
        if (!selectedRemoteName) {
            hasUnmapped = true;
            return;
        }

        const originalRemoteName = dropdown.dataset.originalRemoteName;
        const originalRemoteUri = dropdown.dataset.originalRemoteUri;
        let finalScore = 1.0;
        let finalUri = originalRemoteUri;

        if (selectedRemoteName === originalRemoteName) {
            finalScore = parseFloat(dropdown.dataset.originalScore);
        } else {
            // User selected different field - we don't have its URI from candidates array
            // Backend will need to resolve it
            finalUri = '';
        }

        mappings[myName] = {
            myName: myName,
            remoteName: selectedRemoteName,
            remoteUri: finalUri,
            score: finalScore
        };
    });
    
    if (hasUnmapped) {
        alert('⚠️ Please map all fields before confirming!');
        return;
    }
    
    console.log('Collected mappings:', mappings);
    
    document.getElementById('mappingSection').style.display = 'none';
    document.getElementById('results').style.display = 'block';
    document.getElementById('cottageList').innerHTML = `
        <div class="loading-message">
            <div class="loading-spinner"></div>
            <p>Applying mappings and searching for cottages...</p>
        </div>
    `;
    
    const confirmBtn = document.querySelector('.mapping-confirm-btn');
    confirmBtn.disabled = true;
    confirmBtn.querySelector('.btn-text').style.display = 'none';
    confirmBtn.querySelector('.btn-loader').style.display = 'inline-block';
    
    const formData = new URLSearchParams();
    formData.append('reqType', 'searchCottageWithMapping');
    formData.append('serviceURL', originalSearchContext.serviceURL);
    formData.append('bookerName', originalSearchContext.bookerName);
    formData.append('numberOfPeople', originalSearchContext.numPeople);
    formData.append('numberOfBedrooms', originalSearchContext.numBedrooms);
    formData.append('maxDistanceFromLake', originalSearchContext.maxDistLake);
    formData.append('cityName', originalSearchContext.city);
    formData.append('maxCityDistance', originalSearchContext.maxDistCity);
    formData.append('numberOfDays', originalSearchContext.numDays);
    formData.append('startDate', originalSearchContext.startDate);
    formData.append('possibleShift', originalSearchContext.dateShift);
    formData.append('inputMapping', JSON.stringify(mappings));
    
    console.log('Sending to backend:', Object.fromEntries(formData));
    
    fetch('MediatorServlet', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        console.log('Backend response after mapping:', data);
        displayResults(data, originalSearchContext.bookerName);
    })
    .catch(error => {
        console.error('Error after mapping:', error);
        document.getElementById('cottageList').innerHTML = `
            <div class="error-message"><strong>❌ Error:</strong> ${error.message}</div>
        `;
    })
    .finally(() => {
        confirmBtn.disabled = false;
        confirmBtn.querySelector('.btn-text').style.display = 'inline-block';
        confirmBtn.querySelector('.btn-loader').style.display = 'none';
    });
}

function cancelMapping() {
    document.getElementById('mappingSection').style.display = 'none';
    originalSearchContext = null;
    allCandidates = [];
    alert('Mapping cancelled.');
}