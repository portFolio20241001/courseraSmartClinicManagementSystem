/*
  Export a function named createPatientRow that takes three parameters:
    - patient: an object containing patient details
    - appointmentId: the ID of the related appointment
    - doctorId: the ID of the doctor viewing the data

  Create a new <tr> element using document.createElement

  Set the innerHTML of the row with the following columns:
    - A td for the patient ID (with a class "patient-id")
    - A td for the patient name
    - A td for the patient's phone number
    - A td for the patient's email
    - A td containing an image element (img) that acts as an action icon:
        - Set the src attribute to the prescription icon path
        - Add a class name like 'prescription-btn' for targeting
        - Use data-id to store the patient.id
        - Style the icon if desired (e.g., cursor pointer, width)

  Add an event listener to the td with class "patient-id":
    - On click, redirect to 'patientRecord.html'
    - Pass the patient.id and doctorId as query parameters in the URL

  Add an event listener to the image with class "prescription-btn":
    - On click, redirect to 'addPrescription.html'
    - Pass appointmentId and patient.name as query parameters in the URL

  Return the constructed <tr> element so it can be appended to a table in the DOM
*/
