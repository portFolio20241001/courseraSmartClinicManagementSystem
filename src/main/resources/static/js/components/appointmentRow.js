/*
  Export a function named getAppointments that takes an appointment object as input
  Create a new table row element to represent one appointment
  Set the inner HTML of the row with appointment details:
      - Patient Name
      - Doctor Name
      - Appointment Date
      - Appointment Time
      - Action icon (edit) with appointment ID stored in data-id attribute
  Attach a click event listener to the edit icon (img with class 'prescription-btn')
  On click, redirect to 'addPrescription.html' passing appointment.id as a query parameter
    
  Return the constructed table row so it can be appended to the DOM elsewhere
    
*/
