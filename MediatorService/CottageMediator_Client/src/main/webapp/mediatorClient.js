/**
 * Cottage Mediator Client JavaScript
 * Handles form submission and communication with mediator server
 */

function searchCottages() {
    // Get all input values
    const serviceURL = document.getElementById('serviceURL').value.trim();
    const bookerName = document.getElementById('bookerName').value.trim();
    const numberOfPeople = document.getElementById('numberOfPeople').value;
    const numberOfBedrooms = document.getElementById('numberOfBedrooms').value;
    const maxDistanceFromLake = document.getElementById('maxDistanceFromLake').value;
    const cityName = document.getElementById('cityName').value.trim();
    const maxCityDistance = document.getElementById('maxCityDistance').value;
    const numberOfDays = document.getElementById('numberOfDays').value;
    const startDate = document.getElementById('startDate').value;
    const possibleShift = document.getElementById('possibleShift').value;
    
    // Validate required fields
    if (!serviceURL) {
        showError('Please enter the Service URL');
        return;
    }
    
    if (!bookerName) {
        showError('Please enter Booker Name');
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
    for (const key in requestData) {
        formData.append(key, requestData[key]);
    }
    
    // Send request to mediator server
    fetch('MediatorServlet', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Server response was not ok');
        }
        return response.json();
    })
    .then(data => {
        // Hide loading indicator
        document.getElementById('loading').style.display = 'none';
        
        // Display results
        displayResults(data);
    })
    .catch(error => {
        // Hide loading indicator
        document.getElementById('loading').style.display = 'none';
        
        // Show error message
        showError('Error communicating with server: ' + error.message);
        console.error('Error:', error);
    });
}

function displayResults(data) {
    const resultsDiv = document.getElementById('results');
    const resultsSection = document.getElementById('resultsSection');
    
    if (!data || !data.cottages || data.cottages.length === 0) {
        resultsDiv.innerHTML = '<p>No cottages found matching your criteria.</p>';
        resultsSection.style.display = 'block';
        return;
    }
    
    let html = '<p><strong>Found ' + data.cottages.length + ' cottage(s):</strong></p>';
    
    data.cottages.forEach((cottage, index) => {
        html += `
            <div class="cottage-card">
                <h3>🏠 Cottage ${index + 1}: ${cottage.cottageName || 'N/A'}</h3>
                ${cottage.imageURL ? `<img src="${cottage.imageURL}" alt="Cottage" class="cottage-image">` : ''}
                <p><strong>Booking Number:</strong> ${cottage.bookingNumber || 'N/A'}</p>
                <p><strong>Booker Name:</strong> ${cottage.bookerName || 'N/A'}</p>
                <p><strong>Address:</strong> ${cottage.cottageAddress || 'N/A'}</p>
                <p><strong>Capacity:</strong> ${cottage.capacity || 'N/A'} people</p>
                <p><strong>Bedrooms:</strong> ${cottage.numberOfBedrooms || 'N/A'}</p>
                <p><strong>Distance from Lake:</strong> ${cottage.distanceFromLake || 'N/A'} meters</p>
                <p><strong>Nearest City:</strong> ${cottage.cityName || 'N/A'} (${cottage.cityDistance || 'N/A'} km away)</p>
                <p><strong>Booking Period:</strong> ${cottage.bookingStartDate || 'N/A'} to ${cottage.bookingEndDate || 'N/A'}</p>
            </div>
        `;
    });
    
    resultsDiv.innerHTML = html;
    resultsSection.style.display = 'block';
}

function showError(message) {
    const errorDiv = document.getElementById('error');
    errorDiv.textContent = message;
    errorDiv.style.display = 'block';
}

// Set today's date as default
document.addEventListener('DOMContentLoaded', function() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('startDate').value = today;
});