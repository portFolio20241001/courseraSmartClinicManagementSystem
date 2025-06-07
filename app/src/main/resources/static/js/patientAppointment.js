/* 
 Import the necessary service functions:
   - getPatientAppointments: To fetch all appointments of a patient.
   - getPatientData: To retrieve patient profile data using a stored token.
   - filterAppointments: To filter the patient's appointments based on criteria.

 Initialize important variables:
   - tableBody: Reference the table body element where appointments will be displayed.
   - token: Retrieve the authentication token from localStorage.
   - allAppointments: Array to store all fetched appointment data.
   - filteredAppointments: Array to store filtered results when searching.
   - patientId: To hold the ID of the current patient once retrieved.

 Set up the page when it loads:
   - Wait for the DOM to fully load using the 'DOMContentLoaded' event.
   - Ensure a token exists; if not, stop the execution.
   - Use getPatientData() to fetch the current patient's details.
   - Store the returned patient ID for filtering.
   - Use getPatientAppointments() to fetch all appointments linked to this patient.
   - Filter the results by matching the patientId.
   - Pass the filtered list to the renderAppointments() function.

 Display appointments in a table:
   - Clear existing rows in the table body.
   - Always show the “Actions” column by modifying its CSS if needed.
   - If no appointments exist, display a single row message like “No Appointments Found.”
   - If appointments exist:
     - Loop through each appointment.
     - Display columns: Patient name ("You"), Doctor name, Date, Time, and Action.
     - If the appointment is editable (status 0), add an edit icon or button.
     - Attach an event listener to the edit icon to allow editing the appointment.

 Redirect user to edit their appointment:
   - When the edit icon is clicked:
     - Build a URL with query parameters including: 
       appointmentId, patientId, patientName, doctorName, doctorId, date, and time.
     - Redirect the user to updateAppointment.html with the prepared query string.

Add filtering functionality for search and dropdown:
   - Set up listeners on:
     - A search bar to search by doctor or name.
     - A filter dropdown (e.g., allAppointments, past, upcoming).
   - When a filter changes:
     - Use filterAppointments() service with the search and filter values.
     - Again, ensure only appointments for the current patientId are included.
     - Re-render the filtered appointments using renderAppointments().

*/
