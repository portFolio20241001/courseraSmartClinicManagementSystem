/*
  Export a function named createPatientRecordRow that takes a patient object as input

   Create a new table row element using document.createElement

   Set the inner HTML of the row with the following table data (td):
    - The appointment date (patient.appointmentDate)
    - The appointment ID (patient.id)
    - The patient ID (patient.patientId)
    - An image element (img) acting as a button:
        - Set the image source to the path of the prescription icon
        - Give it a class name like 'prescription-btn' for styling and event handling
        - Set a data-id attribute to store the patient.id
        - Optionally style the image for cursor pointer and size

   Select the image using querySelector (target the class 'prescription-btn')

   Add a click event listener to this image:
    - When clicked, redirect to 'addPrescription.html'
    - Pass query parameters in the URL: 
        - mode=view 
        - appointmentId=patient.id

   Return the constructed table row element so it can be appended to the DOM later
*/
