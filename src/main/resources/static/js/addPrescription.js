/*
  Import the prescription service functions: savePrescription and getPrescription

  Wait for the DOM content to load before running the script
  Inside the event listener:
    - Get references to form input elements and the heading
    - Extract query parameters from the URL:
        - appointmentId: identifies the current appointment
        - mode: determines if the page is in "add" or "view" mode
        - patientName: used to pre-fill the patient name
    - Also retrieve the stored token from localStorage for API calls


  If the heading element exists:
    - Check the "mode" query parameter:
      - If mode is "view": set heading to "View Prescription"
      - Else (default): set heading to "Add Prescription"


  If patientName is available from the URL and the input exists:
    - Set the patientNameInput field with the given name


  If both appointmentId and token are available:
    - Call getPrescription to fetch prescription data from the server
    - If a prescription exists (response.prescription is a non-empty array):
        - Extract the first prescription object
        - Pre-fill all form fields with the data: medication, dosage, notes, etc.
        - Use fallback values (like empty string "") for safety

  Catch and log any errors during fetch to handle cases where no prescription exists


  If mode is "view":
    - Disable all input fields (make them read-only)
    - Hide the save button so the user cannot submit changes


  Attach a click event listener to the "save" button

  On click:
    - Prevent the default form submission behavior
    - Construct a prescription object from input field values
    - Call savePrescription with the object and the token
    - If the save is successful:
        - Show a success alert
        - Redirect or call selectRole('doctor') to return to doctor view
    - If saving fails:
        - Show an error alert with the message
*/
