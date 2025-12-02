/**
 * Cottage Booking Mediator Client JavaScript
 * Task 7-2: Mediator Service Client
 * Handles communication between user interface and SSWAP Mediator Servlet
 */

/**
 * Main function called when user clicks "Search Available Cottages"
 * Collects form data and sends request to mediator servlet
 */
function searchCottages() {
    // Get service URL
    const serviceURL = document.getElementById('serviceURL').value.trim();
    
    // Get all search input values from the form
    const bookerName = document.getElementById('bookerName').value.trim();
    const numPeople = document.getElementById('numPeople').value;
    const numBedrooms = document.getElementById('numBedrooms').value;
    const maxDistLake = document.getElementById('maxDistLake').value;
    const city = document.getElementById('city').value.trim();
    const maxDistCity = document.getElementById('maxDistCity').value;
    const numDays = document.getElementById('numDays').value;
    const startDate = document.getElementById('startDate').value;
    const dateShift = document.getElementById('dateShift').value;

    // Validate required fields (city is optional)
    if (!serviceURL) {
        alert('⚠️ Please enter the SSWAP Service URL!');
        return;
    }
    
    if (!bookerName || !numPeople || !numBedrooms || !maxDistLake || 
        !maxDistCity || !numDays || !startDate || dateShift === '') {
        alert('⚠️ Please fill in all required fields before searching!');
        return;
    }
    
    // Show loading indicator
    document.getElementById('loading').style.display = 'block';
    document.getElementById('error').style.display = 'none';
    document.getElementById('resultsSection').style.display = 'none';
    
    // Prepare data to send to mediator server
    const requestData = {
        reqType: 'searchCottage',
        serviceURL: serviceURL,
        bookerName: bookerName,
        numberOfPeople: numberOfPeople,
        numberOfBedrooms: numberOfBedrooms,
        maxDistanceFromLake: maxDistanceFromLake,
        cityName: cityName,
        maxCityDistance: maxCityDistance,
        numberOfDays: numberOfDays,
        startDate: startDate,
        possibleShift: possibleShift
    };
    alert(cityName);
    // Convert data to URL-encoded format
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

    // Send request to mediator servlet
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
        // Display the results
        displayResults(data, bookerName);
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
        // Re-enable the search button
        searchBtn.disabled = false;
        searchBtn.querySelector('.btn-text').style.display = 'inline-block';
        searchBtn.querySelector('.btn-loader').style.display = 'none';
    });
}

/**
 * Convert date from YYYY-MM-DD to a readable format
 */
function formatDateForDisplay(dateString) {
    if (!dateString) return 'N/A';
    
    // Handle both YYYY-MM-DD and DD.MM.YYYY formats
    let date;
    if (dateString.includes('-')) {
        // YYYY-MM-DD format
        date = new Date(dateString);
    } else if (dateString.includes('.')) {
        // DD.MM.YYYY format
        const [day, month, year] = dateString.split('.');
        date = new Date(year, month - 1, day);
    } else {
        return dateString; // Return as-is if format not recognized
    }
    
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

/**
 * Calculate duration between two dates
 */
function calculateDuration(startDate, endDate) {
    if (!startDate || !endDate) return 'N/A';
    
    let start, end;
    
    // Handle YYYY-MM-DD format
    if (startDate.includes('-')) {
        start = new Date(startDate);
    } else if (startDate.includes('.')) {
        const [day, month, year] = startDate.split('.');
        start = new Date(year, month - 1, day);
    }
    
    // Handle YYYY-MM-DD format
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

/**
 * Display the cottage search results
 */
function displayResults(data, bookerName) {
    const cottageList = document.getElementById('cottageList');
    
    // Check if response has error
    if (data.error) {
        cottageList.innerHTML = `
            <div class="error-message">
                <strong>❌ Error:</strong> ${data.error}
            </div>
        `;
        return;
    }
    
    // Check if we have cottages
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

    // Generate HTML for each cottage
    let html = '';
    cottages.forEach((cottage, index) => {
        // Use booking number from backend or generate one
        const bookingNumber = cottage.bookingNumber || 'BK-' + Date.now() + '-' + (index + 1);
        
        // Format dates for display
        const startDateDisplay = formatDateForDisplay(cottage.bookingStartDate || cottage.startDate);
        const endDateDisplay = formatDateForDisplay(cottage.bookingEndDate || cottage.endDate);
        
        // Calculate duration
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
    
    // Smooth scroll to results
    document.getElementById('results').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

/**
 * Book a cottage - Shows "Coming Soon" message
 * This is a placeholder for Task 7 since booking functionality is not implemented yet
 */
function bookCottage(bookingNumber, cottageName) {
    // Show coming soon alert
    alert(`📅 Booking Feature Coming Soon!\n\n` +
          `Cottage: ${cottageName}\n` +
          `Booking Number: ${bookingNumber}\n\n` +
          `The booking functionality will be available in a future update.\n` +
          `For now, please note this cottage's details and contact us directly to complete your booking.`);
}

/**
 * Set minimum date to today for the date input on page load
 */
window.addEventListener('DOMContentLoaded', () => {
    const startDateInput = document.getElementById('startDate');
    const today = new Date().toISOString().split('T')[0];
    startDateInput.setAttribute('min', today);
    
    // Set default date to 7 days from now
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 7);
    startDateInput.value = defaultDate.toISOString().split('T')[0];
});