/* 
 Import modules:
   - getPatientAppointments: Fetches all appointments of a given patient.
   - createPatientRecordRow: Creates a table row for each appointment entry.

 DOM element references:
   - tableBody: The body of the table where patient appointments will be listed.
   - token: Retrieved from localStorage to authorize the API call.
   - URL parameters: Used to extract the specific `patientId` and `doctorId` to filter data accordingly.

 Page Initialization:
   - On DOMContentLoaded, call `initializePage()` to load and display data.

 Fetch appointments:
   - Ensure the user token exists.
   - Call `getPatientAppointments()` with the patient ID and token, specifying the user as `"doctor"` to access the correct backend logic.
   - Filter appointments further by checking if `doctorId` matches the one in the query string.

 Render Appointments:
   - Clear any previous content in the table.
   - Always make the "Actions" column visible in the table.
   - If no appointments exist, show a "No Appointments Found" message.
   - Otherwise, loop through filtered data and render each appointment row using `createPatientRecordRow()`.

 Error Handling:
   - All major operations are wrapped in try-catch blocks.
   - Errors are logged to the console for debugging.
   - Alerts are used to notify the user of failure to load data.

*/
