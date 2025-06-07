/* 
 Function Overview:
   - `initializePage()`: Main function that initializes the page by fetching appointment and doctor details and populating the form for the update.
   - `updateAppointment()`: Updates the appointment details by sending the modified appointment data to the server.
   - `getDoctors()`: Fetches available doctors to populate the available times for the selected doctor.

 Key Variables:
   - `token`: Token stored in `localStorage` for user authentication. It's required to validate the user's session and permissions.
   - `appointmentId`, `patientId`, `doctorId`, `patientName`, `doctorName`, `appointmentDate`, `appointmentTime`: All are extracted from the URL parameters. They provide necessary data to pre-fill the form and allow for appointment updates.
   - `doctor`: The doctor object retrieved from the list of doctors. It contains the available times for scheduling.
   
 Page Initialization (`initializePage()`):
   - The page first checks if the `token` and `patientId` are available in `localStorage` or the URL query parameters.
   - If the `token` or `patientId` is missing, the user is redirected to the patient appointments page (`patientAppointments.html`).
   
 Fetch Doctors:
   - `getDoctors()` is called to fetch all doctors. This list helps populate the available times for the selected doctor.
   - The selected doctor (by `doctorId` from the URL) is found in the list, and if found, their available times are displayed as options in the form dropdown (`appointmentTime`).
   - If no doctor is found, an error message is shown.

 Pre-fill Appointment Details:
   - The form is pre-filled with the `patientName`, `doctorName`, `appointmentDate`, and `appointmentTime` from the URL.
   - Additionally, the available times for the selected doctor are dynamically added to the time selection dropdown.

 Handle Form Submission:
   - The form submission (`updateAppointmentForm`) is handled to prevent the default submission behavior using `e.preventDefault()`.
   - The updated appointment data is compiled, including the selected date and time.
   - If both the `appointmentDate` and `appointmentTime` are provided, an update request is sent to the server using the `updateAppointment()` function.
   - If the update is successful, the user is redirected back to the patient appointments page.
   - If the update fails, an error message is shown, explaining the failure.

 Error Handling:
   - If any errors occur while fetching the doctor list (`getDoctors()`), an error message is logged, and an alert is shown to the user.
   - If the form submission is unsuccessful (either due to missing data or server failure), the user is informed via an alert.

 Redirection and Flow:
   - If the appointment update is successful, the user is redirected to the patient appointments page.
   - If the session data (`token` or `patientId`) is missing, the user is redirected to the patient appointments page as a fallback to ensure they can re-authenticate.

 Purpose:
   - This script is used on the page that allows patients to update their existing appointments with a doctor. It ensures the correct data is pre-populated, the form is validated, and the update process is properly handled.

*/
