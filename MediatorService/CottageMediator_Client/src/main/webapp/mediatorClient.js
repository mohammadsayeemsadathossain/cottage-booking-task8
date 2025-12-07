/**
 * Cottage Booking Mediator Client JavaScript
 * Task 7-2: Mediator Service Client
 * Task 8: UPDATED - Complete Input & Output Ontology Alignment
 * - Maps 9 INPUTS (hasMapping - Subject/RIG)
 * - Maps 11 OUTPUTS (mapsTo - Object/RRG)
 * - Auto-selects based on similarity scores from Apache Commons Text
 * - Visual similarity indicators
 */

/**
 * Main function called when user clicks "Search Available Cottages"
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
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        if (data.requiresMapping === true) {
            showMappingInterface(data);
        } else {
            displayResults(data, bookerName);
        }
    })
    .catch(error => {
        console.error('Error communicating with mediator servlet:', error);
        document.getElementById('cottageList').innerHTML = `
            <div class="error-message">
                <strong>❌ Error:</strong> Unable to connect to the mediator service.
                <br><br>
                <strong>Please check:</strong>
                <ul style="margin-top: 10px; margin-left: 20px;">
                    <li>The mediator servlet is running</li>
                    <li>The SSWAP service URL is correct</li>
                    <li>The backend service is accessible</li>
                </ul>
                <br>
                <strong>Error details:</strong> ${error.message}
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
        cottageList.innerHTML = `
            <div class="error-message">
                <strong>❌ Error:</strong> ${data.error}
            </div>
        `;
        return;
    }
    
    const cottages = data.cottages || [];
    
    if (cottages.length === 0) {
        cottageList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #7f8c8d;">
                <h3>No cottages found</h3>
                <p>No cottages match your search criteria. Try adjusting your filters:</p>
                <ul style="text-align: left; display: inline-block; margin-top: 15px;">
                    <li>Increase the maximum distance to lake or city</li>
                    <li>Reduce the number of required bedrooms</li>
                    <li>Increase date flexibility</li>
                    <li>Try different dates</li>
                </ul>
            </div>
        `;
        return;
    }

    let html = '';
    cottages.forEach((cottage, index) => {
        const bookingNumber = cottage.bookingNumber || 'BK-' + Date.now() + '-' + (index + 1);
        
        const startDateDisplay = formatDateForDisplay(cottage.bookingStartDate || cottage.startDate);
        const endDateDisplay = formatDateForDisplay(cottage.bookingEndDate || cottage.endDate);
        
        const duration = calculateDuration(
            cottage.bookingStartDate || cottage.startDate,
            cottage.bookingEndDate || cottage.endDate
        );
        
        html += `
            <div class="cottage-card">
                <img src="${cottage.imageURL || 'https://via.placeholder.com/400x220/3498db/ffffff?text=Cottage+Image'}" 
                     alt="${cottage.cottageName || 'Cottage'}" 
                     class="cottage-image" 
                     onerror="this.src='https://via.placeholder.com/400x220/3498db/ffffff?text=Cottage+Image'">
                <div class="cottage-info">
                    <h3>${cottage.cottageName || cottage.cottageAddress || 'Cottage ' + cottage.cottageID}</h3>
                    <div class="booking-number">Booking #${bookingNumber}</div>
                    
                    <div class="info-item">
                        <span class="info-label">Booker Name:</span>
                        <span class="info-value">${cottage.bookerName || bookerName}</span>
                    </div>
                    
                    ${cottage.cottageID ? `
                    <div class="info-item">
                        <span class="info-label">Cottage ID:</span>
                        <span class="info-value">${cottage.cottageID}</span>
                    </div>
                    ` : ''}
                    
                    ${cottage.cottageAddress ? `
                    <div class="info-item">
                        <span class="info-label">Address:</span>
                        <span class="info-value">${cottage.cottageAddress}</span>
                    </div>
                    ` : ''}
                    
                    <div class="info-item">
                        <span class="info-label">Capacity:</span>
                        <span class="info-value">${cottage.capacity || 'N/A'} people</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Bedrooms:</span>
                        <span class="info-value">${cottage.numberOfBedrooms || 'N/A'}</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Distance to Lake:</span>
                        <span class="info-value">${cottage.distanceFromLake || cottage.distanceToLake || 'N/A'} meters</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Nearest City:</span>
                        <span class="info-value">${cottage.cityName || 'N/A'}</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Distance to City:</span>
                        <span class="info-value">${cottage.cityDistance || cottage.distanceToCity || 'N/A'} meters</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Check-in Date:</span>
                        <span class="info-value">${startDateDisplay}</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Check-out Date:</span>
                        <span class="info-value">${endDateDisplay}</span>
                    </div>
                    
                    <div class="info-item">
                        <span class="info-label">Duration:</span>
                        <span class="info-value">${duration} nights</span>
                    </div>
                    
                    <button class="book-btn" onclick="bookCottage('${bookingNumber}', '${cottage.cottageName || cottage.cottageID}')">
                        📅 Book This Cottage
                    </button>
                </div>
            </div>
        `;
    });
    
    cottageList.innerHTML = html;
    document.getElementById('results').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function bookCottage(bookingNumber, cottageName) {
    alert(`📅 Booking Feature Coming Soon!\n\n` +
          `Cottage: ${cottageName}\n` +
          `Booking Number: ${bookingNumber}\n\n` +
          `The booking functionality will be available in a future update.\n` +
          `For now, please note this cottage's details and contact us directly to complete your booking.`);
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
 * ===========================================================================
 * TASK 8: UPDATED ONTOLOGY ALIGNMENT FUNCTIONS
 * - Maps both INPUTS (9 fields) and OUTPUTS (11 fields)
 * - Auto-selects based on similarity scores from backend (Apache Commons Text)
 * - Visual similarity indicators (Green/Yellow/Red badges)
 * ===========================================================================
 */

let originalSearchContext = null;

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

/**
 * UPDATED: Show mapping interface with BOTH input and output mappings
 * @param {Object} data - Response from backend with mapping information
 * Expected format:
 * {
 *   requiresMapping: true,
 *   inputMapping: { ourInputs: {...}, theirInputs: {field: score, ...} },
 *   outputMapping: { ourOutputs: {...}, theirOutputs: {field: score, ...} }
 * }
 */
function showMappingInterface(data) {
    storeMappingContext();
    
    document.getElementById('results').style.display = 'none';
    
    const mappingSection = document.getElementById('mappingSection');
    mappingSection.style.display = 'block';
    
    // Generate INPUT mappings (9 fields)
    const inputMappingContainer = document.getElementById('inputMappingFields');
    inputMappingContainer.innerHTML = '';
    
    if (data.inputMapping) {
        const ourInputs = data.inputMapping.ourInputs || {};
        const theirInputs = data.inputMapping.theirInputs || {};
        
        generateMappingRows(inputMappingContainer, ourInputs, theirInputs, 'input');
    }
    
    // Generate OUTPUT mappings (11 fields)
    const outputMappingContainer = document.getElementById('outputMappingFields');
    outputMappingContainer.innerHTML = '';
    
    if (data.outputMapping) {
        const ourOutputs = data.outputMapping.ourOutputs || {};
        const theirOutputs = data.outputMapping.theirOutputs || {};
        
        generateMappingRows(outputMappingContainer, ourOutputs, theirOutputs, 'output');
    }
    
    mappingSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/**
 * Generate mapping rows for either inputs or outputs
 * @param {HTMLElement} container - Container to add rows to
 * @param {Object} ourFields - Our field names and descriptions
 * @param {Object} theirFields - Their field names with similarity scores
 * @param {String} mappingType - 'input' or 'output'
 */
function generateMappingRows(container, ourFields, theirFields, mappingType) {
    Object.keys(ourFields).forEach(ourFieldName => {
        const fieldDescription = ourFields[ourFieldName];
        
        // Find best match based on similarity scores
        const bestMatch = findBestMatchWithScore(ourFieldName, theirFields);
        
        // Create mapping row
        const mappingRow = document.createElement('div');
        mappingRow.className = 'mapping-row';
        
        // Our field (left side)
        const ourFieldDiv = document.createElement('div');
        ourFieldDiv.className = 'our-field';
        ourFieldDiv.innerHTML = `
            <strong>${ourFieldName}</strong>
            <br>
            <span class="field-description">${fieldDescription}</span>
        `;
        
        // Arrow
        const arrowDiv = document.createElement('div');
        arrowDiv.className = 'mapping-arrow';
        arrowDiv.textContent = '→';
        
        // Their field dropdown (right side)
        const theirFieldDiv = document.createElement('div');
        theirFieldDiv.className = 'their-field';
        
        const selectElement = document.createElement('select');
        selectElement.className = 'mapping-select';
        selectElement.id = `mapping_${mappingType}_${ourFieldName}`;
        selectElement.name = ourFieldName;
        selectElement.dataset.mappingType = mappingType;
        
        // Add default option
        const defaultOption = document.createElement('option');
        defaultOption.value = '';
        
        if (bestMatch.score < 0.70) {
            defaultOption.textContent = '-- Select Field (Low Match) --';
        } else {
            defaultOption.textContent = '-- Select Field --';
        }
        selectElement.appendChild(defaultOption);
        
        // Add options for each of their fields with scores
        const sortedFields = Object.entries(theirFields).sort((a, b) => b[1] - a[1]);
        
        sortedFields.forEach(([theirFieldName, score]) => {
            const option = document.createElement('option');
            option.value = theirFieldName;
            
            const percentage = Math.round(score * 100);
            const badge = getSimilarityEmoji(score);
            option.textContent = `${theirFieldName} ${badge} ${percentage}%`;
            
            // Auto-select if this is the best match and score >= 0.70
            if (bestMatch.field === theirFieldName && bestMatch.score >= 0.70) {
                option.selected = true;
            }
            
            selectElement.appendChild(option);
        });
        
        // Add similarity badge display
        const similarityBadge = document.createElement('div');
        similarityBadge.className = 'similarity-indicator';
        if (bestMatch.score >= 0.70) {
            const percentage = Math.round(bestMatch.score * 100);
            const badgeClass = getSimilarityClass(bestMatch.score);
            similarityBadge.innerHTML = `
                <span class="similarity-badge ${badgeClass}">
                    ${getSimilarityEmoji(bestMatch.score)} ${percentage}% match
                </span>
            `;
        } else {
            similarityBadge.innerHTML = `
                <span class="similarity-badge low">
                    🔴 Low similarity - manual selection needed
                </span>
            `;
        }
        
        theirFieldDiv.appendChild(selectElement);
        theirFieldDiv.appendChild(similarityBadge);
        
        // Add all parts to the row
        mappingRow.appendChild(ourFieldDiv);
        mappingRow.appendChild(arrowDiv);
        mappingRow.appendChild(theirFieldDiv);
        
        container.appendChild(mappingRow);
    });
}

/**
 * Find the best match for our field in their fields based on similarity scores
 * @param {String} ourField - Our field name
 * @param {Object} theirFields - Their fields with similarity scores
 * @returns {Object} {field: bestFieldName, score: bestScore}
 */
function findBestMatchWithScore(ourField, theirFields) {
    let bestField = null;
    let bestScore = 0;
    
    for (const [theirField, score] of Object.entries(theirFields)) {
        if (score > bestScore) {
            bestScore = score;
            bestField = theirField;
        }
    }
    
    return { field: bestField, score: bestScore };
}

/**
 * Get similarity badge emoji based on score
 */
function getSimilarityEmoji(score) {
    if (score >= 0.80) return '🟢';
    if (score >= 0.70) return '🟡';
    return '🔴';
}

/**
 * Get CSS class for similarity badge
 */
function getSimilarityClass(score) {
    if (score >= 0.80) return 'high';
    if (score >= 0.70) return 'medium';
    return 'low';
}

/**
 * UPDATED: Confirm mapping and send BOTH input and output mappings to backend
 */
function confirmMapping() {
    // Collect INPUT mappings
    const inputMappings = {};
    const inputSelects = document.querySelectorAll('.mapping-select[data-mapping-type="input"]');
    
    let allInputsMapped = true;
    inputSelects.forEach(select => {
        const ourField = select.name;
        const theirField = select.value;
        
        if (!theirField) {
            allInputsMapped = false;
        }
        
        inputMappings[ourField] = theirField;
    });
    
    // Collect OUTPUT mappings
    const outputMappings = {};
    const outputSelects = document.querySelectorAll('.mapping-select[data-mapping-type="output"]');
    
    let allOutputsMapped = true;
    outputSelects.forEach(select => {
        const ourField = select.name;
        const theirField = select.value;
        
        if (!theirField) {
            allOutputsMapped = false;
        }
        
        outputMappings[ourField] = theirField;
    });
    
    // Validate that ALL fields are mapped
    if (!allInputsMapped || !allOutputsMapped) {
        let message = '⚠️ Please map all fields before confirming!\n\n';
        if (!allInputsMapped) message += '• Some INPUT fields are not mapped\n';
        if (!allOutputsMapped) message += '• Some OUTPUT fields are not mapped\n';
        alert(message);
        return;
    }
    
    // Hide mapping section and show loading
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
    
    // Prepare data to send with BOTH mappings
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
    
    // Add BOTH mappings as JSON strings
    formData.append('inputMappings', JSON.stringify(inputMappings));
    formData.append('outputMappings', JSON.stringify(outputMappings));
    
    // Send request to backend
    fetch('MediatorServlet', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => {
        displayResults(data, originalSearchContext.bookerName);
    })
    .catch(error => {
        console.error('Error after mapping confirmation:', error);
        document.getElementById('cottageList').innerHTML = `
            <div class="error-message">
                <strong>❌ Error:</strong> Unable to search cottages with the provided mappings.
                <br><br>
                <strong>Error details:</strong> ${error.message}
            </div>
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
    alert('Mapping cancelled. Please try searching again or use a different service URL.');
}
