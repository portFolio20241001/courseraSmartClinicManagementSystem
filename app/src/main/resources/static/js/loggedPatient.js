/*
  Import functions to fetch all doctors, filter doctors, and book an appointment
  Import the function to create doctor cards for display


  When the page is fully loaded (DOMContentLoaded):
    - Call loadDoctorCards() to fetch and display all doctors initially


  Function: loadDoctorCards
  Purpose: Fetch all doctors from the backend and display them as cards

   Call getDoctors() to retrieve doctor data
   Clear any existing content from the container div
   Loop through each doctor and:
     - Create a visual card using createDoctorCard()
     - Append it to the content container
   Handle errors (e.g., show console message if fetch fails)


  Function: showBookingOverlay
  Purpose: Display a modal to book an appointment with a selected doctor

   Create a ripple effect at the clicked location for UI feedback
   Build a modal with:
     - Pre-filled doctor and patient details (disabled inputs)
     - Date picker for appointment
     - Dropdown menu of available time slots

   Append the modal to the document body and animate it
   Add click listener to the "Confirm Booking" button:
     - Collect selected date and time
     - Format the time (extract start time for appointment)
     - Prepare the appointment object with doctor, patient, and datetime
     - Use bookAppointment() to send the request to the backend
     - On success: Show alert, remove modal and ripple
     - On failure: Show error message to the user


  Add event listeners to:
    - The search bar (on input)
    - Time filter dropdown (on change)
    - Specialty filter dropdown (on change)

  All inputs call filterDoctorsOnChange() to apply the filters dynamically


  Function: filterDoctorsOnChange
  Purpose: Fetch and display doctors based on user-selected filters

   Read input values (name, time, specialty)
   Set them to 'null' if empty, as expected by backend
   Call filterDoctors(name, time, specialty)
   If doctors are returned:
     - Clear previous content
     - Create and display a card for each doctor
   If no results found, show a message: "No doctors found with the given filters."
   Handle and display any fetch errors


  Function: renderDoctorCards
  Purpose: Render a list of doctor cards passed as an argument
  Used to dynamically render pre-filtered results or external data
*/
