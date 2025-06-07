/*
  Import a utility to generate a table row for each appointment
  Import the function that retrieves all appointment records for the logged-in user


  Get a reference to the <tbody> element of the appointments table (where rows will be added)
  Get a reference to the filter dropdown that allows selection between "upcoming" and "past"


  Function: loadAppointments
  Purpose: Load and display appointments based on the selected filter

   Fetch all appointments using getAppointmentRecord()
   If no appointments are returned:
    - Show a single table row with the message "No appointments found."

   Create a 'today' date (with time set to 00:00:00 for accurate comparison)

   Filter the appointments array:
    - If the filter is "upcoming", keep only appointments with a date >= today
    - If the filter is "past", keep only appointments with a date < today

   If the filtered list is empty:
    - Show a message row saying "No upcoming/past appointments found."

   Otherwise, clear the table body
    - For each filtered appointment:
      - Create a new row using getAppointments()
      - Append it to the table


  Add an event listener to the dropdown menu
  When the user selects a new filter:
    - Call loadAppointments with the selected filter value (either 'upcoming' or 'past')


  When the script first runs:
    - Call loadAppointments("upcoming") to load and show only future appointments by default

*/
